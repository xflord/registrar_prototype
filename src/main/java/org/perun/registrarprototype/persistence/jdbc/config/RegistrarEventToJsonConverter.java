package org.perun.registrarprototype.persistence.jdbc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.sql.JDBCType;
import org.jspecify.annotations.Nullable;
import org.perun.registrarprototype.services.events.RegistrarEvent;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.mapping.JdbcValue;

@WritingConverter
public class RegistrarEventToJsonConverter implements Converter<RegistrarEvent, JdbcValue> {
  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(
      SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  @Override
  public @Nullable JdbcValue convert(RegistrarEvent source) {
    try {
      return JdbcValue.of(objectMapper.writeValueAsString(source), JDBCType.OTHER);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to convert Map to JSONB string", e);
    }
  }
}
