package belote.ex.business;

import belote.ex.domain.*;
import belote.ex.persistance.entity.UserEntity;
import org.springframework.transaction.annotation.Transactional;

public interface UserServiceInt {
    public UserEntity getUserByKeycloakId(String keycloakId);

    public UserEntity getUserById(Integer id);

    public UserEntity createOrUpdateUser(String keycloakId, String username, String email);

}
