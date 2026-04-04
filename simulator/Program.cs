using System.Collections.Concurrent;
using System.Diagnostics;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using System.Text.Json.Nodes;

#region Bootstrap — load config.json

var configPath = args.Length > 0 ? args[0] : "config.json";
if (!File.Exists(configPath))
{
    Console.Error.WriteLine($"[ERROR] Config file not found: {configPath}");
    return 1;
}

var config = JsonNode.Parse(await File.ReadAllTextAsync(configPath))!;
var simulation = config["simulation"]!.AsObject();
var apis = config["apis"]!.AsObject();
var endpoints = config["endpoints"]!.AsObject();

int nUsers = simulation["concurrentUsers"]!.GetValue<int>();
int ideasPerUser = simulation["ideasPerUser"]!.GetValue<int>();
int votesPerUser = simulation["votesPerUser"]!.GetValue<int>();
int delayMs = simulation["delayBetweenRequestsMs"]?.GetValue<int>() ?? 0;

#endregion

#region HTTP clients — one per API declared in config

/// <summary>
/// Initializes one <see cref="HttpClient"/> per API key declared in the <c>apis</c> section
/// of <c>config.json</c>. The base address already includes the versioned path prefix
/// (e.g. <c>http://localhost:8081/api/v1/</c>) so endpoint paths need only supply the
/// resource segment (e.g. <c>users</c>).
/// </summary>
var clients = apis.ToDictionary(
    kv => kv.Key,
    kv => new HttpClient { BaseAddress = new Uri(kv.Value!.ToString().TrimEnd('/') + "/") });

#endregion

#region Shared state — thread-safe result collectors

/// <summary>
/// Accumulates the complete log produced by each simulated user.
/// Written from multiple concurrent tasks; uses <see cref="ConcurrentBag{T}"/> for thread safety.
/// </summary>
var allLogs = new ConcurrentBag<string>();

/// <summary>
/// Accumulates every error line (any non-success HTTP response or network exception)
/// encountered across all concurrent simulation tasks.
/// The process exits with code <c>1</c> when this bag is non-empty.
/// </summary>
var allErrors = new ConcurrentBag<string>();

#endregion

#region Data generation

/// <summary>
/// Produces a random string value for a single body or path-parameter field, driven
/// entirely by the <paramref name="type"/> declared in <c>config.json</c>.
/// </summary>
/// <param name="type">
/// Field type as declared in <c>config.json</c>. Recognised values:
/// <list type="bullet">
///   <item><term>name</term><description>Random display name prefixed with <c>SimUser_</c>.</description></item>
///   <item><term>username</term><description>Random login handle prefixed with <c>sim_</c>.</description></item>
///   <item><term>base64password</term><description>Base64-encoded random password string.</description></item>
///   <item><term>sentence</term><description>Short phrase; use <paramref name="extra"/> as the leading word.</description></item>
///   <item><term>uuid</term><description>Random UUID v4.</description></item>
///   <item><term>context</term><description>Value looked up from <paramref name="ctx"/> by the key in <paramref name="extra"/>.</description></item>
/// </list>
/// Any unrecognised type falls back to an 8-character random hex string.
/// </param>
/// <param name="extra">
/// Auxiliary hint whose meaning depends on <paramref name="type"/>:
/// for <c>sentence</c> it is the prefix word; for <c>context</c> it is the context key to look up.
/// </param>
/// <param name="ctx">
/// Per-user context dictionary carrying values produced by earlier flow steps
/// (e.g. <c>userId</c>, <c>topicId</c>, <c>token</c>).
/// </param>
/// <param name="rnd">
/// Per-user <see cref="Random"/> instance that ensures each user's generated data is
/// independent but deterministic given the same seed.
/// </param>
/// <returns>The generated string value, ready to be serialised into a JSON body.</returns>
static string GenValue(string type, string? extra, Dictionary<string, string> ctx, Random rnd)
    => type switch
    {
        "name" => $"SimUser_{Guid.NewGuid():N}"[..16],
        "username" => $"sim_{Guid.NewGuid():N}"[..12],
        "base64password" => Convert.ToBase64String(Encoding.UTF8.GetBytes($"Pass_{rnd.Next(1000, 9999)}")),
        "sentence" => $"{extra ?? "Item"} {Guid.NewGuid():N}"[..24],
        "uuid" => Guid.NewGuid().ToString(),
        "context" => ctx.GetValueOrDefault(extra ?? string.Empty, string.Empty),
        _ => $"{Guid.NewGuid():N}"[..8]
    };

