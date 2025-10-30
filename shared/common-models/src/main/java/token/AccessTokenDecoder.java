package token;

public interface AccessTokenDecoder {
    AccessToken decode(String accessTokenEncoded);
}
