//package belote.ex.config.security.auth;
//
//
//import belote.ex.exception.InvalidAccessTokenException;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.AllArgsConstructor;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import token.AccessToken;
//import token.AccessTokenDecoder;
//
//import java.io.IOException;
//import java.util.List;
//
//@AllArgsConstructor
//@Component
//public class AuthenticationRequestFilter extends OncePerRequestFilter {
//
//    private AccessTokenDecoder accessTokenDecoder;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//            throws ServletException, IOException {
//
//        final String requestTokenHeader = request.getHeader("Authorization");
//        if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
//            chain.doFilter(request, response);
//            return;
//        }
//
//        String accessTokenString = requestTokenHeader.substring(7);
//
//        try {
//            AccessToken accessToken = accessTokenDecoder.decode(accessTokenString);
//            setupSpringSecurityContext(accessToken);
//            chain.doFilter(request, response);
//        } catch (InvalidAccessTokenException e) {
//            logger.error("Error validating access token", e);
//            sendAuthenticationError(response);
//        }
//    }
//
//    private void sendAuthenticationError(HttpServletResponse response) throws IOException {
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        response.flushBuffer();
//    }
//
//    private void setupSpringSecurityContext(AccessToken accessToken) {
//        String role = accessToken.getRole();
//        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
//        UserDetails userDetails = new User(accessToken.getSubject(), "",
//                List.of(authority));
//
//        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
//                userDetails, null, userDetails.getAuthorities());
//        usernamePasswordAuthenticationToken.setDetails(accessToken);
//        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
//    }
//
//}