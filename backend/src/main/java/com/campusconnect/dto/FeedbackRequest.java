package com.campusconnect.dto;

import com.campusconnect.model.FeedbackCategory;

public class FeedbackRequest {

    private FeedbackCategory category;
    private String message;
    private boolean anonymous;

    public FeedbackCategory getCategory() {
        return category;
    }

    public void setCategory(FeedbackCategory category) {
        this.category = category;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }
}