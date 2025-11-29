package vn.clone.fahasa_backend.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestTemplate;

public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth2User = defaultOAuth2UserService.loadUser(userRequest);

        // Check if an email is missing (private on GitHub)
        if (oauth2User.getAttribute("email") == null && "github".equals(userRequest.getClientRegistration()
                                                                                   .getRegistrationId())) {
            String email = fetchGitHubEmail(userRequest.getAccessToken().getTokenValue());

            // Create a new attributes map with email
            Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
            attributes.put("email", email);

            return new DefaultOAuth2User(
                    oauth2User.getAuthorities(),
                    attributes,
                    "id" // GitHub uses "id" as the name attribute key
            );
        }

        return oauth2User;
    }

    private String fetchGitHubEmail(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                entity,
                List.class
        );

        // Find the primary email
        List<Map<String, Object>> emails = response.getBody();
        if (emails != null) {
            for (Map<String, Object> emailData : emails) {
                if (Boolean.TRUE.equals(emailData.get("primary"))) {
                    return (String) emailData.get("email");
                }
            }
            // Fallback to first email if no primary found
            if (!emails.isEmpty()) {
                return (String) emails.get(0).get("email");
            }
        }
        return null;
    }
}
