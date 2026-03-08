using System.ComponentModel.DataAnnotations;

namespace Shared.DTOs.Payments
{
    public class ValidateCardDto
    {
        [Required]
        public string CardNumber { get; set; } = string.Empty;

        [Required]
        public string ExpirationDate { get; set; } = string.Empty;

        [Required]
        public string Cvc { get; set; } = string.Empty;

        [Required]
        public string Name { get; set; } = string.Empty;

        public int? PropertyId { get; set; }
        public int? BidId { get; set; }
        public int? BuyerId { get; set; }
        public int? AgentId { get; set; }
    }
}

