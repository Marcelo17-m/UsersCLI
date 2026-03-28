using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace CisDotnetApi.Models;

[Table("votes")]
public class Vote
{
    [Key] public int Id { get; set; }
    public int IdeaId { get; set; }
    public string UserId { get; set; } = string.Empty;
}
