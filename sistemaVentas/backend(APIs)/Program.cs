

using Microsoft.AspNetCore.Authentication.JwtBearer;

using Microsoft.IdentityModel.Tokens;
using System.Text;
using System.Text.Json;


var builder = WebApplication.CreateBuilder(args);

// Agrega soporte para controladores (como ProductosController)
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();

var key = "esta_es_mi_clave_secreta_muy_segura_1234";
builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuer = false,
        ValidateAudience = false,
        ValidateLifetime = true,
        ValidateIssuerSigningKey = true,
        IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(key)),
        ClockSkew = TimeSpan.Zero
    };

});

var app = builder.Build();
app.UseAuthentication(); // Este siempre va antes de UseAuthorization
app.UseAuthorization();
if (app.Environment.IsDevelopment())
{
    
}
// Activar uso de controladores
app.MapControllers();


// app.UseHttpsRedirection();
app.MapGet("/", context =>
{
    context.Response.Redirect("/swagger");
    return Task.CompletedTask;
});


app.Run();
