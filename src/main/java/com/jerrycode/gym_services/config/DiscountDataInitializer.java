package com.jerrycode.gym_services.config;

import com.jerrycode.gym_services.data.dao.DiscountRepository;
import com.jerrycode.gym_services.data.vo.Discount;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Component
public class DiscountDataInitializer {

    private final DiscountRepository discountRepository;

    public DiscountDataInitializer(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @PostConstruct
    public void init() {
        if (discountRepository.count() == 0) {
            Discount none = Discount.builder()
                    .name("None")
                    .percentage(0)
                    .active(false)
                    .build();

            Discount couple = Discount.builder()
                    .name("Couple")
                    .percentage(15)
                    .active(true)
                    .build();

            Discount family = Discount.builder()
                    .name("Family")
                    .percentage(20)
                    .active(true)
                    .build();

            Discount corporate = Discount.builder()
                    .name("Corporate")
                    .percentage(25)
                    .active(true)
                    .build();

            discountRepository.save(none);
            discountRepository.save(couple);
            discountRepository.save(family);
            discountRepository.save(corporate);

            System.out.println("Default discounts initialized successfully");
        }
    }
}