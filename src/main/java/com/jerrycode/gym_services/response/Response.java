package com.jerrycode.gym_services.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jerrycode.gym_services.utils.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
    private boolean success;
    private String message;
    private String token;
    private Role ability;
    private T data;
    private Map<String, List<String>> errors;
}
