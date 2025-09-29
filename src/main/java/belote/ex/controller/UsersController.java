package belote.ex.controller;

import belote.ex.business.UserServiceInt;
import belote.ex.business.exceptions.NotFoundException;
import belote.ex.domain.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UsersController {
    private UserServiceInt userUseCases;

    @RolesAllowed({"ADMIN"})
    @GetMapping
    public ResponseEntity<GetAllUsersResponse> getUsers() {
      return ResponseEntity.ok(userUseCases.getAllUsers());

    }

    @GetMapping("{id}")
    public ResponseEntity<GetUserResponse> getUsers(@PathVariable int id) {
        try {

            return ResponseEntity.ok(userUseCases.getUser(id));
        }
        catch(NotFoundException ex){
            throw new ResponseStatusException(NOT_FOUND, "No such id");
        }

    }

    @PostMapping
    public ResponseEntity<CreateUserResponce> addUser(@RequestBody @Valid CreateUserRequest request){

        CreateUserResponce response = userUseCases.addUser(request);
        
        return  ResponseEntity.ok().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponce> loginUser(@RequestBody  LoginUserRequest request){

        LoginResponce response = userUseCases.loginUser(request.getEmail(), request.getPassword());

        return  ResponseEntity.ok().body(response);
    }

    @DeleteMapping("{id}")
    public void deleteUser(@PathVariable int id) {
        userUseCases.deleteUser(id);

    }
    @PutMapping()
    public void updateUser(UpdateUserRequest request) {
        //use id
        userUseCases.updateUser(request.getId(),request.getUsername());
    }

    @PutMapping("{id}")
    public void updateUser(@PathVariable int id, @RequestBody @Valid UpdateUserRequest request) {
        userUseCases.updateUser(id,request.getUsername());
    }
}
