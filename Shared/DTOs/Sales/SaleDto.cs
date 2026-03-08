namespace Shared.DTOs.Sales
{
    public class SaleDto
    {
        public int Id { get; set; }

        public DateTime TimeOfSale { get; set; }
        
        public int PropertyId { get; set; }

        public string? PropertyTitle { get; set; }//enrichment
        
        public string? BuyerUsername { get; set; }//enrichment

        public string? AgentUsername { get; set; }//enrichment
        
        public decimal FinalAmount { get; set; }//enrichment
        
        public int WinningBidId { get; set; } 
    }
}

