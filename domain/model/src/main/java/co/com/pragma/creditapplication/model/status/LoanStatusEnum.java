package co.com.pragma.creditapplication.model.status;

import lombok.Getter;

@Getter
public enum LoanStatusEnum {

    PENDING_REVIEW("PENDIENTE"),
    APPROVED("APROBADO"),
    REJECTED("RECHAZADO"),
    MANUAL_REVIEW("REVISION MANUAL");

    private final String name;

    LoanStatusEnum(String name) {
        this.name = name;
    }

}
