using Shared.DTOs.Sales;

namespace BlazorApp.Services;

public class HttpSaleService
{
    private readonly HttpClient client;

    public HttpSaleService(HttpClient client)
    {
        this.client = client;
    }

    public async Task<List<SaleDto>> GetAllAsync()
    {
        var result = await client.GetFromJsonAsync<List<SaleDto>>("sales");
        return result ?? new List<SaleDto>();
    }

    public async Task<SaleDto?> GetByIdAsync(int id)
    {
        return await client.GetFromJsonAsync<SaleDto>($"sales/{id}");
    }

    public async Task<SaleDto> CreateAsync(SaleDto dto)
    {
        var response = await client.PostAsJsonAsync("sales", dto);
        if (!response.IsSuccessStatusCode)
        {
            string err = await response.Content.ReadAsStringAsync();
            throw new Exception(string.IsNullOrWhiteSpace(err) ? "Failed to create sale" : err);
        }

        return await response.Content.ReadFromJsonAsync<SaleDto>()
               ?? throw new Exception("Failed to deserialize sale");
    }
}

