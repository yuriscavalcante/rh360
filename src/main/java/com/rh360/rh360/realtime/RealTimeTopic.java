package com.rh360.rh360.realtime;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RealTimeTopic {
    USERS("users"),
    TEAMS("teams"),
    TASKS("tasks"),
    PERMISSIONS("permissions"),
    PERMISSION_TEMPLATES("permission-templates"),
    COMPANY_EXPENSES("company-expenses"),
    USERS_ME("users-me");

    private final String topic;

    RealTimeTopic(String topic) {
        this.topic = topic;
    }

    @JsonValue
    public String topic() {
        return topic;
    }
}

