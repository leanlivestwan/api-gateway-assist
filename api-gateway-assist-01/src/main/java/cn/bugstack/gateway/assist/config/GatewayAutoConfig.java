package cn.bugstack.gateway.assist.config;

import cn.bugstack.gateway.assist.application.GatewayApplication;
import cn.bugstack.gateway.assist.service.RegisterGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GatewayServiceProperties.class)
public class GatewayAutoConfig {
    private Logger logger = LoggerFactory.getLogger(GatewayAutoConfig.class);
    @Bean
    public RegisterGatewayService registerGatewayService() {
        return new RegisterGatewayService();
    }
    @Bean
    public GatewayApplication gatewayApplication(GatewayServiceProperties properties, RegisterGatewayService registerGatewayService) {
        return new GatewayApplication(properties, registerGatewayService);
    }

}
