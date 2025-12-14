package belote.ex.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private int id;
    private String keycloak_id;
    private String username;
    private String email;
}
