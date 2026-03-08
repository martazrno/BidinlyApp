namespace Shared.DTOs.Properties
{
    public class PropertyDto
    {
        public int Id { get; set; }
        public string? Title { get; set; }
        public int? AgentId { get; set; }
        public string? AgentName { get; set; } //not displaying agentId
        public string? Address { get; set; }
        public decimal StartingPrice { get; set; }
        public int Bedrooms { get; set; }
        public int Bathrooms { get; set; }
        public double SizeInSquareMeters { get; set; }
        public string? Description { get; set; }
        public string? Status { get; set; } // "Available", "Sold"

        public string? CreationStatus { get; set; } // "Pending", "Approved", "Rejected"

        public string? ImageUrl { get; set; } 
        
    }
}

