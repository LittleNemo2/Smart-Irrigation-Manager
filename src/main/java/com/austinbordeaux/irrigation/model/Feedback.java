package com.austinbordeaux.irrigation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "feedback")
public class Feedback extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(length = 40)
    private String submittedBy;

    protected Feedback() {
    }

    public Feedback(String name, String email, String subject, String message, String submittedBy) {
        this.name = name;
        this.email = email;
        this.subject = subject;
        this.message = message;
        this.submittedBy = submittedBy;
    }

    @Override
    public String getDisplayName() {
        return subject + " from " + name;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }
}