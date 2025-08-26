package com.jerrycode.gym_services.business.service;

import com.jerrycode.gym_services.request.LoginRequest;
import com.jerrycode.gym_services.request.PasswordUpdateRequest;
import com.jerrycode.gym_services.request.UserRequest;
import com.jerrycode.gym_services.response.Response;
import com.jerrycode.gym_services.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AuthenticationManager {
    Response<UserResponse> login(LoginRequest request);
    Response<UserResponse> logout(String token);
    Response<UserResponse> addUser(UserRequest request);
    Response<List<UserResponse>> getAllUsers();
    Response<UserResponse> getUserById(Long id);
    Response<UserResponse> updateUser(Long id, UserRequest request);
    Response<UserResponse> deleteUser(Long id);
    Response<UserResponse> updatePassword(Long id, PasswordUpdateRequest request);
    Response<UserResponse> updateProfileImage(Long id, MultipartFile image);
    Response<UserResponse> getProfile(Long id);
}