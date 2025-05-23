package master.master.web.rest.dto;

public class AuthResponseDto {
    private final String token;

    public AuthResponseDto(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
