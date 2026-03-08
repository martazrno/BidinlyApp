using Microsoft.AspNetCore.Mvc;
using Shared.DTOs.Sales;
using MainServer.WebAPI.Services;
using MainServer.WebAPI.Protos;
using BidResponse = MainServer.WebAPI.Protos.BidResponse;

namespace MainServer.WebAPI.Controllers
{
    [ApiController]
    [Route("sales")]
    public class SalesController : ControllerBase
    {
        private readonly DataTierGrpcClient dataTierClient;
        private readonly PropertyGrpcClient propertyClient;

        public SalesController(DataTierGrpcClient dataTierClient, PropertyGrpcClient propertyClient)
        {
            this.dataTierClient = dataTierClient;
            this.propertyClient = propertyClient;
        }

        [HttpGet]
        public async Task<ActionResult<List<SaleDto>>> GetManySales()
        {
            try
            {
                GetSalesResponse response = await dataTierClient.GetSalesAsync();

                var uniquePropertyIds = response.Sales.Select(s => s.PropertyId).Distinct().ToList();
                var uniqueBuyerIds = response.Sales.Select(s => s.BuyerId).Distinct().ToList();
                var uniqueAgentIds = response.Sales.Select(s => s.AgentId).Distinct().ToList();
                var uniqueBidIds = response.Sales.Select(s => s.BidId).Distinct().ToList();

                var propertyTasks = uniquePropertyIds.Select(async id =>
                {
                    try
                    {
                        var property = await propertyClient.GetPropertyAsync(id);
                        return new { Id = id, Property = (PropertyResponse?)property };
                    }
                    catch
                    {
                        return new { Id = id, Property = (PropertyResponse?)null };
                    }
                });

                var buyerTasks = uniqueBuyerIds.Select(async id =>
                {
                    try
                    {
                        var user = await dataTierClient.GetUserAsync(id);
                        return new { Id = id, User = (UserResponse?)user };
                    }
                    catch
                    {
                        return new { Id = id, User = (UserResponse?)null };
                    }
                });

                var agentTasks = uniqueAgentIds.Select(async id =>
                {
                    try
                    {
                        var user = await dataTierClient.GetUserAsync(id);
                        return new { Id = id, User = (UserResponse?)user };
                    }
                    catch
                    {
                        return new { Id = id, User = (UserResponse?)null };
                    }
                });

                var bidTasks = uniqueBidIds.Select(async id =>
                {
                    try
                    {
                        var bid = await dataTierClient.GetBidAsync(id);
                        return new { Id = id, Bid = (BidResponse?)bid };
                    }
                    catch
                    {
                        return new { Id = id, Bid = (BidResponse?)null };
                    }
                });

                var propertyResults = await Task.WhenAll(propertyTasks);
                var buyerResults = await Task.WhenAll(buyerTasks);
                var agentResults = await Task.WhenAll(agentTasks);
                var bidResults = await Task.WhenAll(bidTasks);

                var propertyLookup = propertyResults.ToDictionary(r => r.Id, r => r.Property);
                var buyerLookup = buyerResults.ToDictionary(r => r.Id, r => r.User);
                var agentLookup = agentResults.ToDictionary(r => r.Id, r => r.User);
                var bidLookup = bidResults.ToDictionary(r => r.Id, r => r.Bid);

                List<SaleDto> responseDtos = response.Sales.Select(saleResponse =>
                {
                    DateTime timeOfSale = DateTimeOffset.FromUnixTimeSeconds(saleResponse.TimeOfSaleSeconds).DateTime;

                    return new SaleDto
                    {
                        Id = saleResponse.Id,
                        TimeOfSale = timeOfSale,
                        PropertyId = saleResponse.PropertyId,
                        PropertyTitle = propertyLookup.GetValueOrDefault(saleResponse.PropertyId)?.Title,
                        BuyerUsername = buyerLookup.GetValueOrDefault(saleResponse.BuyerId)?.Username,
                        AgentUsername = agentLookup.GetValueOrDefault(saleResponse.AgentId)?.Username,
                        WinningBidId = saleResponse.BidId,
                        FinalAmount = bidLookup.GetValueOrDefault(saleResponse.BidId) != null 
                            ? (decimal)bidLookup.GetValueOrDefault(saleResponse.BidId)!.Amount 
                            : 0
                    };
                }).ToList();

                return Ok(responseDtos);
            }
            catch
            {
                throw;
            }
        }
    }
}

