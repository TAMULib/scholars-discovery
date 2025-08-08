package edu.tamu.scholars.middleware.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.serializer.Deserializer;
import org.springframework.core.serializer.Serializer;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.session.jdbc.config.annotation.web.http.JdbcHttpSessionConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class CustomSessionConfiguration extends JdbcHttpSessionConfiguration {

    private final ObjectMapper objectMapper;

    public CustomSessionConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public JdbcIndexedSessionRepository sessionRepository() {
        
        JdbcIndexedSessionRepository repository = super.sessionRepository();

        repository.setConversionService(createConversionService(objectMapper));

        return repository;
    }

    private DefaultConversionService createConversionService(ObjectMapper objMapper) {

        ObjectMapper copy = objMapper.copy();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        copy.registerModules(SecurityJackson2Modules.getModules(classLoader));

        DefaultConversionService conversionService = new DefaultConversionService();
        conversionService.addConverter(Object.class, byte[].class, new SerializingConverter(new JsonSerializer(copy)));
        conversionService.addConverter(byte[].class, Object.class, new DeserializingConverter(new JsonDeserializer(copy)));

        return conversionService;

    }

    static class JsonSerializer implements Serializer<Object> {

        private final ObjectMapper objectMapper;

        JsonSerializer(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public void serialize(Object object, OutputStream outputStream) throws IOException {
            this.objectMapper.writeValue(outputStream, object);
        }

    }

    static class JsonDeserializer implements Deserializer<Object> {

        private final ObjectMapper objectMapper;

        JsonDeserializer(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public Object deserialize(InputStream inputStream) throws IOException {
            return this.objectMapper.readValue(inputStream, Object.class);
        }

    }
}