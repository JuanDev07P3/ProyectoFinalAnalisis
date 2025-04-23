using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using BackendVentas.Models;

namespace BackendVentas.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ClientesController : ControllerBase
    {
        private readonly IConfiguration _configuration;

        public ClientesController(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        [HttpGet]
        public IActionResult Get()
        {
            List<Cliente> clientes = new List<Cliente>();

            using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();
            string query = "SELECT * FROM Clientes";
            using SqlCommand cmd = new SqlCommand(query, conn);
            using SqlDataReader reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                clientes.Add(new Cliente
                {
                    Id = (int)reader["Id"],
                    Nombre = reader["Nombre"]?.ToString() ?? string.Empty,
                    Correo = reader["Correo"]?.ToString() ?? string.Empty
                });
            }
            return Ok(clientes);
        }
        [HttpPost]
        public IActionResult Post([FromBody] Cliente cliente)
        {
            using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();
            string query = "INSERT INTO Clientes (Nombre, Correo) VALUES (@Nombre, @Correo)";
            using SqlCommand cmd = new SqlCommand(query, conn);
            cmd.Parameters.AddWithValue("@Nombre", cliente.Nombre);
            cmd.Parameters.AddWithValue("@Correo", cliente.Correo);
            int filasAfectadas = cmd.ExecuteNonQuery();
            if (filasAfectadas > 0)
            {
                return Ok(new{mensaje = "Cliente creado correctamente"});
            }
            else
            {
                return BadRequest(new { mensaje = "Error al crear el cliente" });
            }
        }
        [HttpPut("{id}")]
        public IActionResult Put(int id, [FromBody] Cliente cliente)
        {
            using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();
            string query = "UPDATE Clientes SET Nombre = @Nombre, Correo = @Correo WHERE Id = @Id";
            using SqlCommand cmd = new SqlCommand(query, conn);
            cmd.Parameters.AddWithValue("@Id", id);
            cmd.Parameters.AddWithValue("@Nombre", cliente.Nombre);
            cmd.Parameters.AddWithValue("@Correo", cliente.Correo);
            int filasAfectadas = cmd.ExecuteNonQuery();
            if (filasAfectadas > 0)
            {
                return Ok(new { mensaje = "Cliente actualizado correctamente" });
            }
            else
            {
                return BadRequest(new { mensaje = "Error al actualizar el cliente" });
            }
        }   
        [HttpDelete("{id}")]
        public IActionResult Delete(int id)
        {
            using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
            conn.Open();
            string query = "DELETE FROM Clientes WHERE Id = @Id";
            using SqlCommand cmd = new SqlCommand(query, conn);
            cmd.Parameters.AddWithValue("@Id", id);
            int filasAfectadas = cmd.ExecuteNonQuery();
            if (filasAfectadas > 0)
            {
                return Ok(new { mensaje = "Cliente eliminado correctamente" });
            }
            else
            {
                return BadRequest(new { mensaje = "Error al eliminar el cliente" });
            }
        }
    }
}