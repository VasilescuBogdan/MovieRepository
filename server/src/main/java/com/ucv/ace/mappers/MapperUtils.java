package com.ucv.ace.mappers;

import com.ucv.ace.dto.UserDTO;
import com.ucv.ace.entities.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class MapperUtils {

    public static User toUser(UserDTO userDto) {
        var user = new User();

        user.setUserId(userDto.getUserId());
        user.setUserRole("User");
        user.setEmail(userDto.getEmail());
        user.setPassword(new BCryptPasswordEncoder().encode(userDto.getPassword()));
        user.setLastName(userDto.getLastName());
        user.setFirstName(userDto.getFirstName());
        user.setDateOfBirth(userDto.getDateOfBirth());

        return user;
    }

    public static UserDTO toUserDTO(User user) {
        var userDTO = new UserDTO();

        userDTO.setUserId(user.getUserId());
        userDTO.setUserRole(user.getUserRole());
        userDTO.setEmail(user.getEmail());
        userDTO.setLastName(user.getLastName());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setDateOfBirth(user.getDateOfBirth());

        return userDTO;
    }
}
