package com.rh360.rh360.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

public class SecurityUtil {

    public static UUID getUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId != null ? (UUID) userId : null;
    }

    public static String getEmail(HttpServletRequest request) {
        Object email = request.getAttribute("email");
        return email != null ? (String) email : null;
    }

    public static String getRole(HttpServletRequest request) {
        Object role = request.getAttribute("role");
        return role != null ? (String) role : null;
    }
}
