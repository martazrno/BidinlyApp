using System.Security.Claims;
using System.Text;
using System.Text.Encodings.Web;
using System.Text.Json;
using Microsoft.AspNetCore.Authentication;
using Microsoft.Extensions.Options;
using Shared.DTOs.Users;

namespace BlazorApp.Auth;

public class AuthCookieAuthenticationOptions : AuthenticationSchemeOptions;

public class AuthCookieAuthenticationHandler : AuthenticationHandler<AuthCookieAuthenticationOptions>
{
    public const string SchemeName = "BidinlyAuth";
    private const string CookieName = "BidinlyAuth";

    public AuthCookieAuthenticationHandler(
        IOptionsMonitor<AuthCookieAuthenticationOptions> options,
        ILoggerFactory logger,
        UrlEncoder encoder) : base(options, logger, encoder)
    {
    }

    protected override Task<AuthenticateResult> HandleAuthenticateAsync()
    {
        if (!Request.Cookies.TryGetValue(CookieName, out var encodedUser) || string.IsNullOrWhiteSpace(encodedUser))
        {
            return Task.FromResult(AuthenticateResult.NoResult());
        }

        string? userJson;
        try
        {
            byte[] data = Convert.FromBase64String(encodedUser);
            userJson = Encoding.UTF8.GetString(data);
        }
        catch (FormatException)
        {
            return Task.FromResult(AuthenticateResult.Fail("Invalid authentication cookie format."));
        }

        if (string.IsNullOrWhiteSpace(userJson))
        {
            return Task.FromResult(AuthenticateResult.Fail("Authentication cookie payload is empty."));
        }

        UserDto? userDto;
        try
        {
            userDto = JsonSerializer.Deserialize<UserDto>(userJson, new JsonSerializerOptions
            {
                PropertyNameCaseInsensitive = true
            });
        }
        catch (JsonException ex)
        {
            return Task.FromResult(AuthenticateResult.Fail($"Invalid authentication cookie payload. {ex.Message}"));
        }

        if (userDto is null || userDto.Id <= 0)
        {
            return Task.FromResult(AuthenticateResult.Fail("Authentication cookie missing user information."));
        }

        var claims = new List<Claim>
        {
            new(ClaimTypes.NameIdentifier, userDto.Id.ToString()),
            new(ClaimTypes.Name, userDto.Username ?? string.Empty),
            new("RoleName", userDto.RoleName ?? string.Empty)
        };

        var identity = new ClaimsIdentity(claims, Scheme.Name);
        var principal = new ClaimsPrincipal(identity);
        var ticket = new AuthenticationTicket(principal, Scheme.Name);

        return Task.FromResult(AuthenticateResult.Success(ticket));
    }

    protected override Task HandleChallengeAsync(AuthenticationProperties properties)
    {
        Response.StatusCode = StatusCodes.Status401Unauthorized;
        return Task.CompletedTask;
    }
}

