package vn.clone.fahasa_backend.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;

@Setter
public class ResLoginDTO {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("user")
    private UserDTO user;

    public ResLoginDTO(String accessToken, UserDTO user) {
        this.accessToken = accessToken;
        this.user = user;
    }
}