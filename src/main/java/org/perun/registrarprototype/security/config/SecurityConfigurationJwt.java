package org.perun.registrarprototype.security.config;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import org.perun.registrarprototype.security.UserInfoJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Profile("jwt")
public class SecurityConfigurationJwt {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .anonymous(AbstractHttpConfigurer::disable) // <-- CRITICAL: Prevents AnonymousAuthenticationToken so that anonymous RegistrarAuthenticationToken can be created
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("forms/me").permitAll()
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .jwkSetUri("https://login.e-infra.cz/oidc/jwk")
                .decoder(jwtDecoder("https://login.e-infra.cz/oidc/jwk"))
                .jwtAuthenticationConverter(customJwtAuthenticationConverter())
            )

        )
        .addFilterAfter(unauthenticatedUserFilter(), AnonymousAuthenticationFilter.class)
        .csrf(AbstractHttpConfigurer::disable) // TODO configure later on
        .sessionManagement(session ->
                               session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // TODO configure later, might be useful when redirecting between forms?

    return http.build();
  }

  // custom decoder to allow at+jwt
  private NimbusJwtDecoder jwtDecoder(String jwkSetUri) {
    return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).jwtProcessorCustomizer((processor -> {
        // This verifier allows 'JWT', 'at+jwt', and null (no type)
        processor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(
            JOSEObjectType.JWT,
            new JOSEObjectType("at+jwt")
        ));
    })).build();
  }

  @Bean
  public UserInfoJwtAuthenticationConverter customJwtAuthenticationConverter() {
    return new UserInfoJwtAuthenticationConverter();
  }

  @Bean
  public UnauthenticatedUserFilter unauthenticatedUserFilter() {
      return new UnauthenticatedUserFilter();
  }
}
