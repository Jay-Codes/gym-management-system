package com.jerrycode.gym_services.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;


@Configuration
public class AppBeansConfig {

	@Bean(name="threadPoolTaskScheduler")
    public ThreadPoolTaskScheduler threadPoolTaskScheduler(){
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(100);
        threadPoolTaskScheduler.setThreadNamePrefix("gymThreadPoolTaskScheduler");
        
        return threadPoolTaskScheduler;
    }
	
	@Bean(name="threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(){
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(1000);
		threadPoolTaskExecutor.setMaxPoolSize(3000);
		threadPoolTaskExecutor.setQueueCapacity(Integer.MAX_VALUE);
		threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
		threadPoolTaskExecutor.setThreadNamePrefix("gymThreadPoolTaskScheduler");
        
        return threadPoolTaskExecutor;
    }

	@Bean(name = "objectMapper")
	public ObjectMapper getObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return mapper;
	}
	
	@Bean(name="restTemplate")
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	@Bean(name="modelMapper")
	public ModelMapper getModelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		return modelMapper;
	}

	@Bean(name="securedRestTemplate")
	public RestTemplate getSecuredRestTemplate() {
		return new RestTemplateBuilder().basicAuthentication("admin", "password").build();
	}

}
