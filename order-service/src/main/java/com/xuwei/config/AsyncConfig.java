package com.xuwei.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean("orderAsyncExecutor")
    public Executor orderAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(50);

        executor.setMaxPoolSize(100);

        executor.setQueueCapacity(500);

        executor.setThreadNamePrefix("order-async-");

        executor.setKeepAliveSeconds(60);

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        

        executor.setWaitForTasksToCompleteOnShutdown(true);

        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        
        return executor;
    }

    @Bean("batchAsyncExecutor")
    public Executor batchAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(40);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("batch-async-");
        executor.setKeepAliveSeconds(120);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();
        
        return executor;
    }
}