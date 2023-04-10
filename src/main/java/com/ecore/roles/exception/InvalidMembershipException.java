package com.ecore.roles.exception;

public class InvalidMembershipException extends RuntimeException {
    public <T> InvalidMembershipException() {
        super("Invalid 'Membership' object. The provided user doesn't belong to the provided team.");
    }
}
