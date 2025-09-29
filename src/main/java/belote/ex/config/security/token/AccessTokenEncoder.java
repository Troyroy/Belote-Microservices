package belote.ex.config.security.token;

public interface AccessTokenEncoder {
    String encode(AccessToken accessToken);
}
