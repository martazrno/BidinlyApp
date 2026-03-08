using Microsoft.AspNetCore.Mvc;
using Shared.DTOs.Users;
using MainServer.WebAPI.Services;
using MainServer.WebAPI.Protos;

namespace MainServer.WebAPI.Controllers
{
    [ApiController]
    [Route("users")]
    public class UsersController : ControllerBase
    {
        private readonly DataTierGrpcClient dataTierClient;

        public UsersController(DataTierGrpcClient dataTierClient)
        {
            this.dataTierClient = dataTierClient;
        }

        [HttpGet]
        public async Task<ActionResult<List<UserDto>>> GetUsers()
        {
            try
            {
                var usersResponse = await dataTierClient.GetUsersAsync();
                var userDtos = new List<UserDto>();

                foreach (var userResponse in usersResponse.Users)
                {
                    string? roleName = null;
                    if (userResponse.RoleId > 0)
                    {
                        try
                        {
                            var roleResponse = await dataTierClient.GetRoleAsync(userResponse.RoleId);
                            roleName = roleResponse.RoleName;
                        }
                        catch
                        {
                        }
                    }

                    userDtos.Add(new UserDto
                    {
                        Id = userResponse.Id,
                        Username = userResponse.Username,
                        RoleName = roleName,
                        IsActive = userResponse.IsActive,
                        Email = userResponse.HasEmail ? userResponse.Email : null,
                        PublicKey = userResponse.PublicKey
                    });
                }

                return Ok(userDtos);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = ex.Message });
            }
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<UserDto>> GetUser(int id)
        {
            try
            {
                var userResponse = await dataTierClient.GetUserAsync(id);

                string? roleName = null;
                if (userResponse.RoleId > 0)
                {
                    try
                    {
                        var roleResponse = await dataTierClient.GetRoleAsync(userResponse.RoleId);
                        roleName = roleResponse.RoleName;
                    }
                    catch
                    {
                        // If role lookup fails, roleName remains null
                    }
                }

                var userDto = new UserDto
                {
                    Id = userResponse.Id,
                    Username = userResponse.Username,
                    RoleName = roleName,
                    IsActive = userResponse.IsActive,
                    Email = userResponse.HasEmail ? userResponse.Email : null,
                    PublicKey = userResponse.PublicKey
                };

                return Ok(userDto);
            }
            catch (Exception ex) when (ex.InnerException is Grpc.Core.RpcException rpcEx && rpcEx.StatusCode == Grpc.Core.StatusCode.NotFound)
            {
                return NotFound(new { message = $"User with id {id} not found" });
            }
            catch (Exception ex) when (ex.Message.Contains("NOT_FOUND") || ex.Message.Contains("not found"))
            {
                return NotFound(new { message = $"User with id {id} not found" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = ex.Message });
            }
        }

        [HttpPost]
        public async Task<ActionResult<UserDto>> CreateUser([FromBody] CreateUserDto createUserDto)
        {
            try
            {
                if (string.IsNullOrWhiteSpace(createUserDto.Username))
                {
                    return BadRequest(new { message = "Username is required" });
                }

                if (string.IsNullOrWhiteSpace(createUserDto.Password))
                {
                    return BadRequest(new { message = "Password is required" });
                }

                UserResponse userResponse = await dataTierClient.CreateUserAsync(
                    createUserDto.Username,
                    createUserDto.Password,
                    createUserDto.RoleId,
                    createUserDto.Email
                );

                string? roleName = null;
                if (userResponse.RoleId > 0)
                {
                   
                        GetRoleResponse roleResponse = await dataTierClient.GetRoleAsync(userResponse.RoleId);
                        roleName = roleResponse.RoleName;
                    
                }

                UserDto userDto = new UserDto
                {
                    Id = userResponse.Id,
                    Username = userResponse.Username,
                    RoleName = roleName,
                    IsActive = userResponse.IsActive,
                    Email = userResponse.HasEmail ? userResponse.Email : null,
                    PublicKey = userResponse.PublicKey
                };

                return Created($"/users/{userDto.Id}", userDto);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpDelete("{id}")]
        public async Task<ActionResult> DeleteUser(int id)
        {
            try
            {
                var success = await dataTierClient.DeleteUserAsync(id);
                if (success)
                {
                    return NoContent();
                }
                else
                {
                    return NotFound(new { message = $"User with id {id} not found" });
                }
            }
            catch (Exception ex) when (ex.InnerException is Grpc.Core.RpcException rpcEx && rpcEx.StatusCode == Grpc.Core.StatusCode.NotFound)
            {
                return NotFound(new { message = $"User with id {id} not found" });
            }
            catch (Exception ex) when (ex.Message.Contains("NOT_FOUND") || ex.Message.Contains("not found"))
            {
                return NotFound(new { message = $"User with id {id} not found" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = ex.Message });
            }
        }

        [HttpPatch("{id}/toggle-active")]
        public async Task<ActionResult<UserDto>> ToggleUserActive(int id)
        {
            try
            {
                var userResponse = await dataTierClient.GetUserAsync(id);
                var updatedUserResponse = await dataTierClient.UpdateUserAsync(id, null, null, null, !userResponse.IsActive);
                
                string? roleName = null;
                if (updatedUserResponse.RoleId > 0)
                {
                    try
                    {
                        var roleResponse = await dataTierClient.GetRoleAsync(updatedUserResponse.RoleId);
                        roleName = roleResponse.RoleName;
                    }
                    catch
                    {
                        // If role lookup fails, roleName remains null
                    }
                }

                var userDto = new UserDto
                {
                    Id = updatedUserResponse.Id,
                    Username = updatedUserResponse.Username,
                    RoleName = roleName,
                    IsActive = updatedUserResponse.IsActive,
                    Email = updatedUserResponse.HasEmail ? updatedUserResponse.Email : null,
                    PublicKey = updatedUserResponse.PublicKey
                };

                return Ok(userDto);
            }
            catch (Exception ex) when (ex.InnerException is Grpc.Core.RpcException rpcEx && rpcEx.StatusCode == Grpc.Core.StatusCode.NotFound)
            {
                return NotFound(new { message = $"User with id {id} not found" });
            }
            catch (Exception ex) when (ex.Message.Contains("NOT_FOUND") || ex.Message.Contains("not found"))
            {
                return NotFound(new { message = $"User with id {id} not found" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = ex.Message });
            }
        }
    }
}

