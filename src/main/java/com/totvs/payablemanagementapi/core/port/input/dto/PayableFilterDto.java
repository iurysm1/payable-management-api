package com.totvs.payablemanagementapi.core.port.input.dto;

import com.totvs.payablemanagementapi.core.util.DatePeriodCriteria;

public record PayableFilterDto (
    String description,
    DatePeriodCriteria periodCriteria
){
}
