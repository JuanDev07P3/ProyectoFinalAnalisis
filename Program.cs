var builder = WebApplication.CreateBuilder(args);

// Agrega soporte para controladores (como ProductosController)
builder.Services.AddControllers();

var app = builder.Build();

// Activar uso de controladores
app.MapControllers();

// Desactiva HTTPS si te está molestando
// app.UseHttpsRedirection();

app.Run();
