package belote.ex.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UpdateUserRequest {
    @NotBlank
    private int id;
    @Size(min = 1, max = 12, message = "Username must be between 6 and 12 characters")
    private String username;
}
