using Microsoft.AspNetCore.Mvc;
using Shared.DTOs.Payments;
using MainServer.WebAPI.Services;

namespace MainServer.WebAPI.Controllers
{
    [ApiController]
    [Route("payments")]
    public class PaymentController : ControllerBase
    {
        private readonly PaymentGrpcClient paymentClient;

        public PaymentController(PaymentGrpcClient paymentClient)
        {
            this.paymentClient = paymentClient;
        }

        [HttpPost("validate-card")]
        public async Task<ActionResult<ValidateCardResponseDto>> ValidateCard([FromBody] ValidateCardDto validateCardDto)
        {
            try
            {
                if (string.IsNullOrWhiteSpace(validateCardDto.CardNumber))
                {
                    return BadRequest(new { message = "Card number is required" });
                }

                if (string.IsNullOrWhiteSpace(validateCardDto.ExpirationDate))
                {
                    return BadRequest(new { message = "Expiration date is required" });
                }

                if (string.IsNullOrWhiteSpace(validateCardDto.Cvc))
                {
                    return BadRequest(new { message = "CVC is required" });
                }

                if (string.IsNullOrWhiteSpace(validateCardDto.Name))
                {
                    return BadRequest(new { message = "Name is required" });
                }

                var response = await paymentClient.ValidateCardAsync(
                    validateCardDto.CardNumber,
                    validateCardDto.ExpirationDate,
                    validateCardDto.Cvc,
                    validateCardDto.Name,
                    validateCardDto.PropertyId,
                    validateCardDto.BidId,
                    validateCardDto.BuyerId,
                    validateCardDto.AgentId
                );

                var responseDto = new ValidateCardResponseDto
                {
                    IsValid = response.IsValid,  
                    Message = response.Message
                };

                return Ok(responseDto);
            }
            catch (Exception ex) when (ex.InnerException is Grpc.Core.RpcException rpcEx && rpcEx.StatusCode == Grpc.Core.StatusCode.InvalidArgument)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = ex.Message });
            }
        }
    }
}

