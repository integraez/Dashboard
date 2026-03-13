package com.integrationhub.dashboard.config;

import com.integrationhub.dashboard.AuthController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        boolean authenticated = session != null && session.getAttribute(AuthController.AUTHENTICATED_USER) != null;

        if (authenticated) {
            return true;
        }

        String requestUri = request.getRequestURI();
        if (requestUri.contains("/api/")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized\"}");
            return false;
        }

        response.sendRedirect(request.getContextPath() + "/login");
        return false;
    }
}
