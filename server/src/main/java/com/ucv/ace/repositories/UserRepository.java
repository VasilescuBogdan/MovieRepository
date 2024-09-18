package com.ucv.ace.repositories;

import com.ucv.ace.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
