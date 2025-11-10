package com.tickets.backend.controller;

import com.tickets.backend.dto.user.ManagedVenueDto;
import com.tickets.backend.dto.user.MeResponse;
import com.tickets.backend.model.User;
import com.tickets.backend.service.CurrentUserService;
import com.tickets.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    private final CurrentUserService currentUserService;
    private final UserService userService;

    public UserController(CurrentUserService currentUserService, UserService userService) {
        this.currentUserService = currentUserService;
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MeResponse> me() {
        User user = currentUserService.requireCurrentUser();
        List<String> roles = userService.getRoleNames(user);
        List<ManagedVenueDto> managedVenues = userService.getManagedVenues(user.getId()).stream()
            .map(venue -> new ManagedVenueDto(venue.getId(), venue.getName()))
            .toList();
        MeResponse response = new MeResponse(
            user.getId(),
            user.getEmail(),
            user.getDisplayName(),
            roles,
            managedVenues
        );
        return ResponseEntity.ok(response);
    }
}
