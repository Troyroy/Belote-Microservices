package token;

public interface AccessTokenEncoder {
    String encode(AccessToken accessToken);
}
