package com.clinic.clinic.Entity.User;

import com.clinic.clinic.role.Role;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private boolean accountLocked;
    private boolean enabled;
    private Specializations specialization;
    private List<String> roles;
}
