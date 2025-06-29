package com.theoyu.framework.common.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.SneakyThrows;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.theoyu.framework.common.constant.DateConstants;

public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //指定LocalDateTime的序列化和反序列化格式
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DateConstants.DEFAULT_DATETIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DateConstants.DEFAULT_DATETIME_FORMAT)));

        OBJECT_MAPPER.registerModules(javaTimeModule); // 解决 LocalDateTime 的序列化问题

    }

    /**
     *  将对象转换为 JSON 字符串
     * @param obj
     * @return
     */
    @SneakyThrows
    public static String toJsonString(Object obj) {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }

}
