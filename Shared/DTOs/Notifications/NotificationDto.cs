namespace Shared.DTOs.Notifications
{
    public class NotificationDto
    {
        public int Id { get; set; }
        
        public int BidId { get; set; }
        
        public int? UserId { get; set; }
        
        public int PropertyId { get; set; }
        
        public string? Status { get; set; }
        
        public string Message { get; set; } = string.Empty;
        
        public string? PropertyTitle { get; set; }
        
        public DateTime CreatedAt { get; set; }
        
        public bool IsRead { get; set; }
    }
}

