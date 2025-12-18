package vn.clone.fahasa_backend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

import vn.clone.fahasa_backend.security.AuthoritiesConstants;

/**
 * Meta-annotation that restricts access to admin users only.
 * <p>
 * This annotation can be applied to controller methods and classes to enforce
 * that only users with the ADMIN role can access the annotated resources.
 * <p>
 * Example usage:
 * <pre>
 * &#064;PostMapping
 * &#064;AdminOnly
 * public ResponseEntity<Author> createAuthor(&#064;RequestBody AuthorRequestDTO dto) {
 *     // method body
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "')")
// @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
public @interface AdminOnly {
}
