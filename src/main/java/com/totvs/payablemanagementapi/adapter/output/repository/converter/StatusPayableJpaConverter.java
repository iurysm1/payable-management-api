package com.totvs.payablemanagementapi.adapter.output.repository.converter;

import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusPayableJpaConverter implements AttributeConverter<StatusPayableEnum, Short> {

    @Override
    public Short convertToDatabaseColumn(StatusPayableEnum status) {
        return status == null ? null : (short) status.getCode();
    }

    @Override
    public StatusPayableEnum convertToEntityAttribute(Short code) {
        return code == null ? null : StatusPayableEnum.fromCode(code);
    }
}
