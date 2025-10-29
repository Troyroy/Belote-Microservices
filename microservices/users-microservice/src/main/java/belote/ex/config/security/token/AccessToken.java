package belote.ex.config.security.token;


public interface AccessToken {
    String getSubject();

    Integer getStudentId();

    String getRole();

}
