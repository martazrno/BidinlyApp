using System.ComponentModel.DataAnnotations;

namespace Shared.DTOs.Bids
{
    public class CreateBidDto
    {
        [Required]
        public int BuyerId { get; set; }

        [Required]
        public int PropertyId { get; set; }

        [Required]
        public decimal Amount { get; set; }

        [Required]
        public DateTime ExpiryDate { get; set; }
        
        public string? Deal { get; set; }

        // bid_id and status are omitted as they are server-generated/managed.
    }
}
