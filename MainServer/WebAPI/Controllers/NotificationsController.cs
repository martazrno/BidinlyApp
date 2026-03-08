using Microsoft.AspNetCore.Mvc;
using MainServer.WebAPI.Services;
using MainServer.WebAPI.Protos;
using NotificationDto = Shared.DTOs.Notifications.NotificationDto;
using CreateNotificationDto = Shared.DTOs.Notifications.CreateNotificationDto;

namespace MainServer.WebAPI.Controllers
{
    [ApiController]
    [Route("notifications")]
    public class NotificationsController : ControllerBase
    {
        private readonly DataTierGrpcClient dataTierClient;

        public NotificationsController(DataTierGrpcClient dataTierClient)
        {
            this.dataTierClient = dataTierClient;
        }

        [HttpPost]
        public async Task<ActionResult<NotificationDto>> CreateNotification([FromBody] CreateNotificationDto dto)
        {
            try
            {
                NotificationResponse response = await dataTierClient.CreateNotificationAsync(
                    bidId: dto.BidId,
                    userId: dto.UserId,
                    propertyId: dto.PropertyId,
                    status: dto.Status,
                    message: dto.Message,
                    propertyTitle: dto.PropertyTitle
                );

                DateTime createdAt = DateTimeOffset.FromUnixTimeSeconds(response.CreatedAtSeconds).DateTime;

                var notificationDto = new NotificationDto
                {
                    Id = response.Id,
                    BidId = response.BidId,
                    UserId = response.UserId,
                    PropertyId = response.PropertyId,
                    Status = response.HasStatus ? response.Status : null,
                    Message = response.Message,
                    PropertyTitle = response.HasPropertyTitle ? response.PropertyTitle : null,
                    CreatedAt = createdAt,
                    IsRead = response.IsRead
                };

                return CreatedAtAction(nameof(GetNotification), new { id = notificationDto.Id }, notificationDto);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = ex.Message });
            }
        }

        [HttpGet]
        public async Task<ActionResult<List<NotificationDto>>> GetNotifications([FromQuery] int? userId, [FromQuery] bool? isRead)
        {
            try
            {
                GetNotificationsResponse response = await dataTierClient.GetNotificationsAsync(userId, isRead);

                List<NotificationDto> notificationDtos = response.Notifications.Select(notificationResponse =>
                {
                    DateTime createdAt = DateTimeOffset.FromUnixTimeSeconds(notificationResponse.CreatedAtSeconds).DateTime;

                    return new NotificationDto
                    {
                        Id = notificationResponse.Id,
                        BidId = notificationResponse.BidId,
                        UserId = notificationResponse.UserId,
                        PropertyId = notificationResponse.PropertyId,
                        Status = notificationResponse.HasStatus ? notificationResponse.Status : null,
                        Message = notificationResponse.Message,
                        PropertyTitle = notificationResponse.HasPropertyTitle ? notificationResponse.PropertyTitle : null,
                        CreatedAt = createdAt,
                        IsRead = notificationResponse.IsRead
                    };
                }).ToList();

                return Ok(notificationDtos);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = ex.Message });
            }
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<NotificationDto>> GetNotification(int id)
        {
            try
            {
                NotificationResponse response = await dataTierClient.GetNotificationAsync(id);

                DateTime createdAt = DateTimeOffset.FromUnixTimeSeconds(response.CreatedAtSeconds).DateTime;

                var notificationDto = new NotificationDto
                {
                    Id = response.Id,
                    BidId = response.BidId,
                    UserId = response.UserId,
                    PropertyId = response.PropertyId,
                    Status = response.HasStatus ? response.Status : null,
                    Message = response.Message,
                    PropertyTitle = response.HasPropertyTitle ? response.PropertyTitle : null,
                    CreatedAt = createdAt,
                    IsRead = response.IsRead
                };

                return Ok(notificationDto);
            }
            catch (Grpc.Core.RpcException ex) when (ex.StatusCode == Grpc.Core.StatusCode.NotFound)
            {
                return NotFound(new { message = ex.Status.Detail });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = ex.Message });
            }
        }

        [HttpPut("{id}/read")]
        public async Task<ActionResult<NotificationDto>> MarkAsRead(int id)
        {
            try
            {
                NotificationResponse response = await dataTierClient.MarkNotificationAsReadAsync(id);

                DateTime createdAt = DateTimeOffset.FromUnixTimeSeconds(response.CreatedAtSeconds).DateTime;

                var notificationDto = new NotificationDto
                {
                    Id = response.Id,
                    BidId = response.BidId,
                    UserId = response.UserId,
                    PropertyId = response.PropertyId,
                    Status = response.HasStatus ? response.Status : null,
                    Message = response.Message,
                    PropertyTitle = response.HasPropertyTitle ? response.PropertyTitle : null,
                    CreatedAt = createdAt,
                    IsRead = response.IsRead
                };

                return Ok(notificationDto);
            }
            catch (Grpc.Core.RpcException ex) when (ex.StatusCode == Grpc.Core.StatusCode.NotFound)
            {
                return NotFound(new { message = ex.Status.Detail });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = ex.Message });
            }
        }
    }
}

