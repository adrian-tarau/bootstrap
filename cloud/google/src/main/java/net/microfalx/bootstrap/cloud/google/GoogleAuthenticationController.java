package net.microfalx.bootstrap.cloud.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login/google")
@Slf4j
public class GoogleAuthenticationController {

    @Autowired private GoogleIdentityService googleIdentityService;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private SecurityContextRepository securityContextRepository;
    @Autowired private ApplicationEventPublisher eventPublisher;
    @Autowired private UserDetailsService userDetailsService;

    @PostMapping("/token")
    public ResponseEntity<?> login(@RequestParam("idToken") String idToken, HttpServletRequest request,
                                   HttpServletResponse response) throws Exception {
        GoogleIdToken.Payload payload = googleIdentityService.verify(idToken);
        LOGGER.info("Received Google ID token for user: {}, payload: {}", payload.getEmail(), payload);
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        UserDetails userDetails = null;
        try {
            userDetails = userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            userDetails = new
        }

        Authentication authRequest = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        Authentication authenticated = authenticationManager.authenticate(authRequest);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticated);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);
        eventPublisher.publishEvent(new AuthenticationSuccessEvent(authenticated));

        return ResponseEntity.ok().build();
    }
}
