using Grpc.Net.Client;
using Grpc.Core;
using MainServer.WebAPI.Protos;
using System.Net.Http;

namespace MainServer.WebAPI.Services
{
    public class PropertyGrpcClient
    {
        private readonly PropertyService.PropertyServiceClient client;
        private readonly GrpcChannel channel;

        public PropertyGrpcClient(IConfiguration configuration)
        {
            var propertyServiceAddress = configuration["PropertyService:Address"] ?? "http://localhost:9095";

            var httpHandler = new HttpClientHandler();

            channel = GrpcChannel.ForAddress(propertyServiceAddress, new GrpcChannelOptions
            {
                HttpHandler = httpHandler,
                MaxReceiveMessageSize = null,
                MaxSendMessageSize = null
            });

            client = new PropertyService.PropertyServiceClient(channel);
        }

        public async Task<GetPropertiesResponse> GetPropertiesAsync(int? agentId = null, string? status = null, string? creationStatus = null)
        {
            try
            {
                var request = new GetPropertiesRequest();
                if (agentId.HasValue) request.AgentId = agentId.Value;
                if (!string.IsNullOrWhiteSpace(status)) request.Status = status;
                if (!string.IsNullOrWhiteSpace(creationStatus)) request.CreationStatus = creationStatus;

                return await client.GetPropertiesAsync(request);
            }
            catch (RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
        }

        public async Task<PropertyResponse> GetPropertyAsync(int id)
        {
            try
            {
                var request = new GetPropertyRequest { Id = id };
                return await client.GetPropertyAsync(request);
            }
            catch (RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
        }

        public async Task<PropertyResponse> CreatePropertyAsync(CreatePropertyRequest request)
        {
            try
            {
                return await client.CreatePropertyAsync(request);
            }
            catch (RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
        }

        public async Task<PropertyResponse> UpdatePropertyAsync(UpdatePropertyRequest request)
        {
            try
            {
                return await client.UpdatePropertyAsync(request);
            }
            catch (RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
        }
        
        public async Task<PropertyResponse> SetPropertyStatusAsync(int propertyId, string status)
        {
            var request = new UpdatePropertyRequest
            {
                Id = propertyId,
                Status = status
            };

            return await UpdatePropertyAsync(request);
        }

        public async Task<bool> DeletePropertyAsync(int id)
        {
            try
            {
                var response = await client.DeletePropertyAsync(new DeletePropertyRequest { Id = id });
                return response.Success;
            }
            catch (RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
        }
    }
}

