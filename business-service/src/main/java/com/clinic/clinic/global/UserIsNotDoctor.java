package com.clinic.clinic.global;

public class UserIsNotDoctor extends RuntimeException {

    public UserIsNotDoctor(Integer doctorId) {
        super("User with id " + doctorId + " is not a doctor");
    }
}
