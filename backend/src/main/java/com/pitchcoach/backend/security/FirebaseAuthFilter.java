package com.pitchcoach.backend.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

    public static final String ATTR_UID = "uid";
    public static final String ATTR_EMAIL = "email";
    public static final String DEFAULT_UID = "local_user";

    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthFilter(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String idToken = authHeader.substring(7).trim();
            try {
                FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
                request.setAttribute(ATTR_UID, decodedToken.getUid());
                request.setAttribute(ATTR_EMAIL, decodedToken.getEmail());
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"Token Firebase không hợp lệ hoặc đã hết hạn\"}");
                return;
            }
        } else {
            // Cho phép truyền header X-User-Id hoặc dùng "local_user" khi test bằng Postman / trình duyệt
            String fallbackUid = request.getHeader("X-User-Id");
            request.setAttribute(ATTR_UID,
                    (fallbackUid != null && !fallbackUid.isBlank()) ? fallbackUid : DEFAULT_UID);
            request.setAttribute(ATTR_EMAIL, "minhkhoa@pitchcoach.app");
        }
        filterChain.doFilter(request, response);
    }
}
