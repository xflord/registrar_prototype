package org.perun.registrarprototype.persistence.jdbc.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class JsonbToMapConverter implements Converter<PGobject, Map<String, String>> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public Map<String, String> convert(PGobject source) {
    try {
      return objectMapper.readValue(source.getValue(), new TypeReference<Map<String, String>>() {});
    } catch (Exception e) {
      throw new IllegalStateException("Failed to convert JSON string to Map", e);
    }
  }
}
