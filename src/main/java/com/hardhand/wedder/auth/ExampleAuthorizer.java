package com.hardhand.wedder.auth;

import com.hardhand.wedder.core.User;

import io.dropwizard.auth.Authorizer;

public class ExampleAuthorizer implements Authorizer<User> {

    @Override
    public boolean authorize(User user, String role) {
        if(user.getRoles() != null && user.getRoles().contains(role)) {
            return true;
        }

        return false;
    }
}
