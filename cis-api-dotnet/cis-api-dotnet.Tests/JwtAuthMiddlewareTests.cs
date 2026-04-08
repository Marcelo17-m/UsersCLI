using System.IdentityModel.Tokens.Jwt;
using System.Text;
using CisDotnetApi.Middleware;
using CisDotnetApi.Services;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Configuration;
using Microsoft.IdentityModel.Tokens;
using Moq;

namespace cis_api_dotnet.Tests;

public class JwtAuthMiddlewareTests
{
    private const string Secret = "cis-jalau-super-secre-key-2026-sd3-platform";

    private static IConfiguration CreateConfig() =>
        new ConfigurationBuilder()
            .AddInMemoryCollection(new Dictionary<string, string?> { ["Jwt:Secret"] = Secret })
            .Build();

    private static string GenerateToken(string login)
    {
        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(Secret));
        var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);
        var token = new JwtSecurityToken(
            claims: [new System.Security.Claims.Claim(JwtRegisteredClaimNames.Sub, login)],
            expires: DateTime.UtcNow.AddHours(6),
            signingCredentials: creds);
        return new JwtSecurityTokenHandler().WriteToken(token);
    }

    private static DefaultHttpContext BuildContext(string method, string? authHeader = null)
    {
        var ctx = new DefaultHttpContext();
        ctx.Request.Method = method;
        ctx.Response.Body = new MemoryStream();
        if (authHeader != null)
            ctx.Request.Headers.Authorization = authHeader;
        return ctx;
    }

    private static async Task<string> ReadBody(HttpResponse response)
    {
        response.Body.Seek(0, SeekOrigin.Begin);
        return await new StreamReader(response.Body).ReadToEndAsync();
    }

    [Fact]
    public async Task GetRequest_NoToken_PassesThrough()
    {
        var validation = new Mock<IUserValidationService>();
        var middleware = new JwtAuthMiddleware(_ => Task.CompletedTask);
        var ctx = BuildContext("GET");

        await middleware.InvokeAsync(ctx, validation.Object, CreateConfig());

        Assert.Equal(200, ctx.Response.StatusCode);
        validation.VerifyNoOtherCalls();
    }

    [Fact]
    public async Task PostRequest_NoAuthHeader_Returns401()
    {
        var validation = new Mock<IUserValidationService>();
        var middleware = new JwtAuthMiddleware(_ => Task.CompletedTask);
        var ctx = BuildContext("POST");

        await middleware.InvokeAsync(ctx, validation.Object, CreateConfig());

        Assert.Equal(401, ctx.Response.StatusCode);
        Assert.Equal("AUTH-401: Unauthorized access", await ReadBody(ctx.Response));
    }

    [Fact]
    public async Task PostRequest_InvalidToken_Returns401()
    {
        var validation = new Mock<IUserValidationService>();
        var middleware = new JwtAuthMiddleware(_ => Task.CompletedTask);
        var ctx = BuildContext("POST", "Bearer this.is.invalid");

        await middleware.InvokeAsync(ctx, validation.Object, CreateConfig());

        Assert.Equal(401, ctx.Response.StatusCode);
        Assert.Equal("AUTH-401: Unauthorized access", await ReadBody(ctx.Response));
    }

    [Fact]
    public async Task PostRequest_ValidToken_UserNotFound_Returns401()
    {
        var validation = new Mock<IUserValidationService>();
        validation.Setup(s => s.ValidateAsync("ghost"))
            .ReturnsAsync(new UserValidationResult(false, false, string.Empty));

        var middleware = new JwtAuthMiddleware(_ => Task.CompletedTask);
        var ctx = BuildContext("POST", $"Bearer {GenerateToken("ghost")}");

        await middleware.InvokeAsync(ctx, validation.Object, CreateConfig());

        Assert.Equal(401, ctx.Response.StatusCode);
        Assert.Equal("AUTH-401: Unauthorized access", await ReadBody(ctx.Response));
    }

    [Fact]
    public async Task PostRequest_ValidToken_InactiveUser_Returns403()
    {
        var validation = new Mock<IUserValidationService>();
        validation.Setup(s => s.ValidateAsync("jdoe"))
            .ReturnsAsync(new UserValidationResult(true, false, "u1"));

        var middleware = new JwtAuthMiddleware(_ => Task.CompletedTask);
        var ctx = BuildContext("POST", $"Bearer {GenerateToken("jdoe")}");

        await middleware.InvokeAsync(ctx, validation.Object, CreateConfig());

        Assert.Equal(403, ctx.Response.StatusCode);
        Assert.Equal("AUTH-403: Forbidden - Account Inactive", await ReadBody(ctx.Response));
    }

    [Fact]
    public async Task PostRequest_ValidToken_ActiveUser_PassesThrough()
    {
        var validation = new Mock<IUserValidationService>();
        validation.Setup(s => s.ValidateAsync("jdoe"))
            .ReturnsAsync(new UserValidationResult(true, true, "u1"));

        var nextCalled = false;
        var middleware = new JwtAuthMiddleware(_ => { nextCalled = true; return Task.CompletedTask; });
        var ctx = BuildContext("POST", $"Bearer {GenerateToken("jdoe")}");

        await middleware.InvokeAsync(ctx, validation.Object, CreateConfig());

        Assert.True(nextCalled);
        Assert.Equal("u1", ctx.Items["UserId"]);
        Assert.Equal("jdoe", ctx.Items["Login"]);
    }

    [Fact]
    public async Task PostRequest_UsersApiDown_Returns401()
    {
        var validation = new Mock<IUserValidationService>();
        validation.Setup(s => s.ValidateAsync(It.IsAny<string>()))
            .ThrowsAsync(new HttpRequestException("Connection refused"));

        var middleware = new JwtAuthMiddleware(_ => Task.CompletedTask);
        var ctx = BuildContext("POST", $"Bearer {GenerateToken("jdoe")}");

        await middleware.InvokeAsync(ctx, validation.Object, CreateConfig());

        Assert.Equal(401, ctx.Response.StatusCode);
        Assert.Equal("AUTH-401: Unauthorized access", await ReadBody(ctx.Response));
    }

    [Theory]
    [InlineData("PUT")]
    [InlineData("DELETE")]
    public async Task MutatingMethods_WithValidActiveUser_PassesThrough(string method)
    {
        var validation = new Mock<IUserValidationService>();
        validation.Setup(s => s.ValidateAsync("jdoe"))
            .ReturnsAsync(new UserValidationResult(true, true, "u2"));

        var nextCalled = false;
        var middleware = new JwtAuthMiddleware(_ => { nextCalled = true; return Task.CompletedTask; });
        var ctx = BuildContext(method, $"Bearer {GenerateToken("jdoe")}");

        await middleware.InvokeAsync(ctx, validation.Object, CreateConfig());

        Assert.True(nextCalled);
    }
}
