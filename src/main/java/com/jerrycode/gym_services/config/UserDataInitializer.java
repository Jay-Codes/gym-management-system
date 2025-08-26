package com.jerrycode.gym_services.config;

import com.jerrycode.gym_services.data.dao.UserRepository;
import com.jerrycode.gym_services.data.vo.User;
import com.jerrycode.gym_services.utils.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class UserDataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .phoneNumber("1234567890")
                    .password(passwordEncoder.encode("password"))
                    .role(Role.admin)
                    .build();

            User user = User.builder()
                    .name("Jane Smith")
                    .email("jane@example.com")
                    .phoneNumber("2345678901")
                    .password(passwordEncoder.encode("password"))
                    .role(Role.user)
                    .build();

            // Add more users as needed...

            userRepository.save(admin);
            userRepository.save(user);

            System.out.println("Default users initialized successfully");
        }
    }
}
