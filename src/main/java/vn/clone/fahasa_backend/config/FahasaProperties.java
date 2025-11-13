package vn.clone.fahasa_backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fahasa")
@Getter
@Setter
public class FahasaProperties {
    private Security security;

    @Getter
    @Setter
    public static class Security {
        private final Authentication authentication = new Authentication();

        @Getter
        @Setter
        public static class Authentication {
            private final Jwt jwt = new Jwt();

            @Getter
            @Setter
            public static class Jwt {
                private String secret;
                private String base64Secret;
                private long tokenValidityInSeconds;
                private long tokenValidityInSecondsForRefreshToken;
            }
        }
    }
}
