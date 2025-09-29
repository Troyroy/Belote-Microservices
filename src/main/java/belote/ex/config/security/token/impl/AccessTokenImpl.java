package belote.ex.config.security.token.impl;

import belote.ex.config.security.token.AccessToken;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@EqualsAndHashCode
@Getter
@AllArgsConstructor
public class AccessTokenImpl implements AccessToken {
    private final String subject;
    private final Integer studentId;
    private final String role;


}
