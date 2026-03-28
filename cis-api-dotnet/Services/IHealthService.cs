using CisDotnetApi.DTOs;

namespace CisDotnetApi.Services;

public interface IHealthService
{
    Task<HealthResponse> CheckAsync();
}
