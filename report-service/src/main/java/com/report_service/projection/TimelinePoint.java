package com.report_service.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TimelinePoint {
    LocalDate  getDate();
    BigDecimal getWeightKg();
    Long       getCollectionCount();
}
