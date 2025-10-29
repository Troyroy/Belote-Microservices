package belote.ex.business;

import belote.ex.domain.*;

public interface UserServiceInt {
    GetAllUsersResponse getAllUsers();
    GetUserResponse getUser(int id);
    CreateUserResponce addUser(CreateUserRequest user);
    void deleteUser(int id);

    void updateUser(int id, String username);

    LoginResponce loginUser(String username, String password);

}