#endregion

#region HTTP call engine

/// <summary>
/// Builds and dispatches a single HTTP request driven entirely by the endpoint definition
/// stored in <c>config.json</c> under <paramref name="endpointKey"/>.
/// </summary>
/// <remarks>
/// The method resolves <c>{placeholder}</c> tokens in the path using <c>pathParams</c>
/// definitions, constructs the JSON request body by calling <see cref="GenValue"/> for each
/// declared field, and attaches the Bearer token when <paramref name="jwt"/> is provided.
/// </remarks>
/// <param name="endpointKey">
/// Key that must match an entry in the <c>endpoints</c> section of <c>config.json</c>
/// (e.g. <c>"createTopic"</c>, <c>"castVote"</c>).
/// </param>
/// <param name="ctx">
/// Per-user context dictionary used to resolve <c>context</c>-typed fields and path parameters.
/// </param>
/// <param name="rnd">Per-user <see cref="Random"/> instance forwarded to <see cref="GenValue"/>.</param>
/// <param name="jwt">
/// Optional JWT string. When supplied, the request includes the header
/// <c>Authorization: Bearer {jwt}</c>.
/// </param>
/// <returns>
/// A tuple of (<c>Status</c>, <c>Body</c>) where:
/// <list type="bullet">
///   <item><description><c>-1</c> — endpoint key or API key not found in config.</description></item>
///   <item><description><c>0</c>  — network-level exception; body contains the error message.</description></item>
///   <item><description>Any other value — the actual HTTP status code returned by the server.</description></item>
/// </list>
/// </returns>
async Task<(int Status, JsonNode? Body)> Call(
    string endpointKey,
    Dictionary<string, string> ctx,
    Random rnd,
    string? jwt = null)
{
    if (!endpoints.ContainsKey(endpointKey))
        return (-1, JsonNode.Parse($"{{\"error\":\"Endpoint '{endpointKey}' not found in config.json\"}}"));

    var ep = endpoints[endpointKey]!.AsObject();
    var apiKey = ep["api"]!.ToString();
    var method = ep["method"]!.ToString();
    var path = ep["path"]!.ToString().TrimStart('/');

    if (ep["pathParams"] is JsonObject pathParams)
        foreach (var (pKey, pDef) in pathParams)
        {
            var val = GenValue(pDef!["type"]!.ToString(), pDef["key"]?.ToString(), ctx, rnd);
            path = path.Replace($"{{{pKey}}}", val);
        }

    if (!clients.TryGetValue(apiKey, out var client))
        return (-1, JsonNode.Parse($"{{\"error\":\"API key '{apiKey}' not in config.json apis section\"}}"));

    var req = new HttpRequestMessage(new HttpMethod(method), path);

    if (jwt != null)
        req.Headers.Authorization = new AuthenticationHeaderValue("Bearer", jwt);

    if (ep["body"] is JsonObject bodyDef)
    {
        var bodyObj = new JsonObject();
        foreach (var (field, fieldDef) in bodyDef)
        {
            var type = fieldDef!["type"]!.ToString();
            var extra = fieldDef["key"]?.ToString() ?? fieldDef["prefix"]?.ToString();
            bodyObj[field] = GenValue(type, extra, ctx, rnd);
        }
        req.Content = new StringContent(bodyObj.ToJsonString(), Encoding.UTF8, "application/json");
    }

    try
    {
        var res = await client.SendAsync(req);
        var text = await res.Content.ReadAsStringAsync();
        JsonNode? body = null;
        if (!string.IsNullOrWhiteSpace(text))
            try { body = JsonNode.Parse(text); } catch { body = JsonValue.Create(text); }
        return ((int)res.StatusCode, body);
    }
    catch (Exception ex)
    {
        return (0, JsonNode.Parse($"{{\"error\":\"{ex.Message.Replace("\"", "\\\"").Replace("\n", " ")}\"}}"));
    }
}

