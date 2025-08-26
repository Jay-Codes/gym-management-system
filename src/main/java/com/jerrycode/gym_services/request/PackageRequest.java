package com.jerrycode.gym_services.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Data
public class PackageRequest {
    @NotBlank
    private String name;

    @Positive
    private double priceUSD;

    @Positive
    private double priceTZS;

    @Positive
    private int duration;

    @Override
    public String toString() {
        return "PackageRequest{" +
                "name='" + name + '\'' +
                ", priceUSD=" + priceUSD +
                ", priceTZS=" + priceTZS +
                ", duration=" + duration +
                '}';
    }

}
