package com.totvs.payablemanagementapi.adapter.output.repository.converter;

import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationItemEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusPayableImportationItemJpaConverter
        implements AttributeConverter<StatusPayableImportationItemEnum, Short> {

    @Override
    public Short convertToDatabaseColumn(StatusPayableImportationItemEnum status) {
        return status == null ? null : (short) status.getCode();
    }

    @Override
    public StatusPayableImportationItemEnum convertToEntityAttribute(Short code) {
        return code == null ? null : StatusPayableImportationItemEnum.fromCode(code);
    }
}
