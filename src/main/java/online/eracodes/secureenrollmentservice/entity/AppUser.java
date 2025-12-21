package online.eracodes.secureenrollmentservice.entity;

//import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AppUser implements UserDetails {

    private final User user;

    public AppUser(User user) { this.user = user; }

    public Long getUserId() { return user.getId(); }

    public String getEmail() { return user.getEmail(); }

    public String getAuthToken() { return user.getAuthToken(); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() { return user.getRegistrationCode(); }

    @Override
    public String getUsername() { return user.getEmail(); }

    @Override
    public boolean isEnabled() { return user.isEnabled(); }
}