#endregion

#region User simulation

/// <summary>
/// Executes the complete six-step CIS platform flow for a single simulated user.
/// </summary>
/// <remarks>
/// The flow is:
/// <list type="number">
///   <item><description><b>Register</b> — creates a new account via the Java Users API.</description></item>
///   <item><description><b>Login</b> — authenticates and stores the JWT in the user context.</description></item>
///   <item><description><b>Create Topic</b> — posts a topic to the .NET CIS API using the JWT.</description></item>
///   <item><description><b>Create Ideas</b> — posts <c>ideasPerUser</c> ideas under the topic.</description></item>
///   <item><description><b>Cast Votes</b> — votes on up to <c>votesPerUser</c> of those ideas.</description></item>
///   <item><description><b>Unvote</b> — deletes one vote to verify the DELETE path and relational consistency.</description></item>
/// </list>
/// Steps 1 and 2 are mandatory: failure in either aborts the user's flow and records the error.
/// Steps 3–6 continue on partial failure to maximise coverage data collected per run.
/// </remarks>
/// <param name="id">
/// Numeric identifier for this simulated user. Used as the log tag prefix
/// and as part of the seed for the per-user <see cref="Random"/> instance so that
/// each user's data sequence is independent but reproducible.
/// </param>
async Task SimulateUser(int id)
{
    var rnd = new Random(id * 31 + Environment.TickCount);
    var ctx = new Dictionary<string, string>();
    var log = new StringBuilder();
    var tag = $"[User-{id:D3}]";

    /// <summary>Appends a success entry to this user's log buffer.</summary>
    void Ok(string step, string detail)
        => log.AppendLine($"{tag} ✓ {step,-20} {detail}");

    /// <summary>
    /// Appends an error entry to this user's log buffer and to the shared
    /// <see cref="allErrors"/> bag so it surfaces in the final report.
    /// </summary>
    void Err(string step, int status, JsonNode? body)
    {
        var msg = $"{tag} ✗ {step,-20} HTTP {status} | {body}";
        log.AppendLine(msg);
        allErrors.Add(msg);
    }

    // Step 1 — Register
    var rawPass = $"Pass_{rnd.Next(1000, 9999)}";
    ctx["name"] = $"SimUser_{id}";
    ctx["login"] = $"sim_{Guid.NewGuid():N}"[..12];
    ctx["rawPassword"] = rawPass;
    ctx["password"] = Convert.ToBase64String(Encoding.UTF8.GetBytes(rawPass));

    var (rs, rb) = await Call("register", ctx, rnd);
    if (rs == 201)
    {
        ctx["userId"] = rb?["id"]?.ToString() ?? string.Empty;
        Ok("register", $"userId={ctx["userId"]}");
    }
    else
    {
        Err("register", rs, rb);
        allLogs.Add(log.ToString());
        return;
    }

    if (delayMs > 0) await Task.Delay(delayMs);

    // Step 2 — Login → JWT
    var (ls, lb) = await Call("login", ctx, rnd);
    if (ls == 200 && lb?["token"] != null)
    {
        ctx["token"] = lb["token"]!.ToString();
        Ok("login", "JWT obtained");
    }
    else
    {
        Err("login", ls, lb);
        allLogs.Add(log.ToString());
        return;
    }

    var jwt = ctx["token"];
    if (delayMs > 0) await Task.Delay(delayMs);

    // Step 3 — Create Topic
    var (ts, tb) = await Call("createTopic", ctx, rnd, jwt);
    if (ts is 200 or 201)
    {
        ctx["topicId"] = tb?["id"]?.ToString() ?? string.Empty;
        Ok("createTopic", $"topicId={ctx["topicId"]}");
    }
    else Err("createTopic", ts, tb);

    if (delayMs > 0) await Task.Delay(delayMs);

    // Step 4 — Create Ideas
    var ideaIds = new List<string>();
    for (int i = 0; i < ideasPerUser; i++)
    {
        var (is2, ib) = await Call("createIdea", ctx, rnd, jwt);
        if (is2 is 200 or 201)
        {
            var ideaId = ib?["id"]?.ToString() ?? string.Empty;
            ideaIds.Add(ideaId);
            Ok($"createIdea[{i + 1}]", $"ideaId={ideaId}");
        }
        else Err($"createIdea[{i + 1}]", is2, ib);

        if (delayMs > 0) await Task.Delay(delayMs);
    }

    // Step 5 — Vote on ideas
    var voteIds = new List<string>();
    foreach (var ideaId in ideaIds.Take(votesPerUser))
    {
        ctx["ideaId"] = ideaId;
        var (vs, vb) = await Call("castVote", ctx, rnd, jwt);
        if (vs is 200 or 201)
        {
            var voteId = vb?["id"]?.ToString() ?? string.Empty;
            voteIds.Add(voteId);
            Ok("castVote", $"idea={ideaId} → voteId={voteId}");
        }
        else Err("castVote", vs, vb);

        if (delayMs > 0) await Task.Delay(delayMs);
    }

    // Step 6 — Unvote (verifies DELETE + relational consistency)
    foreach (var voteId in voteIds.Take(1))
    {
        ctx["voteId"] = voteId;
        var (ds, _) = await Call("deleteVote", ctx, rnd, jwt);
        if (ds is 200 or 204) Ok("deleteVote", $"voteId={voteId} removed");
        else Err("deleteVote", ds, null);
    }

    log.AppendLine($"{tag} Done");
    allLogs.Add(log.ToString());
}

