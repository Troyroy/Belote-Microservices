package belote.ex.domain;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GetUserResponse {
    private int id;
    private String username;
    private String password;
    private String email;
}
