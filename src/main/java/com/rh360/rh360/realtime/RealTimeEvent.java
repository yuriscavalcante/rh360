package com.rh360.rh360.realtime;

import java.time.Instant;

public class RealTimeEvent {
    private RealTimeTopic topic;
    private String action; // "refresh"
    private String userId; // opcional (para users/me)
    private Instant at;

    public RealTimeEvent() {}

    public RealTimeEvent(RealTimeTopic topic, String action, String userId) {
        this.topic = topic;
        this.action = action;
        this.userId = userId;
        this.at = Instant.now();
    }

    public RealTimeTopic getTopic() {
        return topic;
    }

    public void setTopic(RealTimeTopic topic) {
        this.topic = topic;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Instant getAt() {
        return at;
    }

    public void setAt(Instant at) {
        this.at = at;
    }
}

