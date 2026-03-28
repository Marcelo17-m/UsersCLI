using CisDotnetApi.DTOs;
using CisDotnetApi.Models;

namespace CisDotnetApi.Mappings;

public static class HealthMappingExtensions
{
    public static HealthResponse ToResponse(this HealthStatus health) =>
        new(health.Status, health.DatabaseConnected);
}
