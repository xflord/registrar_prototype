package org.perun.registrarprototype.security.config;

import org.perun.registrarprototype.security.UserInfoTokenIntrospector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    // very basic security configuration, potentially expand with more filters to add unauthorized access handling
    httpSecurity.
        authorizeHttpRequests(authorize -> authorize
                                               .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2
                                            .opaqueToken(opaque -> opaque.introspector(introspector())));
        return httpSecurity.build();
  }

  @Bean
  public OpaqueTokenIntrospector introspector() {
    // replace with spring config options
    return new UserInfoTokenIntrospector("http://localhost:8080/oauth/introspect", "clientId", "clientSecret", "http://localhost:8080/userinfo");
  }
}
