package com.utep.ilink.swim.services.api;

import com.utep.ilink.swim.health.MongoHealthCheck;
import com.utep.ilink.swim.health.RelationalHealthCheck;
import com.utep.ilink.swim.services.controller.OrchestratorController;
import com.utep.ilink.swim.services.controller.SingleRunController;

// import in.vectorpro.dropwizard.swagger.SwaggerBundleConfiguration;
// import in.vectorpro.dropwizard.swagger.SwaggerBundle;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.*;

import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import org.eclipse.jetty.servlets.CrossOriginFilter;

public class ServiceApplication extends Application<ServiceConfiguration> {

        @Override
        public void initialize(Bootstrap<ServiceConfiguration> bootstrap) {
                bootstrap.addBundle(
                                new SwaggerBundle<ServiceConfiguration>() {
                                        @Override
                                        protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
                                                        ServiceConfiguration configuration) {
                                                return configuration.swaggerBundleConfiguration;
                                        }
                                });

                bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(), 
                        new EnvironmentVariableSubstitutor(false)));
        }

        @Override
        public void run(ServiceConfiguration conf, Environment environment) throws Exception {

                // Enable CORS headers
                final FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS",
                                (Class<? extends Filter>) CrossOriginFilter.class);
                cors.setInitParameter("allowedOrigins", "*");
                cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin,Authorization");
                cors.setInitParameter("allowedMethods", "GET,POST,OPTIONS,HEAD");

                // Add URL mapping
                cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

                // Enable CORS for health checks
                FilterRegistration.Dynamic healthCors = environment.getAdminContext().getServletContext().addFilter(
                                "CORS",
                                (Class<? extends Filter>) CrossOriginFilter.class);
                healthCors.setInitParameter("allowedOrigins", "*");
                healthCors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
                healthCors.setInitParameter("allowedMethods", "GET, OPTIONS, HEAD");
                healthCors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/ping");

                // Register service controllers
                environment.jersey().register(new OrchestratorController(conf));
                environment.jersey().register(new SingleRunController(conf));

                // Register healthchecks - Mongo and MySQL
                environment.healthChecks().register("SWIM Database", new MongoHealthCheck(conf.getSWIMDBUser(),
                                conf.getSWIMDBPassword(), conf.getSWIMDBName(), conf.getSWIMDBHost(),
                                conf.getSWIMDBPort(),
                                conf.getSWIMDBAuth()));

                environment.healthChecks().register("Workflow Database", new MongoHealthCheck(conf.getWorkflowDBUser(),
                                conf.getWorkflowDBUser(), conf.getWorkflowDBName(), conf.getWorkflowDBHost(),
                                conf.getWorkflowDBPort(),
                                conf.getWorkflowAuthSource()));

                environment.healthChecks().register("Auth Database", new RelationalHealthCheck(conf.getAuthDBUser(),
                                conf.getAuthDBPassword(), conf.getAuthDBName(), conf.getAuthDBHost(),
                                conf.getAuthDBPort(),
                                conf.getAuthDBDriver()));

                environment.healthChecks().register("User Database", new RelationalHealthCheck(conf.getUserDBUser(),
                                conf.getUserDBPassword(), conf.getUserDBName(), conf.getUserDBHost(),
                                conf.getUserDBPort(),
                                conf.getUserDBDriver()));

        }

        public static void main(String[] args) throws Exception {
                new ServiceApplication().run(args);
        }
}