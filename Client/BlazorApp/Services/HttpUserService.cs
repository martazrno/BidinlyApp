using Shared.DTOs.Users;

namespace BlazorApp.Services;

public class HttpUserService
{
    private readonly HttpClient client;

    public HttpUserService(HttpClient client)
    {
        this.client = client;
    }

    public async Task CreateAsync(CreateUserDto dto)
    {
        var response = await client.PostAsJsonAsync("users", dto);
        if (!response.IsSuccessStatusCode)
        {
            string err = await response.Content.ReadAsStringAsync();
            throw new Exception(string.IsNullOrWhiteSpace(err) ? "Unable to create user" : err);
        }
    }

    public async Task<List<UserDto>> GetAllAsync()
    {
        var response = await client.GetAsync("users");
        if (!response.IsSuccessStatusCode)
        {
            string err = await response.Content.ReadAsStringAsync();
            throw new Exception(string.IsNullOrWhiteSpace(err) ? $"Failed to get users: {response.StatusCode}" : err);
        }
        
        var users = await response.Content.ReadFromJsonAsync<List<UserDto>>();
        return users ?? new List<UserDto>();
    }

    public async Task DeleteAsync(int id)
    {
        var response = await client.DeleteAsync($"users/{id}");
        if (!response.IsSuccessStatusCode)
        {
            string err = await response.Content.ReadAsStringAsync();
            throw new Exception(string.IsNullOrWhiteSpace(err) ? $"Failed to delete user {id}" : err);
        }
    }

    public async Task<UserDto> ToggleActiveAsync(int id)
    {
        var response = await client.PatchAsync($"users/{id}/toggle-active", null);
        if (!response.IsSuccessStatusCode)
        {
            string err = await response.Content.ReadAsStringAsync();
            throw new Exception(string.IsNullOrWhiteSpace(err) ? $"Failed to toggle active status for user {id}" : err);
        }
        
        var user = await response.Content.ReadFromJsonAsync<UserDto>();
        return user ?? throw new Exception("Failed to deserialize user response");
    }
}

