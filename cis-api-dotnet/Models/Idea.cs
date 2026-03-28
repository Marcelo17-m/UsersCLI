using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace CisDotnetApi.Models;

[Table("ideas")]
public class Idea
{
    [Key] public int Id { get; set; }
    public string Content { get; set; } = string.Empty;
    public int TopicId { get; set; }
    public string UserId { get; set; } = string.Empty;
}
