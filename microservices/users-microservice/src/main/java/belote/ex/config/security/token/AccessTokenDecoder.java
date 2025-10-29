package belote.ex.config.security.token;

public interface AccessTokenDecoder {
    AccessToken decode(String accessTokenEncoded);
}
