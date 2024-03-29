package com.example.helloworld.resources;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.testing.junit.ResourceTestRule;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.HttpHeaders;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.ClassRule;
import org.junit.Test;

import com.hardhand.wedder.auth.ExampleAuthenticator;
import com.hardhand.wedder.auth.ExampleAuthorizer;
import com.hardhand.wedder.core.User;
import com.hardhand.wedder.resources.ProtectedClassResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public final class ProtectedClassResourceTest {

    private static final BasicCredentialAuthFilter<User> BASIC_AUTH_HANDLER =
        new BasicCredentialAuthFilter.Builder<User>()
            .setAuthenticator(new ExampleAuthenticator())
            .setAuthorizer(new ExampleAuthorizer())
            .setPrefix("Basic")
            .setRealm("SUPER SECRET STUFF")
            .buildAuthFilter();

    @ClassRule
    public static final ResourceTestRule RULE = ResourceTestRule.builder()
        .addProvider(RolesAllowedDynamicFeature.class)
        .addProvider(new AuthDynamicFeature(BASIC_AUTH_HANDLER))
        .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
        .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
        .addProvider(ProtectedClassResource.class)
        .build();

    @Test
    public void testProtectedAdminEndpoint() {
        String secret = RULE.getJerseyTest().target("/protected/admin").request()
            .header(HttpHeaders.AUTHORIZATION, "Basic Y2hpZWYtd2l6YXJkOnNlY3JldA==")
            .get(String.class);
        assertThat(secret).startsWith("Hey there, chief-wizard. It looks like you are an admin.");
    }

    @Test
    public void testProtectedBasicUserEndpoint() {
        String secret = RULE.getJerseyTest().target("/protected").request()
            .header(HttpHeaders.AUTHORIZATION, "Basic Z29vZC1ndXk6c2VjcmV0")
            .get(String.class);
        assertThat(secret).startsWith("Hey there, good-guy. You seem to be a basic user.");
    }

    @Test
    public void testProtectedBasicUserEndpointAsAdmin() {
        String secret = RULE.getJerseyTest().target("/protected").request()
            .header(HttpHeaders.AUTHORIZATION, "Basic Y2hpZWYtd2l6YXJkOnNlY3JldA==")
            .get(String.class);
        assertThat(secret).startsWith("Hey there, chief-wizard. You seem to be a basic user.");
    }

    @Test
    public void testProtectedGuestEndpoint() {
        String secret = RULE.getJerseyTest().target("/protected/guest").request()
            .header(HttpHeaders.AUTHORIZATION, "Basic Z3Vlc3Q6c2VjcmV0")
            .get(String.class);
        assertThat(secret).startsWith("Hey there, guest. You know the secret!");
    }

    @Test
    public void testProtectedBasicUserEndpointPrincipalIsNotAuthorized403() {
        try {
            RULE.getJerseyTest().target("/protected").request()
            .header(HttpHeaders.AUTHORIZATION, "Basic Z3Vlc3Q6c2VjcmV0")
            .get(String.class);
            failBecauseExceptionWasNotThrown(ForbiddenException.class);
        } catch (ForbiddenException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(403);
        }
    }

}
