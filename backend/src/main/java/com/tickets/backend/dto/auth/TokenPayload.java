package com.tickets.backend.dto.auth;

import java.util.List;

public record TokenPayload(String email, String displayName, List<String> roles) {
}

