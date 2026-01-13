package com.rh360.rh360.util;

import jakarta.servlet.http.HttpServletRequest;

public class SecurityUtil {

    public static Long getUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId != null ? (Long) userId : null;
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
