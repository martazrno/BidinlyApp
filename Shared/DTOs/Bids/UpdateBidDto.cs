using Shared.DTOs.Bids;
using System.ComponentModel.DataAnnotations;

namespace Shared.DTOs.Bids
{
    public class UpdateBidDto
    {
        [Required]
        public int Id { get; set; }

        
        [Required]
        public int BuyerId { get; set; } //authorization checks, buyer only updates their own bid)

        public decimal? Amount { get; set; }//handle in service layer: must be higher than current

        public DateTime? ExpiryDate { get; set; }//should it be modifiable?
        
        public string? Deal { get; set; }

    }
}
