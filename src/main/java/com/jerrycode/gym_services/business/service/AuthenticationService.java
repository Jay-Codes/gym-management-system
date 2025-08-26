package com.jerrycode.gym_services.business.service;

import com.jerrycode.gym_services.config.JwtService;
import com.jerrycode.gym_services.data.dao.UserRepository;
import com.jerrycode.gym_services.data.vo.AccessToken;
import com.jerrycode.gym_services.data.vo.User;
import com.jerrycode.gym_services.exception.ResourceNotFoundException;
import com.jerrycode.gym_services.request.LoginRequest;
import com.jerrycode.gym_services.request.PasswordUpdateRequest;
import com.jerrycode.gym_services.request.UserRequest;
import com.jerrycode.gym_services.response.Response;
import com.jerrycode.gym_services.response.UserResponse;
import com.jerrycode.gym_services.utils.ObjectFactory;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements AuthenticationManager {

    protected Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final org.springframework.security.authentication.AuthenticationManager authenticationManager;

    public Response<UserResponse> login(LoginRequest request) {
        logger.info("Login request {}", request);
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Fetch user
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Check additional conditions
            if (!user.isEnabled()) {
                logger.warn("Login failed: Disabled account for {}", request);
                return Response.<UserResponse>builder()
                        .success(false)
                        .message("Account is disabled")
                        .build();
            }

            // Generate token and response
            AccessToken token = jwtService.generateToken(user);
            UserResponse userResponse = modelMapper.map(user, UserResponse.class);
            logger.info("Login successful for {}", request);
            return Response.<UserResponse>builder()
                    .success(true)
                    .message("Login successful")
                    .token(token.getToken())
                    .ability(user.getRole())
                    .data(userResponse)
                    .build();

        } catch (BadCredentialsException e) {
            logger.warn("Login failed: Invalid credentials for {}", request, e);
            return Response.<UserResponse>builder()
                    .success(false)
                    .message("Invalid email or password")
                    .build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Login failed: {}", e.getMessage(), e);
            return Response.<UserResponse>builder()
                    .success(false)
                    .message("User not found")
                    .build();
        } catch (LockedException e) {
            logger.warn("Login failed: Locked account for {}", request);
            return Response.<UserResponse>builder()
                    .success(false)
                    .message("Account is locked due to too many attempts")
                    .build();
        } catch (AuthenticationException e) {
            logger.warn("Login failed: {}", e.getMessage(), e);
            return Response.<UserResponse>builder()
                    .success(false)
                    .message("Authentication failed")
                    .build();
        } catch (Exception e) {
            logger.error("Unexpected error during login for {}: {}", request, e);
            return Response.<UserResponse>builder()
                    .success(false)
                    .message("An unexpected error occurred during login")
                    .build();
        }
    }


    @Override
    public Response<UserResponse> logout(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        jwtService.revokeToken(token);
        return Response.<UserResponse>builder()
                .success(true)
                .message("Logged out successfully")
                .build();
    }

    @Override
    public Response<UserResponse> addUser(UserRequest request) {
        Response<UserResponse> response = new Response<>();
        try {
            logger.info("Add user request {}", request);

            // Validate request object
            if (request == null) {
                throw new IllegalArgumentException("User request cannot be null");
            }

            // Check for duplicates (name, email, phone_number)
            Map<String, List<String>> errors = ObjectFactory.newMapping();
            if (userRepository.existsByName(request.getName())) {
                errors.put("name", Collections.singletonList("The name has already been taken."));
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                errors.put("email", Collections.singletonList("The email has already been taken."));
            }
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                errors.put("phone_number", Collections.singletonList("The phone number has already been taken."));
            }

            // If there are any errors, return them
            if (!errors.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Validation failed");
                response.setErrors(errors);
                return response;
            }

            // Map request to User entity
            User user;
            try {
                user = modelMapper.map(request, User.class);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to map user request to entity");
            }

            // Encode password
            try {
                user.setPassword(passwordEncoder.encode("fitness"));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to encode password");
            }

            // Save user to the repository
            try {
                user = userRepository.save(user);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save user to database");
            }

            // Map User entity to response
            UserResponse userResponse;
            try {
                userResponse = modelMapper.map(user, UserResponse.class);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to map user entity to response");
            }

            // Set success response
            response.setData(userResponse);
            response.setSuccess(true);
            response.setMessage("User was added successfully");

            logger.info("Add user response {}", user);

        } catch (IllegalArgumentException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Invalid user request";
            logger.error("Validation error while adding user: {}", errorMessage, e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            Map<String, List<String>> errors = new HashMap<>();
            errors.put("general", Collections.singletonList(errorMessage));
            response.setErrors(errors);

        } catch (IllegalStateException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Internal processing error";
            logger.error("Processing error while adding user: {}", errorMessage, e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            Map<String, List<String>> errors = new HashMap<>();
            errors.put("general", Collections.singletonList(errorMessage));
            response.setErrors(errors);

        } catch (RuntimeException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Database operation failed";
            logger.error("Runtime error while adding user: {}", errorMessage, e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            Map<String, List<String>> errors = new HashMap<>();
            errors.put("general", Collections.singletonList(errorMessage));
            response.setErrors(errors);

        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while adding the user. Please try again.";
            logger.error("Unexpected error while adding user: {}", errorMessage, e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            Map<String, List<String>> errors = new HashMap<>();
            errors.put("general", Collections.singletonList(errorMessage));
            response.setErrors(errors);
        }
        logger.info("Add user response {}",response);
        return response;
    }

    @Override
    public Response<List<UserResponse>> getAllUsers() {
        Response<List<UserResponse>> response = new Response<>();
        try {
            // Fetch all users, map them to UserResponse, and set the response data
            List<UserResponse> userResponses = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                    .map(user -> modelMapper.map(user, UserResponse.class))
                    .collect(Collectors.toList());

            response.setData(userResponses);
            response.setSuccess(true);
            response.setMessage("Users retrieved successfully");

            logger.info("Fetched all users successfully. Total users: {}", userResponses.size());
        } catch (Exception e) {
            // Log the error and set the response to indicate failure
            logger.error("Error while fetching users", e);
            response.setSuccess(false);
            response.setMessage("An error occurred while retrieving users. Please try again later.");
        }
        logger.info("Getting all user response");
        return response;
    }

    @Override
    public Response<UserResponse> getUserById(Long id) {
        Response<UserResponse> response = new Response<>();
        try {
            // Fetch user by ID or throw ResourceNotFoundException if not found
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Map User to UserResponse
            UserResponse userResponse = modelMapper.map(user, UserResponse.class);

            // Prepare success response
            response.setData(userResponse);
            response.setSuccess(true);
            response.setMessage("User ID retrieved successfully");

            logger.info("User ID {} retrieved successfully: {}", id, userResponse);
        } catch (ResourceNotFoundException e) {
            // Handle case where the user is not found
            logger.error("Error while retrieving user by ID {}: {}", id, e);
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            // Handle other unexpected errors
            logger.error("Unexpected error while retrieving user by ID {}", id, e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred while retrieving the user. Please try again.");
        }
        logger.info("User fetched response {}",response);
        return response;
    }

    @Override
    public Response<UserResponse> updateUser(Long id, UserRequest request) {
        Response<UserResponse> response = new Response<>();
        try {
            logger.info("Update user request for ID {}: {}", id, request);

            // Validate request object
            if (request == null) {
                throw new IllegalArgumentException("User request cannot be null");
            }

            // Fetch the user by ID or throw ResourceNotFoundException if not found
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Check for duplicates (name, email, phone_number), excluding the current user
            Map<String, List<String>> errors = ObjectFactory.newMapping();
            if (request.getName() != null && !request.getName().equals(user.getName()) && userRepository.existsByNameAndIdNot(request.getName(), id)) {
                errors.put("name", Collections.singletonList("The name has already been taken."));
            }
            if (request.getEmail() != null && !request.getEmail().equals(user.getEmail()) && userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                errors.put("email", Collections.singletonList("The email has already been taken."));
            }
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber()) && userRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), id)) {
                errors.put("phone_number", Collections.singletonList("The phone number has already been taken."));
            }

            // If there are any errors, return them
            if (!errors.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Validation failed");
                response.setErrors(errors);
                logger.warn("Validation failed for user update ID {}: {}", id, errors);
                return response;
            }

            // Map the request data to the existing user entity
            modelMapper.map(request, user);

            // Save the updated user entity
            try {
                user = userRepository.save(user);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save updated user to database");
            }

            // Map the updated user entity to UserResponse
            UserResponse userResponse;
            try {
                userResponse = modelMapper.map(user, UserResponse.class);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to map updated user entity to response");
            }

            // Prepare success response
            response.setData(userResponse);
            response.setSuccess(true);
            response.setMessage("User updated successfully");

            logger.info("User ID {} updated successfully: {}", id, userResponse);

        } catch (ResourceNotFoundException e) {
            // Handle case where the user is not found
            logger.error("Error while updating user with ID {}: {}", id, e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            response.setErrors(Collections.singletonMap("general", Collections.singletonList(e.getMessage())));

        } catch (IllegalArgumentException e) {
            // Handle validation or business logic errors
            logger.error("Validation error while updating user with ID {}: {}", id, e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            response.setErrors(Collections.singletonMap("general", Collections.singletonList(e.getMessage())));

        } catch (IllegalStateException e) {
            // Handle mapping or encoding errors
            logger.error("Processing error while updating user with ID {}: {}", id, e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            response.setErrors(Collections.singletonMap("general", Collections.singletonList(e.getMessage())));

        } catch (RuntimeException e) {
            // Handle database or other runtime errors
            logger.error("Runtime error while updating user with ID {}: {}", id, e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            response.setErrors(Collections.singletonMap("general", Collections.singletonList(e.getMessage())));

        } catch (Exception e) {
            // Handle other unexpected errors
            logger.error("Unexpected error while updating user with ID {}: {}", id, e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            response.setErrors(Collections.singletonMap("general", Collections.singletonList("An unexpected error occurred while updating the user. Please try again.")));
        }
        logger.info("Update user response {}",response);
        return response;
    }

    @Override
    public Response<UserResponse> deleteUser(Long id) {
        Response<UserResponse> response = new Response<>();
        try {
            // Attempt to delete the user by ID
            if (!userRepository.existsById(id)) {
                throw new ResourceNotFoundException("User not found");
            }

            userRepository.deleteById(id);

            // Prepare success response
            response.setSuccess(true);
            response.setMessage("User deleted successfully");

            logger.info("User ID {} deleted successfully", id);
        } catch (ResourceNotFoundException e) {
            // Handle case where the user is not found
            logger.error("Error while deleting user with ID {}: {}", id, e);
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            // Handle other unexpected errors
            logger.error("Unexpected error while deleting user with ID {}", id, e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred while deleting the user. Please try again.");
        }
        logger.info("Delete user response {}",response);
        return response;
    }
    @Override
    public Response<UserResponse> updatePassword(Long id, PasswordUpdateRequest request) {
        Response<UserResponse> response = new Response<>();
        try {
            // Validate input (simulate Laravel validation)
            if (request.getOldPassword() == null || request.getOldPassword().isEmpty()
                    || request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Validation failed: Old password and new password are required.");
                return response;
            }

            // Fetch user by ID
            User user = userRepository.findById(id)
                    .orElse(null);

            if (user == null) {
                response.setSuccess(false);
                response.setMessage("User not found.");
                return response;
            }

            // Verify the old password
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                response.setSuccess(false);
                response.setMessage("Old password is incorrect.");
                return response;
            }

            // Update the user's password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            // Success response
            response.setSuccess(true);
            response.setMessage("Password updated successfully.");
            logger.info("Password for User ID {} updated successfully", id);

        } catch (Exception e) {
            logger.error("Unexpected error while updating password for User ID {}: {}", id, e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred while updating the password. Please try again.");
        }
        logger.info("Update password response {}",response);
        return response;
    }


    @Override
    public Response<UserResponse> updateProfileImage(Long id, MultipartFile image) {
        Response<UserResponse> response = new Response<>();
        try {
            // Find user by ID or throw exception if not found
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Create upload directory if it doesn't exist
            String UPLOAD_DIR = "uploads/profiles/";
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save the file with a unique name
            String filename = id + "_" + System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            Files.copy(image.getInputStream(), filePath);

            // Update user's profile image path
            user.setImage(filePath.toString());
            userRepository.save(user);

            // Map to UserResponse and set success response
            UserResponse userResponse = modelMapper.map(user, UserResponse.class);
            userResponse.setImageUrl(filePath.toString()); // Ensure URL is properly set
            response.setSuccess(true);
            response.setMessage("Profile image updated successfully");
            response.setData(userResponse);

            logger.info("Profile image for User ID {} updated successfully: {}", id, filePath);
        } catch (ResourceNotFoundException e) {
            // Handle case where the user is not found
            logger.error("Error while updating profile image for User ID {}: {}", id, e);
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        } catch (IOException e) {
            // Handle file-related errors
            logger.error("IO error while storing profile image for User ID {}: {}", id, e);
            response.setSuccess(false);
            response.setMessage("Failed to store profile image. Please try again.");
        } catch (Exception e) {
            // Handle any unexpected errors
            logger.error("Unexpected error while updating profile image for User ID {}", id, e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred while updating the profile image. Please try again.");
        }
        logger.info("Update profile image response {}",response);
        return response;
    }

    @Override
    public Response<UserResponse> getProfile(Long id) {
        Response<UserResponse> response = new Response<>();
        try {
            // Fetch user by ID or throw exception if not found
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Map User entity to UserResponse
            UserResponse userResponse = modelMapper.map(user, UserResponse.class);

            // Add any additional profile processing here if needed

            // Prepare success response
            response.setSuccess(true);
            response.setMessage("Profile retrieved successfully");
            response.setData(userResponse);

            logger.info("Profile for User ID {} retrieved successfully", id);
        } catch (ResourceNotFoundException e) {
            // Handle case where the user is not found
            logger.error("Error while retrieving profile for User ID {}: {}", id, e);
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            // Handle other unexpected errors
            logger.error("Unexpected error while retrieving profile for User ID {}", id, e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred while retrieving the profile. Please try again.");
        }
        logger.info("User profile response {}",response);
        return response;
    }
}
