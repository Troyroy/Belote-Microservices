package belote.ex.business.imp;

import belote.ex.domain.User;
import belote.ex.persistance.entity.UserEntity;

final class UserConverter {

    private UserConverter(){}
    public static User convertUser(UserEntity user) {
       return  User.builder().id(user.getId())
                            .username(user.getUsername())
                            .password(user.getPassword())
                            .email(user.getEmail())
                            .build();

    }
}
