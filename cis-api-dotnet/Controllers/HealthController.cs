using CisDotnetApi.Services;
using Microsoft.AspNetCore.Mvc;

namespace CisDotnetApi.Controllers;

[ApiController]
[Route("health")]
public class HealthController : ControllerBase
{
    private readonly IHealthService _service;
    public HealthController(IHealthService service) => _service = service;

    [HttpGet]
    public async Task<IActionResult> Get() => Ok(await _service.CheckAsync());
}
