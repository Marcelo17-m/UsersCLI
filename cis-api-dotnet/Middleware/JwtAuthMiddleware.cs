using System.IdentityModel.Tokens.Jwt;
using System.Text;
using CisDotnetApi.Services;
using Microsoft.IdentityModel.Tokens;

namespace CisDotnetApi.Middleware;

public class JwtAuthMiddleware(RequestDelegate next)
{
    private static readonly HashSet<string> ProtectedMethods =
        new(StringComparer.OrdinalIgnoreCase) { "POST", "PUT", "DELETE" };

    public async Task InvokeAsync(HttpContext context, IUserValidationService userValidation, IConfiguration config)
    {
        if (!ProtectedMethods.Contains(context.Request.Method))
        {
            await next(context);
            return;
        }

        var authHeader = context.Request.Headers.Authorization.FirstOrDefault();
        if (authHeader == null || !authHeader.StartsWith("Bearer ", StringComparison.OrdinalIgnoreCase))
        {
            await WriteError(context, 401, "AUTH-401: Unauthorized access");
            return;
        }

        var token = authHeader["Bearer ".Length..];
        var secret = config["Jwt:Secret"] ?? throw new InvalidOperationException("Jwt:Secret not configured");

        string login;
        try
        {
            login = ExtractLogin(token, secret);
        }
        catch
        {
            await WriteError(context, 401, "AUTH-401: Unauthorized access");
            return;
        }

        UserValidationResult result;
        try
        {
            result = await userValidation.ValidateAsync(login);
        }
        catch
        {
            await WriteError(context, 401, "AUTH-401: Unauthorized access");
            return;
        }

        if (!result.Found)
        {
            await WriteError(context, 401, "AUTH-401: Unauthorized access");
            return;
        }

        if (!result.Active)
        {
            await WriteError(context, 403, "AUTH-403: Forbidden - Account Inactive");
            return;
        }

        context.Items["UserId"] = result.UserId;
        context.Items["Login"] = login;
        await next(context);
    }

    private static string ExtractLogin(string token, string secret)
    {
        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secret));
        var handler = new JwtSecurityTokenHandler();
        handler.ValidateToken(token, new TokenValidationParameters
        {
            ValidateIssuer = false,
            ValidateAudience = false,
            ValidateLifetime = true,
            IssuerSigningKey = key,
            ClockSkew = TimeSpan.Zero
        }, out var validated);

        return ((JwtSecurityToken)validated).Subject;
    }

    private static Task WriteError(HttpContext context, int status, string message)
    {
        context.Response.StatusCode = status;
        context.Response.ContentType = "text/plain";
        return context.Response.WriteAsync(message);
    }
}
