package com.wkr.storeserver.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * 避免雪花算法生成的 Long 主键在 JavaScript 中超过安全整数范围后丢失精度。
 */
@Configuration
public class JacksonConfiguration {

    private static final long JAVASCRIPT_MAX_SAFE_INTEGER = 9_007_199_254_740_991L;
    private static final JsonSerializer<Long> JAVASCRIPT_SAFE_LONG_SERIALIZER = new JsonSerializer<>() {
        @Override
        public void serialize(Long value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
            if (value > JAVASCRIPT_MAX_SAFE_INTEGER || value < -JAVASCRIPT_MAX_SAFE_INTEGER) {
                generator.writeString(value.toString());
                return;
            }
            generator.writeNumber(value);
        }
    };

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer javaScriptSafeLongCustomizer() {
        return builder -> builder
                .serializerByType(Long.class, JAVASCRIPT_SAFE_LONG_SERIALIZER)
                .serializerByType(Long.TYPE, JAVASCRIPT_SAFE_LONG_SERIALIZER);
    }
}
