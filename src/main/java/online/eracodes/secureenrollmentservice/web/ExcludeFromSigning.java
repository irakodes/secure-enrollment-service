package online.eracodes.secureenrollmentservice.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be excluded from automatic response signing
 * Used for public/boostrap endpoints that need to return raw responses.
 * See issue at <a href="https://linear.app/iracodes/issue/IRA-142/pinned-root-public-key-trust-anchor">Linear Issue #IRA-142</a>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcludeFromSigning { }
