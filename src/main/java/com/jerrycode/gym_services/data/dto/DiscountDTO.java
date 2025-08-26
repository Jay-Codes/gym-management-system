package com.jerrycode.gym_services.data.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountDTO {
    private Long id;
    private String name;
    private boolean active;
    private double percentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
