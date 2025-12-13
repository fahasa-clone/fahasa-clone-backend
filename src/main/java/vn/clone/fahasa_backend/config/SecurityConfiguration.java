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
import org.springframework.security.config.core.GrantedAuthorityDefaults;
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

import vn.clone.fahasa_backend.security.AuthoritiesConstants;
import vn.clone.fahasa_backend.util.SecurityUtils;

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
                                           OAuth2JwtSuccessHandler oauth2JwtSuccessHandler) throws Exception {
        String[] whiteList = {
                "/api/auth/login", "/api/accounts/register", "/api/auth/refresh", "/api/accounts/activate",
                "/api/auth/logout", "/login/oauth2/code/**", "/"
        };
        RequestCache nullRequestCache = new NullRequestCache();

        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz.dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR)
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

    /**
     * Configures the role hierarchy for Spring Security authorization.
     * <p>
     * This bean establishes a hierarchical relationship between roles, where a higher-level role
     * automatically inherits the permissions of lower-level roles. This simplifies authorization
     * logic by allowing admin users to access resources restricted to regular users without
     * requiring explicit permission grants.
     * <p>
     * The configured hierarchy is: {@code ADMIN > USER}
     * <p>
     * This means:
     * <ul>
     *   <li>Users with the ADMIN role automatically inherit USER permissions</li>
     *   <li>When checking {@code hasRole("USER")}, an ADMIN user will also pass the check</li>
     *   <li>Role hierarchy is applied across both programmatic and SpEL-based authorization</li>
     * </ul>
     * <p>
     * Example authorization behavior:
     * <pre>
     * &#064;PreAuthorize("hasRole('USER')")
     * public void userOnlyMethod() { }
     *
     * // ADMIN users can access this method due to role hierarchy
     * // USER users can also access this method
     * </pre>
     *
     * @return a {@link RoleHierarchy} instance defining the role inheritance structure
     * @see RoleHierarchy
     * @see RoleHierarchyImpl
     * @see AuthoritiesConstants
     */
    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy(AuthoritiesConstants.ADMIN + " > " + AuthoritiesConstants.USER);
    }

    /**
     * Configures the SpEL (Spring Expression Language) expression handler for method-level security.
     * <p>
     * This bean creates a custom handler that evaluates security expressions in annotations like
     * {@link org.springframework.security.access.prepost.PreAuthorize @PreAuthorize} and
     * {@link org.springframework.security.access.prepost.PostAuthorize @PostAuthorize}.
     * It integrates two critical configurations:
     * <ul>
     *   <li><strong>Role Hierarchy:</strong> Applies the configured role inheritance rules during authorization checks</li>
     *   <li><strong>Role Prefix:</strong> Sets the prefix used by {@code hasRole()} in SpEL expressions</li>
     * </ul>
     * <p>
     * With this configuration:
     * <ul>
     *   <li>{@code @PreAuthorize("hasRole('admin')")} evaluates against authorities with no prefix (empty string)</li>
     *   <li>Role hierarchy is respected, so ADMIN role inherits USER permissions</li>
     *   <li>SpEL functions like {@code hasAuthority()}, {@code hasRole()}, and {@code hasAnyRole()} work correctly</li>
     * </ul>
     * <p>
     * Example usage in controllers:
     * <pre>
     * &#064;PostMapping
     * &#064;PreAuthorize("hasRole('admin')")
     * public ResponseEntity&lt;Author&gt; createAuthor(&#064;RequestBody AuthorRequestDTO dto) {
     *     // Only accessible to users with ADMIN role
     * }
     * </pre>
     *
     * @param roleHierarchy the {@link RoleHierarchy} defining role inheritance relationships
     * @return a {@link MethodSecurityExpressionHandler} configured with role hierarchy and role prefix settings
     * @see DefaultMethodSecurityExpressionHandler
     * @see org.springframework.security.access.prepost.PreAuthorize
     * @see org.springframework.security.access.prepost.PostAuthorize
     * @see SecurityUtils#PREFIX
     */
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        expressionHandler.setDefaultRolePrefix(SecurityUtils.PREFIX);
        return expressionHandler;
    }

    /**
     * Configures the default authority prefix for non-SpEL programmatic security checks.
     * <p>
     * This bean sets the authority prefix to an empty string, which means that role names
     * stored in the JWT token (e.g., "admin", "user") will be treated as-is without any
     * prefix prepending when accessed programmatically via the {@link org.springframework.security.core.Authentication} object.
     * <p>
     * <strong>Note:</strong> This configuration only affects programmatic role checks and does NOT affect
     * SpEL expressions in annotations like {@link org.springframework.security.access.prepost.PreAuthorize @PreAuthorize}.
     * For SpEL expressions, the role prefix is configured separately via
     * {@link DefaultMethodSecurityExpressionHandler#setDefaultRolePrefix}.
     * <p>
     * For example, with this configuration:
     * <ul>
     *   <li>Programmatic check: {@code authentication.getAuthorities()} returns authorities like "admin" and "user"</li>
     *   <li>Database lookup: Role names are stored and retrieved without the "ROLE_" prefix</li>
     * </ul>
     * <p>
     * Without this configuration, Spring Security would prepend the default "ROLE_" prefix to all roles,
     * requiring authorities to be stored as "ROLE_admin" and "ROLE_user" in the database.
     *
     * @return a {@link org.springframework.security.config.core.GrantedAuthorityDefaults} instance
     * configured with an empty prefix from {@link SecurityUtils#PREFIX}
     * @see SecurityUtils#PREFIX
     * @see org.springframework.security.config.core.GrantedAuthorityDefaults
     * @see org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler#setDefaultRolePrefix
     */
    @Bean
    static GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(SecurityUtils.PREFIX);
    }
}
