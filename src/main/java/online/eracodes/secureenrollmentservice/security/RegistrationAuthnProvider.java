package online.eracodes.secureenrollmentservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.eracodes.secureenrollmentservice.entity.AppUser;
import online.eracodes.secureenrollmentservice.repository.UserRepository;
import online.eracodes.secureenrollmentservice.util.StringsUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static online.eracodes.secureenrollmentservice.util.StringsUtil.isRegistrationCodeVerified;
import static online.eracodes.secureenrollmentservice.util.StringsUtil.tokenGenerator;


@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationAuthnProvider
        implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(@NonNull Authentication authentication) {

        var authn = (RegistrationAuthnToken) authentication;

        var email = (String) authn.getPrincipal();
        var fullCode = (String) authn.getCredentials();

        if (fullCode == null) throw new IllegalArgumentException("Registration code cannot be null");

        if (!isRegistrationCodeVerified(fullCode)) {
            throw new BadCredentialsException("Checksum failed");
        }

        log.info("Checksum validation successful");
        log.info("DB Check for user: {}", email);
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        log.debug("User: {}", user);
        log.trace("Pass code: {}", user.getRegistrationCode());

        log.info("User {} exists", email);

        // Check if user is already registered
        if (user.isRegistered()) {
            throw new BadCredentialsException("User already registered");
        }

        var registrationCode = fullCode.substring(0, 16);

        if (!passwordEncoder.matches(registrationCode, user.getRegistrationCode())) {
            throw new BadCredentialsException("Invalid secret pass code");
        }

        user.setAuthToken(tokenGenerator(user));
        user.setRegistered(true);
        user.setEnabled(true);

        userRepository.save(user);

        var userDetails = new AppUser(user);

        return new RegistrationAuthnToken(userDetails);
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return RegistrationAuthnToken.class
                .isAssignableFrom(authentication);
    }
}