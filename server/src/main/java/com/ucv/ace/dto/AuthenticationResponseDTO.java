package com.ucv.ace.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class AuthenticationResponseDTO {

    private String email;

    private String accessToken;
}
