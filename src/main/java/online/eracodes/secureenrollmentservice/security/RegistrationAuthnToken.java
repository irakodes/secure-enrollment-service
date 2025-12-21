package online.eracodes.secureenrollmentservice.security;

import online.eracodes.secureenrollmentservice.entity.AppUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class RegistrationAuthnToken
        extends AbstractAuthenticationToken {

    private final String email;

    // Registration Code field is treated like a password
    private final String registrationCode;
    private final AppUser principal;

    // unauthenticated
    public RegistrationAuthnToken(String email, String registrationCode) {
        super((Collection<? extends GrantedAuthority>) null);
        this.email = email;
        this.registrationCode = registrationCode;
        this.principal = null;
        setAuthenticated(false);
    }

    // authenticated
    public RegistrationAuthnToken(AppUser principal) {
        super(principal.getAuthorities());
        this.email = principal.getEmail();
        this.registrationCode = null;
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return registrationCode;
    }

    @Override
    public Object getPrincipal() {
        return principal != null ? principal : email;
    }
}