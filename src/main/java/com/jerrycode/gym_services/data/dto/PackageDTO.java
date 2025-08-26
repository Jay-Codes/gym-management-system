package com.jerrycode.gym_services.data.dto;

import lombok.*;
import javax.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageDTO {
    private String name;
    private double priceUSD;
    private double priceTZS;
    private int duration;
}
