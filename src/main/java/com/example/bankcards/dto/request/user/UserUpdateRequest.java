package com.example.bankcards.dto.request.user;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserUpdateRequest (
        String firstName,
        String secondName,
        String email,
        String password
) {

    public static UserUpdateRequest withPassword(String password) {
        return new UserUpdateRequest(null, null, null, password);
    }

}
