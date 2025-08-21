package com.record.myprivateproject.converter;

import com.record.myprivateproject.domain.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {
    @Override
    public String convertToDatabaseColumn(Role attribute) {
        return attribute == null ? null : attribute.toDb();
    }
    @Override
    public Role convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Role.fromDb(dbData);
    }

}
