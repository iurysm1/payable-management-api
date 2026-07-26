package com.totvs.payablemanagementapi.adapter.output.repository.converter;

import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusPayableImportationJpaConverter
        implements AttributeConverter<StatusPayableImportationEnum, Short> {

    @Override
    public Short convertToDatabaseColumn(StatusPayableImportationEnum status) {
        return status == null ? null : (short) status.getCode();
    }

    @Override
    public StatusPayableImportationEnum convertToEntityAttribute(Short code) {
        return code == null ? null : StatusPayableImportationEnum.fromCode(code);
    }
}
