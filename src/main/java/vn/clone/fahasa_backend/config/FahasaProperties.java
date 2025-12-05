package vn.clone.fahasa_backend.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fahasa")
@RequiredArgsConstructor
@Getter
public class FahasaProperties {

    private final Security security;

    private final Mail mail;

    private final Cloudinary cloudinary;

    @RequiredArgsConstructor
    @Getter
    public static class Security {
        private final Authentication authentication;

        @RequiredArgsConstructor
        @Getter
        public static class Authentication {

            private final Jwt jwt;

            @RequiredArgsConstructor
            @Getter
            public static class Jwt {

                private final String secret;

                private final String base64Secret;

                private final long tokenValidityInSeconds;

                private final long tokenValidityInSecondsForRefreshToken;
            }
        }
    }

    @RequiredArgsConstructor
    @Getter
    public static class Mail {

        private final String from;

        private final String baseUrl;
    }

    @RequiredArgsConstructor
    @Getter
    public static class Cloudinary {

        private final String productFolder;
    }
}
