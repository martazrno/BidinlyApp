using Shared.DTOs.Properties;
namespace BlazorApp.Services;

public class HttpPropertyService
{
    private readonly HttpClient client;

    public HttpPropertyService(HttpClient client)
    {
        this.client = client;
    }

    public async Task<List<PropertyDto>> GetAllAsync()
    {
        var properties = await client.GetFromJsonAsync<List<PropertyDto>>("properties");
        return properties ?? new List<PropertyDto>();
    }

    public async Task<List<PropertyDto>> GetByAgentAsync(int agentId)
    {
        var properties = await client.GetFromJsonAsync<List<PropertyDto>>($"properties?agentId={agentId}");
        return properties ?? new List<PropertyDto>();
    }

    public async Task<PropertyDto?> GetByIdAsync(int id)
    {
        return await client.GetFromJsonAsync<PropertyDto>($"properties/{id}");
    }

    public async Task<List<PropertyDto>> GetByCreationStatusAsync(string creationStatus)
    {
        var properties = await client.GetFromJsonAsync<List<PropertyDto>>($"properties?creationStatus={Uri.EscapeDataString(creationStatus)}");
        return properties ?? new List<PropertyDto>();
    }

    public async Task<PropertyDto> CreateAsync(CreatePropertyDto dto)
    {
        var response = await client.PostAsJsonAsync("properties", dto);
        if (!response.IsSuccessStatusCode)
        {
            string err = await response.Content.ReadAsStringAsync();
            throw new Exception(string.IsNullOrWhiteSpace(err) ? "Failed to create property" : err);
        }

        return await response.Content.ReadFromJsonAsync<PropertyDto>()
               ?? throw new Exception("Failed to deserialize property");
    }

    public async Task<PropertyDto> UpdateAsync(UpdatePropertyDto dto)
    {
        var response = await client.PutAsJsonAsync($"properties/{dto.Id}", dto);
        if (!response.IsSuccessStatusCode)
        {
            string err = await response.Content.ReadAsStringAsync();
            throw new Exception(string.IsNullOrWhiteSpace(err) ? "Failed to update property" : err);
        }

        return await response.Content.ReadFromJsonAsync<PropertyDto>()
               ?? throw new Exception("Failed to deserialize property");
    }
    
    public async Task DeleteAsync(int id)
    {
        var response = await client.DeleteAsync($"properties/{id}");

        if (!response.IsSuccessStatusCode)
        {
            string err = await response.Content.ReadAsStringAsync();
            throw new Exception(string.IsNullOrWhiteSpace(err)
                ? "Failed to delete property"
                : err);
        }
    }
}

