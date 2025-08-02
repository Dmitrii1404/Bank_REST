package com.example.bankcards.controller;


import com.example.bankcards.dto.request.user.UserUpdatePassword;
import com.example.bankcards.dto.request.user.UserUpdateRequest;
import com.example.bankcards.dto.response.block.BlockResponse;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.security.UserDetailsCustom;
import com.example.bankcards.service.RequestBlockService;
import com.example.bankcards.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final RequestBlockService requestBlockService;

    @PutMapping("/update_password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal UserDetailsCustom userDetailsCustom,
            @RequestBody UserUpdatePassword newPassword
    ) {
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.withPassword(newPassword.newPassword());
        User user = userDetailsCustom.getUser();

        userService.updateUser(user.getId(), userUpdateRequest);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/request_block")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<BlockResponse>> requestBlock(
            @AuthenticationPrincipal UserDetailsCustom userDetailsCustom,
            Pageable pageable
    ) {
        return ResponseEntity.ok(requestBlockService.findRequestByUserId(userDetailsCustom.getUser().getId(), pageable));
    }
}