#endregion

#region Entry point — run and report

Console.WriteLine("╔══════════════════════════════════════════════════════════════╗");
Console.WriteLine("║           CIS API Traffic Simulator                          ║");
Console.WriteLine("╚══════════════════════════════════════════════════════════════╝");
Console.WriteLine($"  Concurrent users : {nUsers}");
Console.WriteLine($"  Ideas per user   : {ideasPerUser}");
Console.WriteLine($"  Votes per user   : {votesPerUser}");
Console.WriteLine($"  Delay between    : {delayMs} ms");
Console.WriteLine($"  Config file      : {Path.GetFullPath(configPath)}");
Console.WriteLine(new string('─', 64));

var sw = Stopwatch.StartNew();
await Task.WhenAll(Enumerable.Range(1, nUsers).Select(SimulateUser));
sw.Stop();

foreach (var entry in allLogs.OrderBy(x => x))
    Console.Write(entry);

if (!allErrors.IsEmpty)
{
    Console.ForegroundColor = ConsoleColor.Red;
    Console.WriteLine($"\n── ERRORS ({allErrors.Count}) {new string('─', 48)}");
    foreach (var e in allErrors)
        Console.WriteLine(e);
    Console.ResetColor();
}

int errCount = allErrors.Count;
Console.WriteLine($"\n── SUMMARY {new string('─', 53)}");
Console.WriteLine($"  Elapsed          : {sw.Elapsed.TotalSeconds:F2}s");
Console.WriteLine($"  Users simulated  : {nUsers}");
Console.WriteLine($"  Errors logged    : {errCount}");

if (errCount == 0)
{
    Console.ForegroundColor = ConsoleColor.Green;
    Console.WriteLine("  Result           : ALL FLOWS COMPLETED SUCCESSFULLY");
}
else
{
    Console.ForegroundColor = ConsoleColor.Yellow;
    Console.WriteLine($"  Result           : COMPLETED WITH {errCount} ERROR(S) — check logs above");
}
Console.ResetColor();

return errCount == 0 ? 0 : 1;

#endregion
