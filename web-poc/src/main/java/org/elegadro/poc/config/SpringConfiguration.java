package org.elegadro.poc.config;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.elegadro.poc.service.ServicePackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;

/**
 * @author Taimo Peelo
 */
@Configuration
@Import(PropertyConfig.class)
@ComponentScan(basePackageClasses = {
    ServicePackage.class,
    org.elegadro.rt.service.ServicePackage.class
})
public class SpringConfiguration {
    private final Logger logger = LoggerFactory.getLogger(SpringConfiguration.class);

    private static final String HTTP_CLIENT_2 =
            "Mozilla/5.0 " +
            "(Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko";

    @Bean
    public HttpClient httpClient() throws Exception {
        SslContextFactory sslContextFactory = new SslContextFactory();

        HttpClient hc = new HttpClient(sslContextFactory);
        hc.setAddressResolutionTimeout(6000);
        hc.setConnectTimeout(6000);
        hc.setMaxConnectionsPerDestination(4);

        HttpField ua = new HttpField(HttpHeader.USER_AGENT, HTTP_CLIENT_2);
        hc.setUserAgentField(ua);
        hc.start();

        return hc;
    }

    @Bean
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int cpuCount = Runtime.getRuntime().availableProcessors();

        // Be generous with number of pools ATM, assuming I/O bound computation submission
        executor.setCorePoolSize(2*cpuCount);
        executor.setMaxPoolSize(4*cpuCount);
        executor.setThreadNamePrefix("SMM-Async-");

        return executor;
    }

    @Bean(name="smmAsyncExecutorService")
    public ExecutorService smmAsyncExecutorService() {
        return new ExecutorServiceAdapter(taskExecutor());
    }

    @PreDestroy
    public void preDestroy() {
        // stop httpClient???
        // TODO: ^^^
    }

    private int cpuCount() {
        return Runtime.getRuntime().availableProcessors();
    }
}
