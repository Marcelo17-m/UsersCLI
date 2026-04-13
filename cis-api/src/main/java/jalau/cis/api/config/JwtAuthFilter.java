package jalau.cis.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jalau.cis.api.exception.UserNotFoundException;
import jalau.cis.api.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jalau.cis.api.dto.UserResponseDto;
import jalau.cis.api.service.UserService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    @Lazy
    private UserService userService;
    @Autowired
    private SecurityErrorResponseWriter securityErrorResponseWriter;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
    throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            securityErrorResponseWriter.write(request, response, HttpServletResponse.SC_UNAUTHORIZED,
                    "AUTH-401", "Unauthorized access");
            return;
        }

        String login = jwtUtil.extractLogin(token);
        UserResponseDto user;
        try {
            user = userService.findByLogin(login);
        } catch (UserNotFoundException ex) {
            securityErrorResponseWriter.write(request, response, HttpServletResponse.SC_UNAUTHORIZED,
                    "AUTH-401", "Unauthorized access");
            return;
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            securityErrorResponseWriter.write(request, response, HttpServletResponse.SC_FORBIDDEN,
                    "AUTH-403", "Forbidden - Account Inactive");
            return;
        }

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(login, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
