package org.freemountain.operator.common;

import org.freemountain.operator.dtos.BaseStatus;

public interface HasBaseStatus {
    BaseStatus getStatus();

    void setStatus(BaseStatus status);
}
