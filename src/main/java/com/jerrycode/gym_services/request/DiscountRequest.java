package com.jerrycode.gym_services.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
public class DiscountRequest {
    @NotBlank
    private String name;

    @Min(0) @Max(100)
    private double percentage;

    private boolean active;

    @Override
    public String toString() {
        return "DiscountRequest{" +
                "name='" + name + '\'' +
                ", percentage=" + percentage +
                ", active=" + active +
                '}';
    }

}
