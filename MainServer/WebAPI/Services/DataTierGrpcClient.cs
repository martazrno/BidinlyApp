using Grpc.Net.Client;
using MainServer.WebAPI.Protos;
using System.Net.Http;

namespace MainServer.WebAPI.Services
{
    public class DataTierGrpcClient
    {
        private readonly BidService.BidServiceClient bidClient;
        private readonly UserService.UserServiceClient userClient;
        private readonly SaleService.SaleServiceClient saleClient;
        private readonly RoleService.RoleServiceClient roleClient;
        private readonly AuthService.AuthServiceClient authClient;
        private readonly NotificationService.NotificationServiceClient notificationClient;
        private readonly GrpcChannel channel;

        public DataTierGrpcClient(IConfiguration configuration)
        {
            var dataTierAddress = configuration["DataTier:Address"] ?? "http://localhost:9093";
            
            var httpHandler = new HttpClientHandler();
            
            channel = GrpcChannel.ForAddress(dataTierAddress, new GrpcChannelOptions
            {
                HttpHandler = httpHandler,
                MaxReceiveMessageSize = null,
                MaxSendMessageSize = null
            });
            
            bidClient = new BidService.BidServiceClient(channel);
            userClient = new UserService.UserServiceClient(channel);
            saleClient = new SaleService.SaleServiceClient(channel);
            roleClient = new RoleService.RoleServiceClient(channel);
            authClient = new AuthService.AuthServiceClient(channel);
            notificationClient = new NotificationService.NotificationServiceClient(channel);
        }

        // Bid operations

