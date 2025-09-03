package vn.clone.fahasa_backend.configuration;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
    //    @Value("${hoidanit.jwt.base64-secret}")
    private String jwtKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public JwtEncoder jwtEncoder() {
//        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
//    }
//
//    @Bean
//    public JwtDecoder jwtDecoder() {
//        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
//                                                      .macAlgorithm(SecurityUtil.JWT_ALGORITHM)
//                                                      .build();
//        return token -> {
//            try {
//                return jwtDecoder.decode(token);
//            } catch (Exception e) {
//                System.out.println(">>> JWT error: " + e.getMessage());
//                throw e;
//            }
//        };
//    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("permission");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        String[] whiteList = {"/", "/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/register", "/storage/**",
                              "/favicon.ico", "/api/v1/email/**",
                              "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"};
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(authz ->
                                           authz.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                                                .anyRequest().permitAll())
//            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())
//                                                  .authenticationEntryPoint(customAuthenticationEntryPoint))
            // .exceptionHandling(
            //         exceptions -> exceptions.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()) //401
            //                                 .accessDeniedHandler(new BearerTokenAccessDeniedHandler())) //403
            .formLogin(AbstractAuthenticationFilterConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

//    private SecretKey getSecretKey() {
//        byte[] keyBytes = Base64.from(jwtKey).decode();
//        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
//    }
}
