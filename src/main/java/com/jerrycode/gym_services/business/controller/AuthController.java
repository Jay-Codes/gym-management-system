package com.jerrycode.gym_services.business.controller;

import com.jerrycode.gym_services.business.service.AuthenticationManager;
import com.jerrycode.gym_services.request.LoginRequest;
import com.jerrycode.gym_services.request.PasswordUpdateRequest;
import com.jerrycode.gym_services.request.UserRequest;
import com.jerrycode.gym_services.response.Response;
import com.jerrycode.gym_services.response.UserResponse;
import com.jerrycode.gym_services.utils.RoleCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

import static com.jerrycode.gym_services.response.BuildResponseEntity.buildResponseEntity;

@RestController
@RequestMapping("/api/")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<Response<UserResponse>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login Request for {}", request);
        Response<UserResponse> response = authenticationManager.login(request);
        logger.info("Login request Response for User {}",response);
        return buildResponseEntity(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Response<UserResponse>> logout(@RequestHeader("Authorization") String token) {
        logger.info("Logout request");
        Response<UserResponse> response = authenticationManager.logout(token);
        logger.info("Logout Response for User {}",response);
        return buildResponseEntity(response);
    }

    @RoleCheck({"admin"})
    @PostMapping("/add-user")
    public ResponseEntity<Response<UserResponse>> addUser(@Valid @RequestBody UserRequest request) {
        logger.info("Adding User {}" ,request);
        Response<UserResponse> response = authenticationManager.addUser(request);
        logger.info("Adding User Response {}",response);
        return buildResponseEntity(response);
    }

    @RoleCheck({"admin"})
    @GetMapping("/all-users")
    public ResponseEntity<Response<List<UserResponse>>> getAllUsers() {
        logger.info("fetching all users");
        Response<List<UserResponse>> response = authenticationManager.getAllUsers();
        logger.info("Fetching All users Response {}",response);
        return buildResponseEntity(response);
    }

    @RoleCheck({"admin","user"})
    @GetMapping("/user/{id}")
    public ResponseEntity<Response<UserResponse>> getUserById(@PathVariable Long id) {
        logger.info("User Request for {}" ,id);
        Response<UserResponse> response = authenticationManager.getUserById(id);
        logger.info("Getting User Request Response {}",response);
        return buildResponseEntity(response);
    }

    @RoleCheck({"admin","user"})
    @PutMapping("/user/{id}/update")
    public ResponseEntity<Response<UserResponse>> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        logger.info("User Update {}" , request);
        Response<UserResponse> response = authenticationManager.updateUser(id, request);
        logger.info("User Updated Response {}",response);
        return buildResponseEntity(response);
    }

    @RoleCheck({"admin"})
    @DeleteMapping("/user/{id}/delete")
    public ResponseEntity<Response<UserResponse>> deleteUser(@PathVariable Long id) {
        logger.info("User Delete {}" ,id);
        Response<UserResponse> response = authenticationManager.deleteUser(id);
        logger.info("User Deleted Response {}",response);
        return buildResponseEntity(response);
    }

    @RoleCheck({"admin","user"})
    @PutMapping("/password/{id}/update")
    public ResponseEntity<Response<UserResponse>> updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordUpdateRequest request) {
        logger.info("Update User {} Password {}",id,request);
        Response<UserResponse> response = authenticationManager.updatePassword(id, request);
        logger.info("Updated Password Response {}",response);
        return buildResponseEntity(response);
    }

    @RoleCheck({"admin","user"})
    @PostMapping("/profile/{id}/image")
    public ResponseEntity<Response<UserResponse>> updateProfileImage(
            @PathVariable Long id,
            @RequestParam MultipartFile image) {
        logger.info("User Update Profile {} image {}", id,image);
        Response<UserResponse> response = authenticationManager.updateProfileImage(id, image);
        logger.info("User Profile Update Response {}",response);
        return buildResponseEntity(response);
    }

    @RoleCheck({"admin","user"})
    @GetMapping("/profile/{id}")
    public ResponseEntity<Response<UserResponse>> getProfile(@PathVariable Long id) {
        logger.info("User Profile {}" ,id);
        Response<UserResponse> response = authenticationManager.getProfile(id);
        logger.info("User Profile Response {}",response);
        return buildResponseEntity(response);
    }

}