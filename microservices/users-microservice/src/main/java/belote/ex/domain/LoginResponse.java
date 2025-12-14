package belote.ex.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Integer expiresIn;
    private Integer refreshExpiresIn;

    // User info
    private Long userId;
    private String username;
    private String email;
}