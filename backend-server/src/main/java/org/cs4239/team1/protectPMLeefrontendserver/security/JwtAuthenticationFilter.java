package org.cs4239.team1.protectPMLeefrontendserver.security;

import java.io.IOException;
import java.util.stream.Stream;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private AESEncryptionDecryptionTool AESEncryptionDecryptionTool;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static int i = 0;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String requestId = request.getHeader("SessionId");
            String encryptedJwt = getEncryptedJwtFromRequest(request);

            if (requestId == null || !StringUtils.hasText(encryptedJwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            String jwt = AESEncryptionDecryptionTool.decrypt(encryptedJwt, jwtSecret, requestId, "AES/CBC/PKCS5PADDING");

            if (!tokenProvider.validateToken(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            String nric = tokenProvider.getNric(jwt);
            Role role = Role.create(tokenProvider.getRole(jwt));
            String sessionId = tokenProvider.getSessionId(jwt);
            User user = customUserDetailsService.loadUserByUsername(nric);

            if (!user.hasRole(role) || !sessionId.equals(requestId)) {
                throw new JwtException("Invalid JWT.");
            }

            user.setSelectedRole(role);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getEncryptedJwtFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Stream.of(cookies)
                .filter(cookie -> cookie.getName().equals("sessionCookie"))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
