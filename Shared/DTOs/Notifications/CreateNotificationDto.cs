using System.ComponentModel.DataAnnotations;

namespace Shared.DTOs.Notifications
{
    public class CreateNotificationDto
    {
        [Required]
        public int BidId { get; set; }

        public int? UserId { get; set; }

        [Required]
        public int PropertyId { get; set; }

        public string? Status { get; set; }

        [Required]
        public string Message { get; set; } = string.Empty;

        public string? PropertyTitle { get; set; }
        
    }
}

