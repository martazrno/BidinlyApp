using CloudinaryDotNet;
using CloudinaryDotNet.Actions;
using Microsoft.AspNetCore.Mvc;
using MainServer.WebAPI.Services;
using MainServer.WebAPI.Protos;
using PropertyDto = Shared.DTOs.Properties.PropertyDto;
using Shared.DTOs.Properties;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Formats.Jpeg;
using SixLabors.ImageSharp.Processing;
using Size = SixLabors.ImageSharp.Size;

namespace MainServer.WebAPI.Controllers
{
    [ApiController]
    [Route("properties")]
    public class PropertiesController : ControllerBase
    {
        private readonly PropertyGrpcClient propertyClient;
        private readonly DataTierGrpcClient dataTierClient;

        public PropertiesController(PropertyGrpcClient propertyClient, DataTierGrpcClient dataTierClient)
        {
            this.propertyClient = propertyClient;
            this.dataTierClient = dataTierClient;
        }

        [HttpGet]
        public async Task<ActionResult<List<PropertyDto>>> GetManyProperties([FromQuery] string? status = null, [FromQuery] int? agentId = null, [FromQuery] string? creationStatus = null)
        {
            try
            {
                GetPropertiesResponse response = await propertyClient.GetPropertiesAsync(agentId, status, creationStatus);

                var uniqueAgentIds = response.Properties.Select(p => p.AgentId).Distinct().ToList();

                var agentTasks = uniqueAgentIds.Select(async id =>
                {
                    try
                    {
                        var user = await dataTierClient.GetUserAsync(id);
                        return new { Id = id, User = (UserResponse?)user };
                    }
                    catch
                    {
                        return new { Id = id, User = (UserResponse?)null };
                    }
                });

                var agentResults = await Task.WhenAll(agentTasks);
                var agentLookup = agentResults.ToDictionary(r => r.Id, r => r.User);

                List<PropertyDto> responseDtos = response.Properties.Select(propertyResponse =>
                {
                    return new PropertyDto
                    {
                        Id = propertyResponse.Id,
                        Title = propertyResponse.Title,
                        AgentId = propertyResponse.AgentId,
                        AgentName = agentLookup.GetValueOrDefault(propertyResponse.AgentId)?.Username,
                        Address = propertyResponse.Address,
                        StartingPrice = (decimal)propertyResponse.StartingPrice,
                        Bedrooms = propertyResponse.Bedrooms,
                        Bathrooms = propertyResponse.Bathrooms,
                        SizeInSquareMeters = propertyResponse.SizeInSquareMeters,
                        Description = propertyResponse.Description,
                        Status = propertyResponse.Status,
                        CreationStatus = propertyResponse.CreationStatus,
                        ImageUrl = propertyResponse.ImageUrl
                    };
                }).ToList();

                return Ok(responseDtos);
            }
            catch
            {
                throw;
            }
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<PropertyDto>> GetSingleProperty(int id)
        {
            try
            {
                PropertyResponse propertyResponse = await propertyClient.GetPropertyAsync(id);

                UserResponse? agentUser = null;
                try
                {
                    agentUser = await dataTierClient.GetUserAsync(propertyResponse.AgentId);
                }
                catch
                {
                }

                PropertyDto propertyDto = new PropertyDto
                {
                    Id = propertyResponse.Id,
                    Title = propertyResponse.Title,
                    AgentId = propertyResponse.AgentId,
                    AgentName = agentUser?.Username,
                    Address = propertyResponse.Address,
                    StartingPrice = (decimal)propertyResponse.StartingPrice,
                    Bedrooms = propertyResponse.Bedrooms,
                    Bathrooms = propertyResponse.Bathrooms,
                    SizeInSquareMeters = propertyResponse.SizeInSquareMeters,
                    Description = propertyResponse.Description,
                    Status = propertyResponse.Status,
                    CreationStatus = propertyResponse.CreationStatus,
                    ImageUrl = propertyResponse.ImageUrl
                };

                return Ok(propertyDto);
            }
            catch (Grpc.Core.RpcException ex) when (ex.StatusCode == Grpc.Core.StatusCode.NotFound)
            {
                return NotFound();
            }
            catch
            {
                throw;
            }
        }

        [HttpPost]
        public async Task<ActionResult<PropertyDto>> CreateProperty([FromBody] CreatePropertyDto createPropertyDto)
        {
            try
            {           
                CreatePropertyRequest createRequest = new CreatePropertyRequest
                {
                    Title = createPropertyDto.Title,
                    AgentId = createPropertyDto.AgentId,
                    StartingPrice = (double)createPropertyDto.StartingPrice,
                    Bedrooms = createPropertyDto.Bedrooms,
                    Bathrooms = createPropertyDto.Bathrooms,
                    SizeInSquareMeters = (int)createPropertyDto.SizeInSquareMeters,
                    Status = "Available",
                    CreationStatus = "Pending"
                };

                if (!string.IsNullOrWhiteSpace(createPropertyDto.Address))
                {
                    createRequest.Address = createPropertyDto.Address;
                }
                if (!string.IsNullOrWhiteSpace(createPropertyDto.Description))
                {
                    createRequest.Description = createPropertyDto.Description;
                }
                if (!string.IsNullOrWhiteSpace(createPropertyDto.ImageUrl))
                {
                    createRequest.ImageUrl = createPropertyDto.ImageUrl;
                }

                PropertyResponse propertyResponse = await propertyClient.CreatePropertyAsync(createRequest);

                PropertyDto createdPropertyDto = new PropertyDto
                {
                    Id = propertyResponse.Id,
                    Title = propertyResponse.Title,
                    AgentId = propertyResponse.AgentId,
                    AgentName = null,
                    Address = propertyResponse.Address,
                    StartingPrice = (decimal)propertyResponse.StartingPrice,
                    Bedrooms = propertyResponse.Bedrooms,
                    Bathrooms = propertyResponse.Bathrooms,
                    SizeInSquareMeters = propertyResponse.SizeInSquareMeters,
                    Description = propertyResponse.Description,
                    Status = propertyResponse.Status,
                    CreationStatus = propertyResponse.CreationStatus,
                    ImageUrl = propertyResponse.ImageUrl

                };

                return CreatedAtAction(nameof(GetSingleProperty), new { id = createdPropertyDto.Id }, createdPropertyDto);
            }
            catch
            {
                throw;
            }
        }
        
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteProperty(int id)
        {
            try
            {
                var success = await propertyClient.DeletePropertyAsync(id);

                if (!success)
                    return NotFound();

                return NoContent();
            }
            catch (Grpc.Core.RpcException ex) when (ex.StatusCode == Grpc.Core.StatusCode.NotFound)
            {
                return NotFound();
            }
            catch
            {
                throw;
            }
        }

        [HttpPut("{id}")]
        public async Task<ActionResult<PropertyDto>> UpdateProperty(int id, [FromBody] UpdatePropertyDto updatePropertyDto)
        {
            try
            {
                if (id != updatePropertyDto.Id)
                {
                    return BadRequest("Property ID mismatch");
                }

                var updateRequest = new UpdatePropertyRequest
                {
                    Id = updatePropertyDto.Id
                };

                if (!string.IsNullOrWhiteSpace(updatePropertyDto.Title))
                {
                    updateRequest.Title = updatePropertyDto.Title;
                }
                if (!string.IsNullOrWhiteSpace(updatePropertyDto.Address))
                {
                    updateRequest.Address = updatePropertyDto.Address;
                }
                if (updatePropertyDto.StartingPrice.HasValue)
                {
                    updateRequest.StartingPrice = (double)updatePropertyDto.StartingPrice.Value;
                }
                if (updatePropertyDto.Bedrooms.HasValue)
                {
                    updateRequest.Bedrooms = updatePropertyDto.Bedrooms.Value;
                }
                if (updatePropertyDto.Bathrooms.HasValue)
                {
                    updateRequest.Bathrooms = updatePropertyDto.Bathrooms.Value;
                }
                if (updatePropertyDto.SizeInSquareMeters.HasValue)
                {
                    updateRequest.SizeInSquareMeters = (int)updatePropertyDto.SizeInSquareMeters.Value;
                }
                if (!string.IsNullOrWhiteSpace(updatePropertyDto.Description))
                {
                    updateRequest.Description = updatePropertyDto.Description;
                }
                if (!string.IsNullOrWhiteSpace(updatePropertyDto.Status))
                {
                    updateRequest.Status = updatePropertyDto.Status;
                }
                if (!string.IsNullOrWhiteSpace(updatePropertyDto.CreationStatus))
                {
                    updateRequest.CreationStatus = updatePropertyDto.CreationStatus;
                }
                if (!string.IsNullOrWhiteSpace(updatePropertyDto.ImageUrl))
                {
                    updateRequest.ImageUrl = updatePropertyDto.ImageUrl;
                }

                PropertyResponse propertyResponse = await propertyClient.UpdatePropertyAsync(updateRequest);

                UserResponse? agentUser = null;
                try
                {
                    agentUser = await dataTierClient.GetUserAsync(propertyResponse.AgentId);
                }
                catch
                {
                }

                PropertyDto updatedPropertyDto = new PropertyDto
                {
                    Id = propertyResponse.Id,
                    Title = propertyResponse.Title,
                    AgentId = propertyResponse.AgentId,
                    AgentName = agentUser?.Username,
                    Address = propertyResponse.Address,
                    StartingPrice = (decimal)propertyResponse.StartingPrice,
                    Bedrooms = propertyResponse.Bedrooms,
                    Bathrooms = propertyResponse.Bathrooms,
                    SizeInSquareMeters = propertyResponse.SizeInSquareMeters,
                    Description = propertyResponse.Description,
                    Status = propertyResponse.Status,
                    CreationStatus = propertyResponse.CreationStatus,
                    ImageUrl = propertyResponse.ImageUrl
                };

                return Ok(updatedPropertyDto);
            }
            catch (Grpc.Core.RpcException ex) when (ex.StatusCode == Grpc.Core.StatusCode.NotFound)
            {
                return NotFound();
            }
            catch
            {
                throw;
            }
        }
        
        [HttpPost("upload")]
        [RequestSizeLimit(60_000_000)]
        public async Task<ActionResult<List<string>>> UploadImages(
            [FromServices] CloudinaryDotNet.Cloudinary cloudinary,
            List<IFormFile> files)
        {
            Console.WriteLine("=== UploadImages CALLED ===");
            var sw = System.Diagnostics.Stopwatch.StartNew();
            Console.WriteLine("=== UploadImages PARALLEL START ===");

            try
            {
                if (files == null || files.Count == 0)
                    return BadRequest("No files provided.");

                if (files.Count > 5)
                    return BadRequest("Maximum 5 images allowed.");

                var tasks = files.Select(async file =>
                {
                    var fileSw = System.Diagnostics.Stopwatch.StartNew();
                    var compressedStream = await CompressImageAsync(file);
                    var uploadParams = new ImageUploadParams
                    {
                        File = new FileDescription(file.FileName + ".jpg", compressedStream),
                        Folder = "bidinly-properties",
                        UseFilename = false,
                        UniqueFilename = true,
                        Overwrite = false
                    };

                    var result = await cloudinary.UploadAsync(uploadParams);

                    fileSw.Stop();
                    Console.WriteLine($"(Parallel) Finished {file.FileName} in {fileSw.ElapsedMilliseconds} ms");

                    return result.SecureUrl?.ToString();
                });

                var urls = await Task.WhenAll(tasks);

                sw.Stop();
                Console.WriteLine($"=== TOTAL PARALLEL TIME: {sw.ElapsedMilliseconds} ms ===");

                return Ok(urls);
            }
            catch (Exception ex)
            {
                sw.Stop();
                Console.WriteLine($" Parallel Upload FAILED after {sw.ElapsedMilliseconds} ms: {ex.Message}");
                return StatusCode(500, $"Upload failed: {ex.Message}");
            }
        }
        
        private async Task<Stream> CompressImageAsync(IFormFile file)
        {
            using var inputStream = file.OpenReadStream();
            using var image = await Image.LoadAsync(inputStream);

            int maxWidth = 1920;
            if (image.Width > maxWidth)
            {
                image.Mutate(x => x.Resize(new ResizeOptions
                {
                    Mode = ResizeMode.Max,
                    Size = new Size(maxWidth, 0)
                }));
            }

            var output = new MemoryStream();
            var encoder = new JpegEncoder
            {
                Quality = 80
            };

            await image.SaveAsJpegAsync(output, encoder);
            output.Position = 0;

            return output;
        }
    }
}
