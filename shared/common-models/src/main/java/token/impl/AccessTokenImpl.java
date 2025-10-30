package token.impl;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import token.AccessToken;

@EqualsAndHashCode
@Getter
@AllArgsConstructor
public class AccessTokenImpl implements AccessToken {
    private final String subject;
    private final Integer studentId;
    private final String role;


}
