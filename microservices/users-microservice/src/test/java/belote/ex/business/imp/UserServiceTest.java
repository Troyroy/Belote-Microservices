//package belote.ex.business.imp;
//
//import belote.ex.config.security.token.AccessTokenEncoder;
//import belote.ex.domain.*;
//import belote.ex.persistance.UserRepository;
//import belote.ex.persistance.entity.UserEntity;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DataJpaTest
//class UserServiceTest {
//
//
//    @Mock
//    private UserRepository repository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private AccessTokenEncoder accessTokenEncoder;
//
//    @InjectMocks
//    private UserService userService;
//
//    @Test
//    void testGetAllUsers() {
//
//        List<UserEntity> users = List.of(
//                new UserEntity(1, "user1", "password1", "email1", "REGULAR",null,null),
//                new UserEntity(2, "user2", "password2", "email2","REGULAR",null, null));
//        when(repository.findAll()).thenReturn(users);
//
//        GetAllUsersResponse response = userService.getAllUsers();
//
//        assertEquals(2, response.getUsers().size());
//        assertEquals("user1", response.getUsers().get(0).getUsername());
//        assertEquals("user2", response.getUsers().get(1).getUsername());
//    }
//
//    @Test
//    void testGetUser() {
//
//        UserEntity user = new UserEntity(1, "user1", "password1", "email1","REGULAR",null,null);
//        when(repository.findById(1)).thenReturn(Optional.of(user));
//
//        GetUserResponse response = userService.getUser(1);
//
//        assertEquals(1, response.getId());
//        assertEquals("user1", response.getUsername());
//        assertEquals("password1", response.getPassword());
//    }
//
//    @Test
//    void testAddUser() {
//
//        CreateUserRequest request = new CreateUserRequest("user1", "password1", "email1");
//        when(repository.save(any())).thenReturn(new UserEntity(1, "user1", "password1", "email1","REGULAR",null,null));
//
//        CreateUserResponce response = userService.addUser(request);
//
//        assertEquals(1, response.getId());
//    }
//
//    @Test
//    void testDeleteUser() {
//
//        userService.deleteUser(1);
//
//        verify(repository).deleteById(1);
//    }
//
//    @Test
//    void testUpdateUser() {
//
//        UserEntity user = new UserEntity(1, "user1", "password1", "email1","REGULAR",null,null);
//        when(repository.findById(1)).thenReturn(Optional.of(user));
//
//        userService.updateUser(1, "newUsername");
//
//        assertEquals("newUsername", user.getUsername());
//    }
//
//    /*@Test
//    void testLoginUser() {
//          String password = "password123";
//        String encoded = "encodedpassword";
//        String email = "email@gmail.com";
//
//        List<UserEntity> users = List.of(
//                new UserEntity(1, "username1", encoded, email, "REGULAR"));
//
//        when(repository.findAll()).thenReturn(users);
//        when(passwordEncoder.matches(password, encoded)).thenReturn(true);
//        when(accessTokenEncoder.encode(any())).thenReturn("accessToken");
//
//        LoginResponce response = userService.loginUser(email,password);
//
//        assertEquals(1, response.getId());
//    }*/
//}