package belote.ex.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CreateUserRequest {

    @Size(min = 2, max = 20, message = "Username must be between 2 and 20 characters")
    private String username;
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    private String password;

    private String email;
}

