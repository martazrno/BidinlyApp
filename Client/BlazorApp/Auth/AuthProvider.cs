using System.Security.Claims;
using System.Text.Json;
using Microsoft.AspNetCore.Components.Authorization;
using Microsoft.JSInterop;
using Shared.DTOs.Auth;
using Shared.DTOs.Users;

namespace BlazorApp.Auth;

public class AuthProvider : AuthenticationStateProvider
{

    private string userAsJson = string.Empty;
    private readonly HttpClient httpClient;
    private readonly IJSRuntime jsRuntime;

    public AuthProvider(HttpClient httpClient, IJSRuntime jsRuntime)
    {
        this.httpClient = httpClient;
        this.jsRuntime = jsRuntime;
    }

    private async Task<ClaimsPrincipal> CreateClaimsPrincipalAsync(UserDto userDto) //helper
    {
        string? roleName = userDto.RoleName;
        
        // If RoleName is missing, fetch it by calling users/{id} endpoint
        // This endpoint internally calls the roles controller to get the role name
        if (string.IsNullOrEmpty(roleName) && userDto.Id > 0)
        {
            try
            {
                var response = await httpClient.GetAsync($"users/{userDto.Id}");
                if (response.IsSuccessStatusCode)
                {
                    var content = await response.Content.ReadAsStringAsync();
                    var updatedUserDto = JsonSerializer.Deserialize<UserDto>(content, new JsonSerializerOptions { PropertyNameCaseInsensitive = true });
                    if (updatedUserDto != null && !string.IsNullOrEmpty(updatedUserDto.RoleName))
                    {
                        roleName = updatedUserDto.RoleName;
                        // Update the userDto with the fetched role name
                        userDto.RoleName = roleName;
                    }
                }
            }
            catch
            {
                // If fetching fails, roleName remains null/empty
            }
        }

        List<Claim> claims = new List<Claim>()
        {
            new Claim(ClaimTypes.Name, userDto.Username ?? string.Empty),
            new Claim("Id", userDto.Id.ToString()),
            new Claim("RoleName", roleName ?? "")
        };

        ClaimsIdentity identity = new ClaimsIdentity(claims, "apiauth");
        return new ClaimsPrincipal(identity);
    }

    public async Task Login(string userName, string password)
    {
        try
        {
            await jsRuntime.InvokeVoidAsync("console.log", $"[AuthProvider] Login called for user: {userName}");
        }
        catch { }

        HttpResponseMessage response = await httpClient.PostAsJsonAsync(
            "auth/login",
            new LoginRequestDTO { Username = userName, Password = password });
        string content = await response.Content.ReadAsStringAsync();
        userAsJson = content;
        
        if (!response.IsSuccessStatusCode)
        {
            try
            {
                await jsRuntime.InvokeVoidAsync("console.error", $"[AuthProvider] Login failed - Status: {response.StatusCode}, Response: {content}");
            }
            catch { }
            throw new Exception(content);
        }

        try
        {
            await jsRuntime.InvokeVoidAsync("console.log", $"[AuthProvider] Login API call successful - Status: {response.StatusCode}");
        }
        catch { }

        UserDto userDto = JsonSerializer.Deserialize<UserDto>(content, new JsonSerializerOptions { PropertyNameCaseInsensitive = true })!;

        try
        {
            await jsRuntime.InvokeVoidAsync("console.log", $"[AuthProvider] User deserialized - Id: {userDto.Id}, Username: {userDto.Username}, RoleName: {userDto.RoleName ?? "null"}");
        }
        catch { }

        await jsRuntime.InvokeVoidAsync("authStorage.saveUser", content);

        try
        {
            await jsRuntime.InvokeVoidAsync("console.log", "[AuthProvider] User saved to sessionStorage");
        }
        catch { }

        ClaimsPrincipal claimsPrincipal = await CreateClaimsPrincipalAsync(userDto);

        try
        {
            await jsRuntime.InvokeVoidAsync("console.log", $"[AuthProvider] ClaimsPrincipal created - IsAuthenticated: {claimsPrincipal.Identity?.IsAuthenticated}, Name: {claimsPrincipal.Identity?.Name}, RoleName: {userDto.RoleName ?? "null"}");
        }
        catch { }

        NotifyAuthenticationStateChanged(Task.FromResult(new AuthenticationState(claimsPrincipal)));

        try
        {
            await jsRuntime.InvokeVoidAsync("console.log", "[AuthProvider] AuthenticationStateChanged notified");
        }
        catch { }
    }

    public override async Task<AuthenticationState> GetAuthenticationStateAsync()
    {
        try
        {
            await jsRuntime.InvokeVoidAsync("console.log", "[AuthProvider] GetAuthenticationStateAsync called");
        }
        catch { }

        try
        {
            string? storedUser = await jsRuntime.InvokeAsync<string?>("authStorage.getUser");
            userAsJson = storedUser ?? string.Empty;
        }
        catch (InvalidOperationException e)
        {
            try
            {
                await jsRuntime.InvokeVoidAsync("console.warn", $"[AuthProvider] InvalidOperationException getting sessionStorage: {e.Message}");
            }
            catch { }
            return new AuthenticationState(new());
        }
        
        if (string.IsNullOrEmpty(userAsJson))
        {
            try
            {
                await jsRuntime.InvokeVoidAsync("console.log", "[AuthProvider] No user found in sessionStorage - returning unauthenticated state");
            }
            catch { }
            return new AuthenticationState(new());
        }

        try
        {
            await jsRuntime.InvokeVoidAsync("console.log", $"[AuthProvider] User found in sessionStorage - Length: {userAsJson.Length}");
        }
        catch { }

        UserDto userDto = JsonSerializer.Deserialize<UserDto>(userAsJson,
            new JsonSerializerOptions { PropertyNameCaseInsensitive = true })!;
        
        try
        {
            await jsRuntime.InvokeVoidAsync("console.log", $"[AuthProvider] User deserialized from sessionStorage - Id: {userDto.Id}, Username: {userDto.Username}, RoleName: {userDto.RoleName ?? "null"}");
        }
        catch { }

        ClaimsPrincipal claimsPrincipal = await CreateClaimsPrincipalAsync(userDto);

        try
        {
            await jsRuntime.InvokeVoidAsync("console.log", $"[AuthProvider] Returning authenticated state - IsAuthenticated: {claimsPrincipal.Identity?.IsAuthenticated}, Name: {claimsPrincipal.Identity?.Name}, RoleName: {userDto.RoleName ?? "null"}");
        }
        catch { }

        return new AuthenticationState(claimsPrincipal);
        
    }

    public async Task Logout()
    {
        try
        {
            await jsRuntime.InvokeVoidAsync("console.log", "[AuthProvider] Logout called");
        }
        catch { }

        await jsRuntime.InvokeVoidAsync("authStorage.clearUser");
        
        try
        {
            await jsRuntime.InvokeVoidAsync("console.log", "[AuthProvider] User cleared from sessionStorage");
        }
        catch { }

        NotifyAuthenticationStateChanged(Task.FromResult(new AuthenticationState(new())));

        try
        {
            await jsRuntime.InvokeVoidAsync("console.log", "[AuthProvider] AuthenticationStateChanged notified (logged out)");
        }
        catch { }
    }
}