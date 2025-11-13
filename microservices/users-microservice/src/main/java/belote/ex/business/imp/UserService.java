package belote.ex.business.imp;

import belote.ex.business.UserServiceInt;
import belote.ex.business.exceptions.NotFoundException;

import belote.ex.domain.*;
import belote.ex.persistance.UserRepository;
import belote.ex.persistance.entity.UserEntity;
import io.micrometer.core.instrument.Counter;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import token.AccessTokenEncoder;
import token.impl.AccessTokenImpl;

import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService implements UserServiceInt {
    private UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenEncoder accessTokenEncoder;
    private final Counter loginCounter;

    @Override
    public GetAllUsersResponse getAllUsers() {
        List<UserEntity> users = repository.findAll();

        final GetAllUsersResponse response = new GetAllUsersResponse();
        List<User> allUsers = users
                .stream()
                .map(UserConverter::convertUser)
                .toList();
        response.setUsers(allUsers);

        return  response;
    }

    @Override
    public GetUserResponse getUser(int id)  {

        UserEntity user = repository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));

            return GetUserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .email(user.getEmail())
                    .build();

    }

    @Override
    public CreateUserResponce addUser(CreateUserRequest request) {

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        UserEntity user = repository.save(UserEntity.builder()
                                .username(request.getUsername())
                                .password(encodedPassword)
                                .email(request.getEmail())
                                .role("REGULAR")
                                .build());

        return CreateUserResponce.builder()
                .id(user.getId())
                .build();
    }

    @Override
    public void deleteUser(int id) {
        repository.deleteById(id);
    }

    @Override
    public void updateUser(int id, String username) {
        this.getUser(id);
        repository.findById(id).ifPresent(user -> user.setUsername(username));
    }

    @Override
    public LoginResponce loginUser(String email, String password) {


      /*  UserEntity user = repository.findAll().stream()
                .filter(x -> (x.getEmail().equals(email)) && (passwordEncoder.matches(password, x.getPassword())))
                .findFirst().orElseThrow(() -> new NotFoundException("No matching credentials"));*/

        UserEntity user2 = repository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("No matching credentials"));


        loginCounter.increment();
        String token = generateAccessToken(user2);


        return LoginResponce.builder()
                .id(user2.getId())
                .token(token).build();
    }

    private String generateAccessToken(UserEntity user) {
        Integer userId = user.getId();
        String role = user.getRole();

        return accessTokenEncoder.encode(
                new AccessTokenImpl(user.getUsername(), userId, role));
    }

}
