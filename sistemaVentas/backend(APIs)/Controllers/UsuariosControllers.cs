using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using BackendVentas.Models;

namespace BackendVentas.Controllers
{
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
            List<Usuario> usuarios = new();
            using SqlConnection conn = new(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            string query = "SELECT * FROM Usuario";
            using SqlCommand cmd = new(query, conn);
            using SqlDataReader reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                usuarios.Add(new Usuario
                {
                    Id = (int)reader["id"],
                    Nombre_Usuario = reader["nombre_usuario"].ToString() ?? "",
                    Contrasena = reader["contrasena"].ToString() ?? ""
                });
            }

            return Ok(usuarios);
        }

        // GET: api/usuarios/5
        [HttpGet("{id}")]
        public IActionResult GetById(int id)
        {
            using SqlConnection conn = new(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            string query = "SELECT * FROM Usuario WHERE id = @id";
            using SqlCommand cmd = new(query, conn);
            cmd.Parameters.AddWithValue("@id", id);
            using SqlDataReader reader = cmd.ExecuteReader();
            if (reader.Read())
            {
                var usuario = new Usuario
                {
                    Id = (int)reader["id"],
                    Nombre_Usuario = reader["nombre_usuario"].ToString() ?? "",
                    Contrasena = reader["contrasena"].ToString() ?? ""
                };
                return Ok(usuario);
            }

            return NotFound(new { mensaje = "Usuario no encontrado." });
        }

        // POST: api/usuarios
        [HttpPost]
        public IActionResult Post([FromBody] Usuario usuario)
        {
            using SqlConnection conn = new(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            // Validar duplicado
            string check = "SELECT COUNT(*) FROM Usuario WHERE nombre_usuario = @nombre";
            using SqlCommand checkCmd = new(check, conn);
            checkCmd.Parameters.AddWithValue("@nombre", usuario.Nombre_Usuario);
            int count = (int)checkCmd.ExecuteScalar();
            if (count > 0)
                return Conflict(new { mensaje = "El nombre de usuario ya existe." });

            string insert = "INSERT INTO Usuario (nombre_usuario, contrasena) VALUES (@nombre, @contrasena)";
            using SqlCommand cmd = new(insert, conn);
            cmd.Parameters.AddWithValue("@nombre", usuario.Nombre_Usuario);
            cmd.Parameters.AddWithValue("@contrasena", usuario.Contrasena);

            int filas = cmd.ExecuteNonQuery();
            return filas > 0
                ? Ok(new { mensaje = "Usuario creado correctamente." })
                : BadRequest(new { mensaje = "Error al crear usuario." });
        }

        // PUT: api/usuarios/5
        [HttpPut("{id}")]
        public IActionResult Put(int id, [FromBody] Usuario usuario)
        {
            using SqlConnection conn = new(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            string update = "UPDATE Usuario SET nombre_usuario = @nombre, contrasena = @contrasena WHERE id = @id";
            using SqlCommand cmd = new(update, conn);
            cmd.Parameters.AddWithValue("@id", id);
            cmd.Parameters.AddWithValue("@nombre", usuario.Nombre_Usuario);
            cmd.Parameters.AddWithValue("@contrasena", usuario.Contrasena);

            int filas = cmd.ExecuteNonQuery();
            return filas > 0
                ? Ok(new { mensaje = "Usuario actualizado correctamente." })
                : NotFound(new { mensaje = "Usuario no encontrado." });
        }

        // DELETE: api/usuarios/5
        [HttpDelete("{id}")]
        public IActionResult Delete(int id)
        {
            using SqlConnection conn = new(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            string delete = "DELETE FROM Usuario WHERE id = @id";
            using SqlCommand cmd = new(delete, conn);
            cmd.Parameters.AddWithValue("@id", id);

            int filas = cmd.ExecuteNonQuery();
            return filas > 0
                ? Ok(new { mensaje = "Usuario eliminado correctamente." })
                : NotFound(new { mensaje = "Usuario no encontrado." });
        }
    }
}
