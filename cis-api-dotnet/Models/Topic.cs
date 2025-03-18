using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace CisDotnetApi.Models;

[Table("topics")]
public class Topic
{
    [Key] public int Id { get; set; }
    public string Title { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public string UserId { get; set; } = string.Empty;
}
