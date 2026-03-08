using System.ComponentModel.DataAnnotations;

namespace Shared.DTOs.Properties
{
    public class UpdatePropertyDto
    {
        
        [Required]
        public int Id { get; set; }//identification: required for an update operation
        
        
        [Required]
        public int AgentId { get; set; }//to verify the agent owns the property

        [StringLength(2000, MinimumLength = 1)]
        public string? Title { get; set; }
        
        public string? Address { get; set; }

        public decimal? StartingPrice { get; set; } 

        public int? Bedrooms { get; set; } 
        public int? Bathrooms { get; set; } 
        public double? SizeInSquareMeters { get; set; }

        [StringLength(5000)]
        public string? Description { get; set; }

        public string? Status { get; set; }

        public string? CreationStatus { get; set; }

        public string? ImageUrl { get; set; }

    }
}
