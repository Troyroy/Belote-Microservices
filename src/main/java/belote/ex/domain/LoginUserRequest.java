package belote.ex.domain;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginUserRequest {
    private String email;
    private String password;
}