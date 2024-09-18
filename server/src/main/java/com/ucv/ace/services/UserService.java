package com.ucv.ace.services;

import com.ucv.ace.entities.User;
import com.ucv.ace.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private UserRepository usersDB;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usersDB.findAll().stream().filter(user -> Objects.equals(username, user.getUsername())).findFirst().orElseThrow();
    }

    public void create(User user) {
        usersDB.save(user);
    }

    public Optional<User> getCurrentUser() {
        Optional<User> loggedUser = Optional.empty();
        final Object currentUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(currentUser instanceof User user) {
            loggedUser = Optional.of(user);

        }
        return loggedUser;
    }
}
