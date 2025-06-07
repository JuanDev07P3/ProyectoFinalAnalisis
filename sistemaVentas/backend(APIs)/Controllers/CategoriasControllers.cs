using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using BackendVentas.Models;
using Microsoft.AspNetCore.Authorization;

namespace BackendVentas.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]")]
    public class CategoriasController : ControllerBase
    {
        private readonly IConfiguration _configuration;

        public CategoriasController(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        // GET: api/categorias
        [HttpGet]
        public IActionResult Get()
        {
            List<Categoria> categorias = new();

            using SqlConnection conn = new(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            string query = "SELECT * FROM Categorias";
            using SqlCommand cmd = new(query, conn);
            using SqlDataReader reader = cmd.ExecuteReader();

            while (reader.Read())
            {
                categorias.Add(new Categoria
                {
                    Id = (int)reader["id"],
                    Nombre = reader["nombre"].ToString() ?? "",
                    Descripcion = reader["descripcion"]?.ToString()
                });
            }

            return Ok(categorias);
        }

        // GET: api/categorias/5
        [HttpGet("{id}")]
        public IActionResult GetById(int id)
        {
            using SqlConnection conn = new(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            string query = "SELECT * FROM Categorias WHERE id = @id";
            using SqlCommand cmd = new(query, conn);
            cmd.Parameters.AddWithValue("@id", id);
            using SqlDataReader reader = cmd.ExecuteReader();

            if (reader.Read())
            {
                var categoria = new Categoria
                {
                    Id = (int)reader["id"],
                    Nombre = reader["nombre"].ToString() ?? "",
                    Descripcion = reader["descripcion"]?.ToString()
                };
                return Ok(categoria);
            }

            return NotFound(new { mensaje = "Categoría no encontrada." });
        }

        // POST: api/categorias
        [HttpPost]
        public IActionResult Post([FromBody] Categoria categoria)
        {
            using SqlConnection conn = new(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            // Validar nombre duplicado
            string check = "SELECT COUNT(*) FROM Categorias WHERE nombre = @nombre";
            using SqlCommand checkCmd = new(check, conn);
            checkCmd.Parameters.AddWithValue("@nombre", categoria.Nombre);
            int count = (int)checkCmd.ExecuteScalar();
            if (count > 0)
                return Conflict(new { mensaje = "La categoría ya existe." });

            string insert = "INSERT INTO Categorias (nombre, descripcion) VALUES (@nombre, @descripcion)";
            using SqlCommand cmd = new(insert, conn);
            cmd.Parameters.AddWithValue("@nombre", categoria.Nombre);
            cmd.Parameters.AddWithValue("@descripcion", (object?)categoria.Descripcion ?? DBNull.Value);

            int filas = cmd.ExecuteNonQuery();
            return filas > 0
                ? Ok(new { mensaje = "Categoría creada correctamente." })
                : BadRequest(new { mensaje = "Error al crear la categoría." });
        }

        // PUT: api/categorias/5
        [HttpPut("{id}")]
        public IActionResult Put(int id, [FromBody] Categoria categoria)
        {
            using SqlConnection conn = new(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            string update = "UPDATE Categorias SET nombre = @nombre, descripcion = @descripcion WHERE id = @id";
            using SqlCommand cmd = new(update, conn);
            cmd.Parameters.AddWithValue("@id", id);
            cmd.Parameters.AddWithValue("@nombre", categoria.Nombre);
            cmd.Parameters.AddWithValue("@descripcion", (object?)categoria.Descripcion ?? DBNull.Value);

            int filas = cmd.ExecuteNonQuery();
            return filas > 0
                ? Ok(new { mensaje = "Categoría actualizada correctamente." })
                : NotFound(new { mensaje = "Categoría no encontrada." });
        }

        // DELETE: api/categorias/5
        [HttpDelete("{id}")]
        public IActionResult Delete(int id)
        {
            using SqlConnection conn = new(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();

            string delete = "DELETE FROM Categorias WHERE id = @id";
            using SqlCommand cmd = new(delete, conn);
            cmd.Parameters.AddWithValue("@id", id);

            int filas = cmd.ExecuteNonQuery();
            return filas > 0
                ? Ok(new { mensaje = "Categoría eliminada correctamente." })
                : NotFound(new { mensaje = "Categoría no encontrada." });
        }
    }
}
