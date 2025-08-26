package com.jerrycode.gym_services.data.dto;

import com.jerrycode.gym_services.utils.Role;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO  {
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
    private String image;
    private Role role;

}
