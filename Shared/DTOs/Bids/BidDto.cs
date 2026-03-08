namespace Shared.DTOs.Bids
{
    public class BidDto
    {
        public int Id { get; set; }
        
        public int PropertyId { get; set; }
        public int BuyerId { get; set; }

        public string? BuyerUsername { get; set; }//enrichment
        public string? PropertyTitle { get; set; }//erichment

        public decimal Amount { get; set; }

        public DateTime ExpiryDate { get; set; } 

        public string? Status { get; set; } // e.g., "Pending", "Accepted", "Rejected", "Expired"
        
        public string? Deal { get; set; }
        
        public bool SignatureValid { get; set; }
    }
}

