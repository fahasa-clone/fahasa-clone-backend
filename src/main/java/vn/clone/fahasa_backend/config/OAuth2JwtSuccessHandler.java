package vn.clone.fahasa_backend.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import vn.clone.fahasa_backend.controller.AuthenticateController;
import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.service.AccountService;

@Component
@RequiredArgsConstructor
public class OAuth2JwtSuccessHandler implements AuthenticationSuccessHandler {

    private final FahasaProperties fahasaProperties;

    private final AuthenticateController authenticateController;

    private final AccountService accountService;

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // Extract OAuth2User
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        // Get or create a new account from OAuth2 data
        String email = oauth2User.getAttribute("email");
        Account account = accountService.getOrCreateUser(email, oauth2User);

        // Generate JWT tokens
        Authentication usernamePasswordAuthenticationToken =
                UsernamePasswordAuthenticationToken.authenticated(email, null, List.of(new SimpleGrantedAuthority("admin")));
        String accessToken = authenticateController.createToken(usernamePasswordAuthenticationToken, true);
        String refreshToken = authenticateController.createToken(usernamePasswordAuthenticationToken, false);

        // Save a refresh token to a database
        accountService.addRefreshToken(account, refreshToken);

        // Create a refresh token cookie
        ResponseCookie responseCookie = authenticateController.createCookie(
                refreshToken,
                fahasaProperties.getSecurity()
                                .getAuthentication()
                                .getJwt()
                                .getTokenValidityInSecondsForRefreshToken()
        );

        // Set response headers
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        // Create a response body
        Map<String, String> tokenResponse = new HashMap<>();
        tokenResponse.put("access_token", accessToken);

        // Write JSON response
        // objectMapper.writeValue(response.getWriter(), tokenResponse);
        objectMapper.writeValue(response.getWriter(), AuthenticateController.JWTToken.builder()
                                                                                     .accessToken(accessToken)
                                                                                     .build());
    }
}
