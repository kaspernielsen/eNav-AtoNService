package org.grad.eNav.atonService.config;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The Boostrap Configuration.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Configuration
public class BootstrapConfig {

    /**
     * <p>
     * Add an HTTP trace repository in memory to be used for the respective
     * actuator.
     * </p>
     * <p>
     * The functionality has been removed by default in Spring Boot 2.2.0. For
     * more info see:
     * </p>
     * <a href="https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.2.0-M3-Release-Notes#actuator-http-trace-and-auditing-are-disabled-by-default">...</a>
     *
     * @return the in memory HTTP trance repository
     */
    @ConditionalOnProperty(value = "management.trace.http.enabled", havingValue = "true")
    @Bean
    public HttpTraceRepository httpTraceRepository() {
        return new InMemoryHttpTraceRepository();
    }

    /**
     * Load Keycloak configuration from application.properties or application.yml
     *
     * On multi-tenant scenarios, Keycloak will defer the resolution of a
     * KeycloakDeployment to the target application at the request-phase.
     *
     * A Request object is passed to the resolver and callers expect a complete
     * KeycloakDeployment. Based on this KeycloakDeployment, Keycloak will
     * resume authenticating and authorizing the request.
     *
     * This is required in a separate configuration according to:
     * https://stackoverflow.com/questions/57957006/unable-to-build-spring-based-project-for-authentication-using-keycloak
     *
     * Otherwise, a circular dependency issues appear during startup.
     *
     * @return The keycloak configuration resolver
     */
    @Bean
    public KeycloakConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

}
