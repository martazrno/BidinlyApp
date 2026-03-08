using System.ComponentModel.DataAnnotations;

namespace Shared.DTOs.Properties
{
   
    public class CreatePropertyDto
    {

        [Required] 
        public int AgentId { get; set; } //links to the agent creating the property

        [Required]
        [StringLength(2000, MinimumLength = 1)]
        public required string Title { get; set; }
        
        [Required]
        public string? Address { get; set; }

        [Required]
        public decimal StartingPrice { get; set; }

        [Required]
        public int Bedrooms { get; set; }

        [Required]
        public int Bathrooms { get; set; }

        [Required]
        public double SizeInSquareMeters { get; set; }

        public string? Description { get; set; }

        public string? ImageUrl { get; set; }

    }
}
