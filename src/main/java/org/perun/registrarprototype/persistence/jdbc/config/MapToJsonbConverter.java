package org.perun.registrarprototype.persistence.jdbc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.JDBCType;
import java.sql.SQLType;
import java.util.Map;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.dialect.JdbcPostgresDialect;
import org.springframework.data.jdbc.core.mapping.JdbcValue;

@WritingConverter
public class MapToJsonbConverter implements Converter<Map<String, String>, JdbcValue> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public JdbcValue convert(Map<String, String> source) {
    try {
      System.out.println("CONVERTING MAP");
      return JdbcValue.of(objectMapper.writeValueAsString(source), JDBCType.OTHER);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to convert Map to JSONB string", e);
    }
  }
}
