package com.ucv.ace.controllers;

import com.ucv.ace.dto.AuthenticationRequestDTO;
import com.ucv.ace.dto.AuthenticationResponseDTO;
import com.ucv.ace.dto.UserDTO;
import com.ucv.ace.entities.User;
import com.ucv.ace.mappers.MapperUtils;
import com.ucv.ace.security.JWT;
import com.ucv.ace.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:8081", maxAge = 3600, allowCredentials="true")
@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/v1/users")
public class UsersController {
    private final AuthenticationManager authManager;

    private final JWT jwt;

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequestDTO request) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword())
            );
            User user = (User) authentication.getPrincipal();
            String accessToken = jwt.generateAccessToken(user);
            AuthenticationResponseDTO response = new AuthenticationResponseDTO(user.getEmail(), accessToken);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createAccount(@RequestBody UserDTO userInfo) {
        this.userService.create(MapperUtils.toUser(userInfo));
        return ResponseEntity.ok("OK");
    }
}
