package com.nikhil.habit_money.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {

    private String firstName;
    private String lastName;
    private String email;
}
