package com.example.bankcards.dto.request.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserUpdateRequest (

        String firstName,

        String secondName,

        @Email(message = "Неверный формат Email. Ожидается example@gmail.com")
        String email,

        String password
) {

    public static UserUpdateRequest withPassword(String password) {
        return new UserUpdateRequest(null, null, null, password);
    }

}
