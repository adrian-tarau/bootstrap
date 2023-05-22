package net.microfalx.bootstrap.security.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * A service around user management.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Returns the entity which contains the user information for the user attached to the web session.
     *
     * @return a non-null instance
     * @throws SecurityException if such a user does not exist
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUserName(userDetails.getUsername());
        if (user == null) throw new SecurityException("A user with user name '" + userDetails.getUsername()
                + "' could not be located");
        return user;
    }
}
