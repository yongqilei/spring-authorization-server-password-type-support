package com.xxx.cloud.auth.repository.converter;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Collections;
import java.util.List;

@Converter(autoApply = true)
public class StringToArrayConverter implements AttributeConverter<List<String>, String> {
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return String.join(",", attribute);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (ObjectUtils.isEmpty(dbData)) {
            return Collections.emptyList();
        }
        String[] splitData = dbData.split(",");
        return List.of(splitData);
    }
}
