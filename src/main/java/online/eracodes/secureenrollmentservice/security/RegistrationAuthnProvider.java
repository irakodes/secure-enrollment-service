package online.eracodes.secureenrollmentservice.security;

import lombok.RequiredArgsConstructor;
import online.eracodes.secureenrollmentservice.entity.AppUser;
import online.eracodes.secureenrollmentservice.repository.UserRepository;
import online.eracodes.secureenrollmentservice.util.StringsUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class RegistrationAuthnProvider
        implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(@NonNull Authentication authentication) {

        var token = (RegistrationAuthnToken) authentication;

        var email = (String) token.getPrincipal();
        var fullCode = (String) token.getCredentials();

        if (fullCode == null) throw new IllegalArgumentException("Registration code cannot be null");

        if (!StringsUtil.isRegistrationCodeVerified(fullCode)) {
            throw new BadCredentialsException("Invalid registration code checksum");
        }

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!user.isRegistered()) {
            throw new BadCredentialsException("User already registered");
        }

        var registrationCode = fullCode.substring(0, 16);

        if (!passwordEncoder.matches(registrationCode, user.getRegistrationCode())) {
            throw new BadCredentialsException("Invalid secret pass code");
        }

        user.setAuthToken(StringsUtil.tokenGenerator(user));
        user.setRegistered(true);
        userRepository.save(user);

        AppUser userDetails = new AppUser(user);

        return new RegistrationAuthnToken(userDetails);
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return RegistrationAuthnToken.class
                .isAssignableFrom(authentication);
    }
}