package com.jerrycode.gym_services.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jerrycode.gym_services.response.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseWrapper<T> {
    private List<Response<T>> data; // Wrap entire response inside "data"
}
