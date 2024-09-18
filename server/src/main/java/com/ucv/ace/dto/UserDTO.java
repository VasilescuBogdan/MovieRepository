package com.ucv.ace.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class UserDTO {

    private Long userId;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private String userRole;

    private LocalDate dateOfBirth;

}
