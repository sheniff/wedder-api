package com.hardhand.wedder.auth;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.hardhand.wedder.core.User;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ExampleAuthenticator implements Authenticator<BasicCredentials, User> {
    /**
     * Valid users with mapping user -> roles
     */
    private static final Map<String, ImmutableSet<? extends Object>> VALID_USERS = ImmutableMap.of(
        "guest", ImmutableSet.of(),
        "good-guy", ImmutableSet.of("BASIC_GUY"),
        "chief-wizard", ImmutableSet.of("ADMIN", "BASIC_GUY")
    );

    @Override
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
        if (VALID_USERS.containsKey(credentials.getUsername()) && "secret".equals(credentials.getPassword())) {
            return Optional.of(new User(credentials.getUsername(), (Set<String>) VALID_USERS.get(credentials.getUsername())));
        }
        return Optional.empty();
    }
}
