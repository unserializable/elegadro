package org.elegadro.poc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * @author Taimo Peelo
 */
@Configuration
@PropertySource(
    value = {PropertyConfig.APP_CONFIG_LOCATION_PROPERTY},
    ignoreResourceNotFound = true
)
public class PropertyConfig {
    /** System property for defining location of configuration file. */
    public static final String APP_CONFIG_LOCATION_PROPERTY="${elegadro.conf.loc}/elegadro.properties";

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
