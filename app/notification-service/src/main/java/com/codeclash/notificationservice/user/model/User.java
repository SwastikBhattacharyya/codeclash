package com.codeclash.notificationservice.user.model;

public record User(
    Id id, Name name, Email email, Verified verified, VerificationToken verificationToken) {}
