package org.perun.registrarprototype.persistence.jdbc.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

@Configuration
@EnableJdbcAuditing
public class JdbcConfig extends AbstractJdbcConfiguration {
  @Override
  protected List<?> userConverters() {
    return Arrays.asList(
        new JsonbToMapConverter(),
        new MapToJsonbConverter(),
        new RegistrarEventToJsonConverter(),
        new CurrentUserToJsonbConverter()
    );
  }
}
