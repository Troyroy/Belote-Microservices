package belote.ex.persistance;

import belote.ex.persistance.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<UserEntity, Integer> {

    UserEntity findByEmail(String email);
}
