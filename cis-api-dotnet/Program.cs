using CisDotnetApi.Data;
using CisDotnetApi.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.ApplicationModels;
using Microsoft.EntityFrameworkCore;

var builder = WebApplication.CreateBuilder(args);

var connectionString = builder.Configuration.GetConnectionString("CisDatabase");
builder.Services.AddDbContext<CisDbContext>(options =>
    options.UseMySql(connectionString, new MySqlServerVersion(new Version(8, 0, 0))));

builder.Services.AddScoped<IHealthService, HealthService>();

builder.Services.AddControllers(options =>
{
    options.Conventions.Add(new RoutePrefixConvention("api/v1"));
});

builder.Services.AddOpenApi();

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
    app.UseSwaggerUI(c =>
    {
        c.SwaggerEndpoint("/openapi/v1.json", "CIS API v1");
        c.RoutePrefix = "swagger";
    });
}

app.MapControllers();

app.Run();

public class RoutePrefixConvention : IControllerModelConvention
{
    private readonly string _prefix;
    public RoutePrefixConvention(string prefix) => _prefix = prefix;

    public void Apply(ControllerModel controller)
    {
        foreach (var selector in controller.Selectors)
        {
            if (selector.AttributeRouteModel != null)
            {
                selector.AttributeRouteModel = AttributeRouteModel.CombineAttributeRouteModel(
                    new AttributeRouteModel(new RouteAttribute(_prefix)),
                    selector.AttributeRouteModel);
            }
            else
            {
                selector.AttributeRouteModel = new AttributeRouteModel(new RouteAttribute(_prefix));
            }
        }
    }
}
