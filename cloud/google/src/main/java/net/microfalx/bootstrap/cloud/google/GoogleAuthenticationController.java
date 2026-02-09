package net.microfalx.bootstrap.cloud.google;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.lang.SecretUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login/google")
@Slf4j
public class GoogleAuthenticationController {

    @Autowired(required = false) private AuthenticationManager authenticationManager;
    @Autowired(required = false) private SecurityContextRepository securityContextRepository;

    @PostMapping("/token")
    public ResponseEntity<?> login(@RequestParam("idToken") String idToken, HttpServletRequest request,
                                   HttpServletResponse response) throws Exception {
        LOGGER.info("Received Google ID token: {}", SecretUtils.maskSecret(idToken));
        Authentication authRequest = new GoogleIdTokenAuthenticationToken(idToken);

        Authentication authentication = authenticationManager.authenticate(authRequest);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        securityContextRepository.saveContext(context, request, response);
        return ResponseEntity.ok().build();
    }
}
