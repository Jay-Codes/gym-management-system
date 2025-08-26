package com.jerrycode.gym_services.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jerrycode.gym_services.data.vo.User;
import com.jerrycode.gym_services.utils.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private String token;
    private Role ability;
    private UserResponse user;

    public LoginResponse(String token, Role ability, UserResponse user) {
        this.token = token;
        this.ability = ability;
        this.user = user;
    }
}