        public async Task<GetBidsResponse> GetBidsAsync()
        {
            try
            {
                var request = new GetBidsRequest();
                var response = await bidClient.GetBidsAsync(request);
                return response;
            }
            catch (Grpc.Core.RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
            catch
            {
                throw;
            }
        }


        public async Task<BidResponse> GetBidAsync(int id)
        {
            try
            {
                var request = new GetBidRequest { Id = id };
                var response = await bidClient.GetBidAsync(request);
                return response;
            }
            catch (Grpc.Core.RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
            catch
            {
                throw;
            }
        }

        public async Task<BidResponse> CreateBidAsync(int buyerId, int propertyId, double amount, long expiryDateSeconds, string? deal)
        {
            try
            {
                CreateBidRequest request = new CreateBidRequest
                {
                    BuyerId = buyerId,
                    PropertyId = propertyId,
                    Amount = amount,
                    ExpiryDateSeconds = expiryDateSeconds
                };
                if (!string.IsNullOrWhiteSpace(deal))
                    request.Deal = deal;
                
                BidResponse response = await bidClient.CreateBidAsync(request);
                return response;
            }
            catch (Grpc.Core.RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
            catch
            {
                throw;
            }
        }

        public async Task<bool> DeleteBidAsync(int id)
        {
            var request = new DeleteBidRequest { Id = id };
            var response = await bidClient.DeleteBidAsync(request);
            return response.Success;
        }

        // User operations
        public async Task<UserResponse> GetUserAsync(int id)
        {
            try
            {
                var request = new GetUserRequest { Id = id };
                var response = await userClient.GetUserAsync(request);
                return response;
            }
            catch (Grpc.Core.RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
            catch
            {
                throw;
            }
        }

        public async Task<UserResponse> CreateUserAsync(string username, string password, int roleId, string? email = null)
        {
            try
            {
                var request = new CreateUserRequest
                {
                    Username = username,
                    Password = password,
                    RoleId = roleId
                };
                
                if (!string.IsNullOrWhiteSpace(email))
                {
                    request.Email = email;
                }
                
                UserResponse response = await userClient.CreateUserAsync(request);
                return response;
            }
            catch (Grpc.Core.RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
        }

        public async Task<GetUsersResponse> GetUsersAsync()
        {
            try
            {
                var request = new GetUsersRequest();
                var response = await userClient.GetUsersAsync(request);
                return response;
            }
            catch (Grpc.Core.RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
            catch
            {
                throw;
            }
        }

        public async Task<bool> DeleteUserAsync(int id)
        {
            try
            {
                var request = new DeleteUserRequest { Id = id };
                var response = await userClient.DeleteUserAsync(request);
                return response.Success;
            }
            catch (Grpc.Core.RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
            catch
            {
                throw;
            }
        }

        public async Task<UserResponse> UpdateUserAsync(int id, string? username = null, string? password = null, int? roleId = null, bool? isActive = null, string? email = null)
        {
            try
            {
                var requestBuilder = new UpdateUserRequest { Id = id };
                
                if (username != null)
                {
                    requestBuilder.Username = username;
                }
                
                if (password != null)
                {
                    requestBuilder.Password = password;
                }
                
                if (roleId.HasValue)
                {
                    requestBuilder.RoleId = roleId.Value;
                }
                
                if (isActive.HasValue)
                {
                    requestBuilder.IsActive = isActive.Value;
                }
                
                if (email != null)
                {
                    requestBuilder.Email = email;
                }
                
                var response = await userClient.UpdateUserAsync(requestBuilder);
                return response;
            }
            catch (Grpc.Core.RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
            catch
            {
                throw;
            }
        }

        // Sale operations
        public async Task<GetSalesResponse> GetSalesAsync()
        {
            try
            {
                var request = new GetSalesRequest();
                var response = await saleClient.GetSalesAsync(request);
                return response;
            }
            catch (Grpc.Core.RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
            catch
            {
                throw;
            }
        }

        // Role operations
        public async Task<GetRoleResponse> GetRoleAsync(int roleId)
        {
            try
            {
                GetRoleRequest request = new GetRoleRequest { RoleId = roleId };
                GetRoleResponse response = await roleClient.GetRoleAsync(request);
                return response;
            }
            catch (Grpc.Core.RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
            catch
            {
                throw;
            }
        }

        // Login operations
        public async Task<LoginResponse> AuthenticateUserAsync(string username, string password)
        {
            try
            {
                var request = new MainServer.WebAPI.Protos.LoginRequest
                {
                    Username = username,
                    Password = password
                };
                
                var response = await authClient.AuthenticateUserAsync(request);
                return response;
            }
            catch (Grpc.Core.RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
            catch
            {
                throw;
            }
        }
        
        public async Task<BidResponse> SetBidStatusAsync(int bidId, string status)
        {
            try
            {
                var request = new SetBidStatusRequest
                {
                    Id = bidId,
                    Status = status
                };

                var response = await bidClient.SetBidStatusAsync(request);
                return response;
            }
            catch (Grpc.Core.RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
        }

        // Notification operations
        public async Task<NotificationResponse> CreateNotificationAsync(int bidId, int propertyId, string message, string? status = null, int? userId = null, string? propertyTitle = null)
        {
            try
            {
                var request = new CreateNotificationRequest
                {
                    BidId = bidId,
                    PropertyId = propertyId,
                    Message = message
                };
                
                if (userId.HasValue)
                {
                    request.UserId = userId.Value;
                }
                
                if (!string.IsNullOrEmpty(status))
                {
                    request.Status = status;
                }
                
                if (!string.IsNullOrEmpty(propertyTitle))
                {
                    request.PropertyTitle = propertyTitle;
                }
                
                var response = await notificationClient.CreateNotificationAsync(request);
                return response;
            }
            catch (Grpc.Core.RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
            catch
            {
                throw;
            }
        }

        public async Task<GetNotificationsResponse> GetNotificationsAsync(int? userId = null, bool? isRead = null)
        {
            try
            {
                var request = new GetNotificationsRequest();
                
                if (userId.HasValue)
                {
                    request.UserId = userId.Value;
                }
                if (isRead.HasValue)
                {
                    request.IsRead = isRead.Value;
                }
                var response = await notificationClient.GetNotificationsAsync(request);
                return response;
            }
            catch (Grpc.Core.RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
            catch
            {
                throw;
            }
        }

        public async Task<NotificationResponse> GetNotificationAsync(int id)
        {
            try
            {
                var request = new GetNotificationRequest { Id = id };
                var response = await notificationClient.GetNotificationAsync(request);
                return response;
            }
            catch (Grpc.Core.RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
            catch
            {
                throw;
            }
        }

        public async Task<NotificationResponse> MarkNotificationAsReadAsync(int id)
        {
            try
            {
                var request = new MarkNotificationAsReadRequest { Id = id };
                var response = await notificationClient.MarkNotificationAsReadAsync(request);
                return response;
            }
            catch (Grpc.Core.RpcException ex)
            {
                throw new Exception($"gRPC error ({ex.StatusCode}): {ex.Status.Detail}", ex);
            }
            catch
            {
                throw;
            }
        }
        
        
    }
}