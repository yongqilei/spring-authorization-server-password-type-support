package com.xxx.cloud.auth.repository.converter;


import org.springframework.security.oauth2.core.AuthorizationGrantType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter(autoApply = true)
public class AuthorizationGrantTypeConverter extends AbstractAttributeConverter<List<AuthorizationGrantType>, String>
        implements AttributeConverter<List<AuthorizationGrantType>, String> {

    @Override
    public String convertToDatabaseColumn(List<AuthorizationGrantType> attribute) {
        return attribute.stream().map(AuthorizationGrantType::getValue).collect(Collectors.joining(DEFAULT_ARRAY_DELIMITER));
    }

    @Override
    public List<AuthorizationGrantType> convertToEntityAttribute(String dbData) {
        String[] split = dbData.split(DEFAULT_ARRAY_DELIMITER);
        return Arrays.stream(split).map(AuthorizationGrantType::new).collect(Collectors.toList());
    }
}
