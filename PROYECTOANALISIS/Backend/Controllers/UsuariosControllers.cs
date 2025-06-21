using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using BackendVentas.Models;
using Microsoft.AspNetCore.Authorization;
using BackendVentas.Utils;

namespace BackendVentas.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]")]
    public class UsuariosController : ControllerBase
    {
        private readonly IConfiguration _configuration;

        public UsuariosController(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        // GET: api/usuarios
        [HttpGet]
        public IActionResult Get()
        {
            try
            {
                List<Usuario> usuarios = new List<Usuario>();
                using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
                conn.Open();
                string query = "SELECT * FROM Usuario";
                using SqlCommand cmd = new SqlCommand(query, conn);
                using SqlDataReader reader = cmd.ExecuteReader();
                while (reader.Read())
                {
                    usuarios.Add(new Usuario
                    {
                        Id = (int)reader["Id"],
                        Nombre_Usuario = reader["Nombre_Usuario"]?.ToString() ?? string.Empty,
                        Contrasena = reader["Contrasena"]?.ToString() ?? string.Empty,
                        Rol = reader["Rol"]?.ToString() ?? string.Empty
                    });
                }
                return Ok(usuarios);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al obtener los usuarios", detalle = ex.Message });
            }
        }

        // GET: api/usuarios/{id}
        [HttpGet("{id}")]
        public IActionResult Get(int id)
        {
            try
            {
                using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
                conn.Open();
                string query = "SELECT * FROM Usuario WHERE Id = @Id";
                using SqlCommand cmd = new SqlCommand(query, conn);
                cmd.Parameters.AddWithValue("@Id", id);
                using SqlDataReader reader = cmd.ExecuteReader();
                if (reader.Read())
                {
                    var usuario = new Usuario
                    {
                        Id = (int)reader["Id"],
                        Nombre_Usuario = reader["Nombre_Usuario"]?.ToString() ?? string.Empty,
                        Contrasena = reader["Contrasena"]?.ToString() ?? string.Empty,
                        Rol = reader["Rol"]?.ToString() ?? string.Empty
                    };
                    return Ok(usuario);
                }
                else
                {
                    return NotFound(new { mensaje = "Usuario no encontrado." });
                }
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al obtener el usuario", detalle = ex.Message });
            }
        }

        // POST: api/usuarios
        [HttpPost]
        public IActionResult Post([FromBody] Usuario usuario)
        {
            try
            {
                using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
                conn.Open();

                // Validar duplicado de nombre de usuario
                string checkQuery = "SELECT COUNT(*) FROM Usuario WHERE Nombre_Usuario = @Nombre_Usuario";
                using SqlCommand checkCmd = new SqlCommand(checkQuery, conn);
                checkCmd.Parameters.AddWithValue("@Nombre_Usuario", usuario.Nombre_Usuario);
                int count = (int)checkCmd.ExecuteScalar();
                if (count > 0)
                {
                    return Conflict(new { mensaje = "El nombre de usuario ya existe." });
                }

                // Hashear la contraseña antes de guardar
                usuario.Contrasena = PasswordHasher.HashPassword(usuario.Contrasena);

                string insertQuery = @"INSERT INTO Usuario (Nombre_Usuario, Contrasena, Rol) 
                                      VALUES (@Nombre_Usuario, @Contrasena, @Rol)";
                using SqlCommand cmd = new SqlCommand(insertQuery, conn);
                cmd.Parameters.AddWithValue("@Nombre_Usuario", usuario.Nombre_Usuario);
                cmd.Parameters.AddWithValue("@Contrasena", usuario.Contrasena);
                cmd.Parameters.AddWithValue("@Rol", usuario.Rol);

                int filas = cmd.ExecuteNonQuery();
                return filas > 0
                    ? Ok(new { mensaje = "Usuario creado correctamente." })
                    : BadRequest(new { mensaje = "No se pudo crear el usuario." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al crear el usuario", detalle = ex.Message });
            }
        }

        // PUT: api/usuarios/{id}
        [HttpPut("{id}")]
        public IActionResult Put(int id, [FromBody] Usuario usuario)
        {
            try
            {
                using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
                conn.Open();

                // Si la contraseña no está vacía, hashearla
                if (!string.IsNullOrWhiteSpace(usuario.Contrasena))
                {
                    usuario.Contrasena = PasswordHasher.HashPassword(usuario.Contrasena);
                }

                string updateQuery = @"UPDATE Usuario SET 
                                        Nombre_Usuario = @Nombre_Usuario, 
                                        Contrasena = @Contrasena, 
                                        Rol = @Rol 
                                      WHERE Id = @Id";
                using SqlCommand cmd = new SqlCommand(updateQuery, conn);
                cmd.Parameters.AddWithValue("@Id", id);
                cmd.Parameters.AddWithValue("@Nombre_Usuario", usuario.Nombre_Usuario);
                cmd.Parameters.AddWithValue("@Contrasena", usuario.Contrasena);
                cmd.Parameters.AddWithValue("@Rol", usuario.Rol);

                int filas = cmd.ExecuteNonQuery();
                return filas > 0
                    ? Ok(new { mensaje = "Usuario actualizado correctamente." })
                    : NotFound(new { mensaje = "Usuario no encontrado." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al actualizar el usuario", detalle = ex.Message });
            }
        }

        // DELETE: api/usuarios/{id}
        [HttpDelete("{id}")]
        public IActionResult Delete(int id)
        {
            try
            {
                using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
                conn.Open();

                string deleteQuery = "DELETE FROM Usuario WHERE Id = @Id";
                using SqlCommand cmd = new SqlCommand(deleteQuery, conn);
                cmd.Parameters.AddWithValue("@Id", id);

                int filas = cmd.ExecuteNonQuery();
                return filas > 0
                    ? Ok(new { mensaje = "Usuario eliminado correctamente." })
                    : NotFound(new { mensaje = "Usuario no encontrado." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al eliminar el usuario", detalle = ex.Message });
            }
        }
        // POST: api/usuarios/login
        [AllowAnonymous]
        [HttpPost("login")]
        public IActionResult Login([FromBody] Login login)
        {
            try
            {
                using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
                conn.Open();

                string query = "SELECT * FROM Usuario WHERE Nombre_Usuario = @Nombre_Usuario";
                using SqlCommand cmd = new SqlCommand(query, conn);
                cmd.Parameters.AddWithValue("@Nombre_Usuario", login.Nombre_Usuario);

                using SqlDataReader reader = cmd.ExecuteReader();
                if (reader.Read())
                {
                    var usuario = new Usuario
                    {
                        Id = (int)reader["Id"],
                        Nombre_Usuario = reader["Nombre_Usuario"]?.ToString() ?? string.Empty,
                        Contrasena = reader["Contrasena"]?.ToString() ?? string.Empty,
                        Rol = reader["Rol"]?.ToString() ?? string.Empty
                    };
                    // Verificar la contraseña hasheada
                    if (PasswordHasher.VerifyPassword(login.Contrasena, usuario.Contrasena))
                    {
                        usuario.Contrasena = string.Empty; // No devolver el hash
                        return Ok(usuario);
                    }
                }
                return Unauthorized(new { mensaje = "Credenciales inválidas." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al iniciar sesión", detalle = ex.Message });
            }
        }
        // POST: api/usuarios/hash
        [AllowAnonymous]
        [HttpPost("hash")]
        public IActionResult HashPassword([FromBody] string password)
        {
            if (string.IsNullOrWhiteSpace(password))
                return BadRequest(new { mensaje = "La contraseña no puede estar vacía." });

            var hashed = BackendVentas.Utils.PasswordHasher.HashPassword(password);
            return Ok(new { hashedPassword = hashed });
        }
        // POST: api/usuarios/actualizar-contrasenas
        [HttpPost("actualizar-contrasenas")]
        public IActionResult ActualizarContrasenas()
        {
            try
            {
                using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
                conn.Open();
                string selectQuery = "SELECT Id, Contrasena FROM Usuario";
                using SqlCommand selectCmd = new SqlCommand(selectQuery, conn);
                using SqlDataReader reader = selectCmd.ExecuteReader();
                var usuarios = new List<(int Id, string Contrasena)>();
                while (reader.Read())
                {
                    var id = (int)reader["Id"];
                    var contrasena = reader["Contrasena"]?.ToString() ?? string.Empty;
                    usuarios.Add((id, contrasena));
                }
                reader.Close();
                foreach (var usuario in usuarios)
                {
                    // Si la contraseña no parece hasheada (por ejemplo, longitud menor a 30)
                    if (!string.IsNullOrEmpty(usuario.Contrasena) && usuario.Contrasena.Length < 30)
                    {
                        string hashed = PasswordHasher.HashPassword(usuario.Contrasena);
                        string updateQuery = "UPDATE Usuario SET Contrasena = @Contrasena WHERE Id = @Id";
                        using SqlCommand updateCmd = new SqlCommand(updateQuery, conn);
                        updateCmd.Parameters.AddWithValue("@Contrasena", hashed);
                        updateCmd.Parameters.AddWithValue("@Id", usuario.Id);
                        updateCmd.ExecuteNonQuery();
                    }
                }
                return Ok(new { mensaje = "Contraseñas actualizadas correctamente." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al actualizar contraseñas", detalle = ex.Message });
            }
        }
    }
}
