
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace BackendVentas.Controllers
{
    
    [Route("api/[controller]")]
    [ApiController]
    public class AuthController : ControllerBase
    {
        [HttpPost("login")]
        public IActionResult Login([FromBody] LoginModel login)
        {
            if (login.Usuario == "admin" && login.Contraseña == "1234")
            {
                var claims = new[]
                {
                    new Claim(ClaimTypes.Name, login.Usuario)
                };

                var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes("esta_es_mi_clave_secreta_muy_segura_1234"));
                var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

                var token = new JwtSecurityToken(
                    claims: claims,
                    expires: DateTime.Now.AddSeconds(30),
                    signingCredentials: creds);

                return Ok(new { token = new JwtSecurityTokenHandler().WriteToken(token) });
            }

            return Unauthorized();
        }

        public class LoginModel
        {
            public string? Usuario { get; set; }
            public string? Contraseña { get; set; }
        }
    }
}

