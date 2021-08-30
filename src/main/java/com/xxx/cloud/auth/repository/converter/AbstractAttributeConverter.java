package com.xxx.cloud.auth.repository.converter;

import javax.persistence.AttributeConverter;

public abstract class AbstractAttributeConverter<X, Y> implements AttributeConverter<X, Y> {
    protected static final String DEFAULT_ARRAY_DELIMITER = ",";
}
