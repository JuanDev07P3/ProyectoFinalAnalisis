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

        // GET: api/clientes
        [HttpGet]
        public IActionResult Get()
        {
            try
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
                        Apellido = reader["Apellido"]?.ToString() ?? string.Empty,
                        NIT = reader["NIT"]?.ToString() ?? string.Empty,
                        Direccion = reader["Direccion"]?.ToString(),
                        Telefono = reader["Telefono"]?.ToString(),
                        email = reader["email"]?.ToString()
                    });
                }

                return Ok(clientes);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al obtener los clientes", detalle = ex.Message });
            }
        }
        // GET: api/clientes/{id}
        [HttpGet("{id}")]
        public IActionResult Get(int id)
        {
            try
            {
                using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
                conn.Open();

                string query = "SELECT * FROM Clientes WHERE Id = @Id";
                using SqlCommand cmd = new SqlCommand(query, conn);
                cmd.Parameters.AddWithValue("@Id", id);

                using SqlDataReader reader = cmd.ExecuteReader();
                if (reader.Read())
                {
                    Cliente cliente = new Cliente
                    {
                        Id = (int)reader["Id"],
                        Nombre = reader["Nombre"]?.ToString() ?? string.Empty,
                        Apellido = reader["Apellido"]?.ToString() ?? string.Empty,
                        NIT = reader["NIT"]?.ToString() ?? string.Empty,
                        Direccion = reader["Direccion"]?.ToString(),
                        Telefono = reader["Telefono"]?.ToString(),
                        email = reader["email"]?.ToString()
                    };
                    return Ok(cliente);
                }
                else
                {
                    return NotFound(new { mensaje = "Cliente no encontrado." });
                }
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al obtener el cliente", detalle = ex.Message });
            }
        }
        // GET: api/clientes/search?nit={nit}
        [HttpGet("search")]
        public IActionResult SearchByNIT([FromQuery] string nit)
        {
            try
            {
                using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
                conn.Open();

                string query = "SELECT * FROM Clientes WHERE NIT = @NIT";
                using SqlCommand cmd = new SqlCommand(query, conn);
                cmd.Parameters.AddWithValue("@NIT", nit);

                using SqlDataReader reader = cmd.ExecuteReader();
                if (reader.Read())
                {
                    var cliente = new Cliente
                    {
                        Id = (int)reader["Id"],
                        Nombre = reader["Nombre"]?.ToString() ?? string.Empty,
                        Apellido = reader["Apellido"]?.ToString() ?? string.Empty,
                        NIT = reader["NIT"]?.ToString() ?? string.Empty,
                        Direccion = reader["Direccion"]?.ToString(),
                        Telefono = reader["Telefono"]?.ToString(),
                        email = reader["email"]?.ToString()
                    };
                    return Ok(cliente);
                }
                else
                {
                    return NotFound(new { mensaje = "Cliente no encontrado." });
                }
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al buscar el cliente", detalle = ex.Message });
            }
        }

        // POST: api/clientes
        [HttpPost]
        public IActionResult Post([FromBody] Cliente cliente)
        {
            try
            {
                using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
                conn.Open();

                // Validar duplicado de NIT
                string checkQuery = "SELECT COUNT(*) FROM Clientes WHERE NIT = @NIT";
                using SqlCommand checkCmd = new SqlCommand(checkQuery, conn);
                checkCmd.Parameters.AddWithValue("@NIT", cliente.NIT);
                int count = (int)checkCmd.ExecuteScalar();
                if (count > 0)
                {
                    return Conflict(new { mensaje = "Ya existe un cliente con el mismo NIT" });
                }

                string query = @"INSERT INTO Clientes (Nombre, Apellido, NIT, Direccion, Telefono, email) 
                                 VALUES (@Nombre, @Apellido, @NIT, @Direccion, @Telefono, @Email)";

                using SqlCommand cmd = new SqlCommand(query, conn);
                cmd.Parameters.AddWithValue("@Nombre", cliente.Nombre);
                cmd.Parameters.AddWithValue("@Apellido", cliente.Apellido);
                cmd.Parameters.AddWithValue("@NIT", cliente.NIT);
                cmd.Parameters.AddWithValue("@Direccion", (object?)cliente.Direccion ?? DBNull.Value);
                cmd.Parameters.AddWithValue("@Telefono", (object?)cliente.Telefono ?? DBNull.Value);
                cmd.Parameters.AddWithValue("@Email", (object?)cliente.email ?? DBNull.Value);

                int filas = cmd.ExecuteNonQuery();
                return filas > 0
                    ? Ok(new { mensaje = "Cliente registrado correctamente." })
                    : BadRequest(new { mensaje = "No se pudo registrar el cliente." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al registrar el cliente", detalle = ex.Message });
            }
        }

        // PUT: api/clientes/{id}
        [HttpPut("{id}")]
        public IActionResult Put(int id, [FromBody] Cliente cliente)
        {
            try
            {
                using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
                conn.Open();

                // Validar NIT duplicado en otro cliente
                string checkQuery = "SELECT COUNT(*) FROM Clientes WHERE NIT = @NIT AND Id != @Id";
                using SqlCommand checkCmd = new SqlCommand(checkQuery, conn);
                checkCmd.Parameters.AddWithValue("@NIT", cliente.NIT);
                checkCmd.Parameters.AddWithValue("@Id", id);
                int count = (int)checkCmd.ExecuteScalar();
                if (count > 0)
                {
                    return Conflict(new { mensaje = "Ya existe otro cliente con el mismo NIT" });
                }

                string query = @"UPDATE Clientes SET 
                                    Nombre = @Nombre, 
                                    Apellido = @Apellido, 
                                    NIT = @NIT, 
                                    Direccion = @Direccion, 
                                    Telefono = @Telefono, 
                                    email = @Email 
                                 WHERE Id = @Id";

                using SqlCommand cmd = new SqlCommand(query, conn);
                cmd.Parameters.AddWithValue("@Id", id);
                cmd.Parameters.AddWithValue("@Nombre", cliente.Nombre);
                cmd.Parameters.AddWithValue("@Apellido", cliente.Apellido);
                cmd.Parameters.AddWithValue("@NIT", cliente.NIT);
                cmd.Parameters.AddWithValue("@Direccion", (object?)cliente.Direccion ?? DBNull.Value);
                cmd.Parameters.AddWithValue("@Telefono", (object?)cliente.Telefono ?? DBNull.Value);
                cmd.Parameters.AddWithValue("@Email", (object?)cliente.email ?? DBNull.Value);

                int filas = cmd.ExecuteNonQuery();
                return filas > 0
                    ? Ok(new { mensaje = "Cliente actualizado correctamente." })
                    : NotFound(new { mensaje = "Cliente no encontrado." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al actualizar el cliente", detalle = ex.Message });
            }
        }

        // DELETE: api/clientes/{id}
        [HttpDelete("{id}")]
        public IActionResult Delete(int id)
        {
            try
            {
                using SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("ConexionSQL"));
                conn.Open();

                string query = "DELETE FROM Clientes WHERE Id = @Id";
                using SqlCommand cmd = new SqlCommand(query, conn);
                cmd.Parameters.AddWithValue("@Id", id);

                int filas = cmd.ExecuteNonQuery();
                return filas > 0
                    ? Ok(new { mensaje = "Cliente eliminado correctamente." })
                    : NotFound(new { mensaje = "Cliente no encontrado." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al eliminar el cliente", detalle = ex.Message });
            }
        }
    }
}