package vn.clone.fahasa_backend.controller;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import vn.clone.fahasa_backend.config.FahasaProperties;
import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.DTO.LoginDTO;
import vn.clone.fahasa_backend.domain.DTO.UserInfoDTO;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.security.DomainUserDetailsService;
import vn.clone.fahasa_backend.service.AccountService;
import vn.clone.fahasa_backend.util.SecurityUtils;

import static vn.clone.fahasa_backend.util.SecurityUtils.*;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthenticateController {

    private final AccountService accountService;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final JwtDecoder jwtDecoder;

    private final JwtEncoder jwtEncoder;

    private final FahasaProperties fahasaProperties;

    @PostMapping("/login")
    public ResponseEntity<JWTToken> login(@RequestBody @Valid LoginDTO loginDTO) {
        Authentication authenticationToken = UsernamePasswordAuthenticationToken.unauthenticated(loginDTO.getEmail(),
                                                                                                 loginDTO.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject()
                                                                    .authenticate(authenticationToken);

        String accessToken = this.createToken(authentication, true);
        String refreshToken = this.createToken(authentication, false);

        DomainUserDetailsService.UserWithId userWithId = (DomainUserDetailsService.UserWithId) authentication.getPrincipal();
        accountService.addRefreshToken(userWithId.getId(), refreshToken);

        ResponseCookie responseCookie = createCookie(refreshToken, fahasaProperties.getSecurity()
                                                                                   .getAuthentication()
                                                                                   .getJwt()
                                                                                   .getTokenValidityInSecondsForRefreshToken());
        return ResponseEntity.ok()
                             .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                             .body(new JWTToken(accessToken));
    }

    @GetMapping("/account")
    public ResponseEntity<UserInfoDTO> getAccountInfo() {
        String email = SecurityUtils.getCurrentUserLogin()
                                    .orElse("");
        Account account = accountService.getUserInfo(email);
        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setId(account.getId());
        userInfo.setEmail(account.getEmail());
        userInfo.setFirstName(account.getFirstName());
        userInfo.setLastName(account.getLastName());
        userInfo.setPhone(account.getPhone());
        userInfo.setBirthday(account.getBirthday());
        return ResponseEntity.ok().body(userInfo);
    }

    @GetMapping("/refresh")
    public ResponseEntity<JWTToken> refreshAccessToken(@CookieValue(value = "refresh_token") Optional<String> refreshTokenOptional) {
        if (refreshTokenOptional.isEmpty()) {
            throw new BadRequestException("Refresh token required!");
        }
        String refreshToken = refreshTokenOptional.get();
        Jwt decodedToken = jwtDecoder.decode(refreshToken);
        String email = decodedToken.getSubject();

        Account account = accountService.getUserByRefreshToken(refreshToken);
        accountService.deleteRefreshToken(refreshToken);

        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(email, null, List.of(new SimpleGrantedAuthority("admin")));
        String newAccessToken = createToken(authentication, true);
        String newRefreshToken = createToken(authentication, false);

        accountService.addRefreshToken(account.getId(), newRefreshToken);
        ResponseCookie responseCookie = createCookie(newRefreshToken, fahasaProperties.getSecurity()
                                                                                      .getAuthentication()
                                                                                      .getJwt()
                                                                                      .getTokenValidityInSecondsForRefreshToken());
        return ResponseEntity.ok()
                             .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                             .body(new JWTToken(newAccessToken));
    }

    @GetMapping("logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "refresh_token") Optional<String> refreshTokenOptional) {
        if (refreshTokenOptional.isEmpty()) {
            throw new BadRequestException("Refresh token required!");
        }

        accountService.deleteRefreshToken(refreshTokenOptional.get());

        ResponseCookie deleteCookie = createCookie(null, 0);

        return ResponseEntity.status(HttpStatus.CREATED)
                             .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                             .build();
    }

    private String createToken(Authentication authentication, boolean isAccessToken) {
        String authorities = authentication.getAuthorities()
                                           .stream()
                                           .map(GrantedAuthority::getAuthority)
                                           .collect(Collectors.joining(AUTHORITIES_CLAIM_DELIMITER));

        Instant now = Instant.now();
        Instant validity;
        if (isAccessToken) {
            validity = now.plusSeconds(this.fahasaProperties.getSecurity()
                                                            .getAuthentication()
                                                            .getJwt()
                                                            .getTokenValidityInSeconds());
        } else {
            validity = now.plusSeconds(this.fahasaProperties.getSecurity()
                                                            .getAuthentication()
                                                            .getJwt()
                                                            .getTokenValidityInSecondsForRefreshToken());
        }

        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                                                   .issuedAt(now)
                                                   .expiresAt(validity)
                                                   .subject(authentication.getName());
        if (authentication.getPrincipal() instanceof DomainUserDetailsService.UserWithId user) {
            builder.claim(USER_ID_CLAIM, user.getId());
        }
        if (isAccessToken) {
            builder.claim(AUTHORITIES_CLAIM, authorities);
        }

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM)
                                       .type("JWT")
                                       .build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, builder.build()))
                              .getTokenValue();
    }

    private ResponseCookie createCookie(@Nullable String refreshToken, long maxAge) {
        return ResponseCookie.from("refresh_token", refreshToken)
                             .httpOnly(true)
                             .secure(true)
                             .path("/")
                             .maxAge(maxAge)
                             .build();
    }

    /**
     * Object to return as body in JWT Authentication.
     */
    static class JWTToken {
        private String accessToken;

        JWTToken(String accessToken) {
            this.accessToken = accessToken;
        }

        @JsonProperty("access_token")
        String getIdToken() {
            return accessToken;
        }

        void setIdToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}