using CisDotnetApi.Models;
using Microsoft.EntityFrameworkCore;

namespace CisDotnetApi.Data;

public class CisDbContext : DbContext
{
    public CisDbContext(DbContextOptions<CisDbContext> options) : base(options) { }

    public DbSet<Topic> Topics { get; set; } = null!;
    public DbSet<Idea> Ideas { get; set; } = null!;
    public DbSet<Vote> Votes { get; set; } = null!;
}
