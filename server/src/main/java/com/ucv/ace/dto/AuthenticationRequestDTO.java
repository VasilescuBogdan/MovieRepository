package com.ucv.ace.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthenticationRequestDTO {
    private String email;

    private String password;
}
