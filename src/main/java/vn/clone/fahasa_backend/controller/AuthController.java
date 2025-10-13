package vn.clone.fahasa_backend.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;

import vn.clone.fahasa_backend.configuration.CustomUser;
import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.DTO.*;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.service.AccountService;
import vn.clone.fahasa_backend.service.EmailService;
import vn.clone.fahasa_backend.util.SecurityUtils;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AccountService accountService;
    private final EmailService emailService;
    private final SecurityUtils securityUtils;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtDecoder jwtDecoder;


    AuthController(AccountService accountService, EmailService emailService, SecurityUtils securityUtils, AuthenticationManagerBuilder authenticationManagerBuilder, JwtDecoder jwtDecoder) {
        this.accountService = accountService;
        this.emailService = emailService;
        this.securityUtils = securityUtils;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.jwtDecoder = jwtDecoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterDTO user) {
        Account newUser = accountService.registerAccount(user);
        emailService.sendActivationEmail(new ActivationUserDTO(
                newUser.getEmail(),
                newUser.getFirstName(),
                newUser.getLastName(),
                newUser.getActivationKey()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body("Registered account successfully");
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activate(@RequestParam(value = "email") String email,
                                           @RequestParam(value = "key") String key) {
        Optional<Account> accountOptional = accountService.activateRegistration(email, key);
        if (accountOptional.isEmpty()) {
            throw new BadRequestException("Email or activation key is invalid");
        }
        return ResponseEntity.ok("Activated account successfully");
    }


    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> login(@RequestBody @Valid LoginDTO loginDTO) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        CustomUser customUser = (CustomUser) authentication.getPrincipal();
        String accessToken = securityUtils.createAccessToken(customUser.getUserInfo(), List.of("ROLE_USER"));

        String refreshToken = securityUtils.createRefreshToken(customUser.getUserInfo());
        accountService.updateUserToken(customUser.getUsername(), refreshToken);

        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", refreshToken)
                                                      .httpOnly(true)
                                                      .secure(true)
                                                      .path("/")
                                                      .maxAge(securityUtils.getRefreshTokenExpiration())
                                                      .build();

        ResLoginDTO resLoginDTO = new ResLoginDTO(accessToken, customUser.getUserInfo());

        return ResponseEntity.ok()
                             .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                             .body(resLoginDTO);
    }

    @GetMapping("/account")
    public ResponseEntity<UserInfoDTO> getAccountInfo() {
        String email = SecurityUtils.getCurrentUserLogin().orElse("");
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
    public ResponseEntity<ResLoginDTO> refresh(@CookieValue(value = "refresh_token") Optional<String> refreshTokenOptional) {
        if (refreshTokenOptional.isEmpty()) {
            throw new BadRequestException("Refresh token required!");
        }
        String refreshToken = refreshTokenOptional.get();
        Jwt decodedToken = jwtDecoder.decode(refreshToken);
        String email = decodedToken.getSubject();

        Account account = accountService.getUserByRefreshToken(refreshToken, email);
        UserDTO userDTO = new UserDTO();
        userDTO.setId(account.getId());
        userDTO.setEmail(account.getEmail());
        userDTO.setFirstName(account.getFirstName());
        userDTO.setLastName(account.getLastName());

        String newRefreshToken = securityUtils.createRefreshToken(userDTO);

        String newAccessToken = securityUtils.createAccessToken(userDTO, List.of("ROLE_USER"));

        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", newRefreshToken)
                                                      .httpOnly(true)
                                                      .secure(true)
                                                      .path("/")
                                                      .maxAge(securityUtils.getRefreshTokenExpiration())
                                                      .build();

        ResLoginDTO resLoginDTO = new ResLoginDTO(newAccessToken, userDTO);
        return ResponseEntity.ok()
                             .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                             .body(resLoginDTO);
    }

    @GetMapping("logout")
    public ResponseEntity<Void> logout() {
        String email = SecurityUtils.getCurrentUserLogin().orElse("");

        accountService.updateUserToken(email, null);

        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", null)
                                                    .httpOnly(true)
                                                    .secure(true)
                                                    .path("/")
                                                    .maxAge(0)
                                                    .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }
}