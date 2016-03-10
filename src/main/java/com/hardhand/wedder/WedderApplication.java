package com.hardhand.wedder;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import com.hardhand.wedder.auth.ExampleAuthenticator;
import com.hardhand.wedder.auth.ExampleAuthorizer;
import com.hardhand.wedder.cli.RenderCommand;
import com.hardhand.wedder.core.Person;
import com.hardhand.wedder.core.Template;
import com.hardhand.wedder.core.User;
import com.hardhand.wedder.db.PersonDAO;
import com.hardhand.wedder.filter.DateRequiredFeature;
import com.hardhand.wedder.health.TemplateHealthCheck;
import com.hardhand.wedder.resources.FilteredResource;
import com.hardhand.wedder.resources.HelloWorldResource;
import com.hardhand.wedder.resources.PeopleResource;
import com.hardhand.wedder.resources.PersonResource;
import com.hardhand.wedder.resources.ProtectedResource;
import com.hardhand.wedder.resources.ViewResource;

import java.util.Map;

public class WedderApplication extends Application<WedderConfiguration> {
    public static void main(String[] args) throws Exception {
        new WedderApplication().run(args);
    }

    private final HibernateBundle<WedderConfiguration> hibernateBundle =
            new HibernateBundle<WedderConfiguration>(Person.class) {
                @Override
                public DataSourceFactory getDataSourceFactory(WedderConfiguration configuration) {
                    return configuration.getDataSourceFactory();
                }
            };

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<WedderConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        bootstrap.addCommand(new RenderCommand());
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new MigrationsBundle<WedderConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(WedderConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
        bootstrap.addBundle(hibernateBundle);
        bootstrap.addBundle(new ViewBundle<WedderConfiguration>() {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(WedderConfiguration configuration) {
                return configuration.getViewRendererConfiguration();
            }
        });
    }

    @Override
    public void run(WedderConfiguration configuration, Environment environment) {
        final PersonDAO dao = new PersonDAO(hibernateBundle.getSessionFactory());
        final Template template = configuration.buildTemplate();

        environment.healthChecks().register("template", new TemplateHealthCheck(template));
        environment.jersey().register(DateRequiredFeature.class);
        environment.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
                .setAuthenticator(new ExampleAuthenticator())
                .setAuthorizer(new ExampleAuthorizer())
                .setRealm("SUPER SECRET STUFF")
                .buildAuthFilter()));
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new HelloWorldResource(template));
        environment.jersey().register(new ViewResource());
        environment.jersey().register(new ProtectedResource());
        environment.jersey().register(new PeopleResource(dao));
        environment.jersey().register(new PersonResource(dao));
        environment.jersey().register(new FilteredResource());
    }
}
