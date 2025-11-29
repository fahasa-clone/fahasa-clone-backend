package vn.clone.fahasa_backend.config;

import java.util.Arrays;
import java.util.List;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    // CORS configuration
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4173", "http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "x-no-retry"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // How long the response from a pre-flight request can be cached by clients

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply this configuration to all paths
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CustomAuthenticationEntryPoint authenticationEntryPoint,
                                           OAuth2JwtSuccessHandler oauth2JwtSuccessHandler)
            throws Exception {
        String[] whiteList = {
                "/api/auth/login", "/api/accounts/register", "/api/auth/refresh", "/api/accounts/activate",
                "/api/auth/logout", "/login/oauth2/code/**", "/"
        };
        RequestCache nullRequestCache = new NullRequestCache();

        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz.dispatcherTypeMatchers(DispatcherType.ERROR)
                                                 .permitAll()
                                                 .requestMatchers(whiteList)
                                                 .permitAll()
                                                 .requestMatchers(HttpMethod.GET, "/api/categories/**", "/api/books/**")
                                                 .permitAll()
                                                 .anyRequest()
                                                 .authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())
                                                  .authenticationEntryPoint(authenticationEntryPoint))
            .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint)
                                                       .accessDeniedHandler(new BearerTokenAccessDeniedHandler())) // 403
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .requestCache((cache) -> cache.requestCache(nullRequestCache))
            .oauth2Login(oauth2 -> oauth2.successHandler(oauth2JwtSuccessHandler)
                                         .userInfoEndpoint(config -> config.userService(new CustomOAuth2UserService())));
        return http.build();
    }

    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ADMIN > USER");
    }

    // and, if using pre-post method security also add
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        return expressionHandler;
    }
}
