package com.totvs.payablemanagementapi.core.port.input.dto;

import com.totvs.payablemanagementapi.core.util.DatePeriodCriteria;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;

public record PayableFilterDto (
    String description,
    DatePeriodCriteria periodCriteria,
    StatusPayableEnum status
){
}
