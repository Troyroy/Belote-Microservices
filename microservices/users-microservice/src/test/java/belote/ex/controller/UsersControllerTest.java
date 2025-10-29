//package belote.ex.controller;
//
//import belote.ex.business.exceptions.NotFoundException;
//import belote.ex.business.imp.UserService;
//import belote.ex.config.security.token.AccessTokenDecoder;
//import belote.ex.config.security.token.AccessTokenEncoder;
//import belote.ex.domain.*;
//import belote.ex.persistance.entity.UserEntity;
//import belote.ex.persistance.UserRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.MediaType;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ExtendWith(MockitoExtension.class)
//@SpringBootTest
//@AutoConfigureMockMvc
//class UsersControllerTest {
//
//
//    @Autowired
//    private MockMvc mockMvc;
//    @MockBean
//    private UserService userService;
//
//
//
//
//    @Test
//    @WithMockUser(roles = {"ADMIN"})
//    void getUsers() throws Exception {
//
//        GetAllUsersResponse response = GetAllUsersResponse.builder().build() .builder()
//                .users(List.of( User.builder().id(1) .username("Troy").password("12345678").email("troyroy@gmail.com").build(),
//                        User.builder().id(2) .username("Troyroy").password("12345678").email("troyroy2@gmail.com").build())) .build();
//
//        when(userService.getAllUsers()).thenReturn(response);
//
//        mockMvc.perform(get("/users"))
//                .andExpect(status().isOk())
//                .andExpect(content().json(toJson(response)));
//
//    }
//
//    private String toJson(final Object obj) {
//        try {
//            return new ObjectMapper().writeValueAsString(obj);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//
//    @Test
//    @WithMockUser(roles = {"ADMIN"})
//    void getUser() throws Exception {
//
//        UserEntity user = UserEntity.builder().id(1) .username("Troy").password("12345678").email("troyroy@gmail.com").build();
//
//        GetUserResponse response = GetUserResponse.builder()
//                .id(1)
//                .username("Troy")
//                .password("12345678")
//                .build();
//
//        when(userService.getUser(1)).thenReturn(response);
//
//        mockMvc.perform(get("/users/1"))
//                .andExpect(status().isOk())
//                .andExpect(content().json(toJson(response)));
//    }
//
//    @Test
//    @WithMockUser(roles = {"ADMIN"})
//    void failGetUser() throws Exception {
//        int id = 0;
//
//        when(userService.getUser(id)).thenThrow(new NotFoundException("User is not found"));
//
//        mockMvc.perform(get("/users/{id}",id))
//                .andExpect(status().isNotFound());
//
//        verify(userService).getUser(id);
//    }
//
//    @Test
//    void addUser() throws Exception {
//
//        CreateUserRequest request = CreateUserRequest.builder()
//                .username("useranme1")
//                .password("password1")
//                .email("email@gmail.com")
//                .build();
//
//        CreateUserResponce response = CreateUserResponce.builder()
//                .id(1)
//                .build();
//       when(userService.addUser(request)).thenReturn(response);
//
//        mockMvc.perform(post("/users")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(toJson(request)))
//                        .andExpect(status().isOk())
//                        .andExpect(content().json(toJson(response)));
//
//
//
//    }
//    @Test
//    void loginUser() throws Exception{
//        LoginUserRequest request = LoginUserRequest.builder()
//                .email("email@gmail.com")
//                .password("12345678")
//                .build();
//
//        LoginResponce response = LoginResponce.builder()
//                .id(1)
//                .build();
//
//        when(userService.loginUser("email@gmail.com","12345678")).thenReturn(response);
//
//        mockMvc.perform(post("/users/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(toJson(request)))
//                        .andExpect(status().isOk());
//    }
//    @Test
//    @WithMockUser(roles = {"ADMIN"})
//    void  deleteUser() throws Exception{
//        int id = 1;
//        doNothing().when(userService).deleteUser(id);
//
//        mockMvc.perform(delete("/users/" + id ))
//                        .andExpect(status()
//                                .isOk());
//
//        verify(userService).deleteUser(id);
//    }
//}