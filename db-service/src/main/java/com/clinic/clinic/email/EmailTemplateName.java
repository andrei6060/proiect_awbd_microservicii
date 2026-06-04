package com.clinic.clinic.email;

import lombok.Getter;

@Getter
public enum EmailTemplateName {
    ACTIVATE_ACCOUNT("activate_account"),
    ACTIVATE_DOCTOR("activate_doctor_account"),
    ;


    private final String templateName;

    EmailTemplateName(String activateAccount) {
        this.templateName = activateAccount;
    }
}
