package cn.bugstack.gateway.assist.application;

import cn.bugstack.gateway.assist.config.GatewayServiceProperties;
//import cn.bugstack.gateway.assist.service.RegisterGatewayService;
import cn.bugstack.gateway.assist.domain.model.aggregates.ApplicationSystemRichInfo;
import cn.bugstack.gateway.assist.domain.service.GatewayCenterService;
import com.alibaba.fastjson.JSON;
import org.springframework.context.ApplicationListener;

import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @description 网关应用；与 Spring 链接，调用网关注册和接口拉取
 * 这里会用到Spring提供的监听服务ApplicationListener监听容器刷新时间之后，调用网关注册中心注册上去，重复注册则是标记服务启动
 */
public class GatewayApplication implements ApplicationListener<ContextRefreshedEvent> {
    private GatewayServiceProperties properties;
    private GatewayCenterService gatewayCenterService;
    public GatewayApplication(GatewayServiceProperties properties, GatewayCenterService registerGatewayService) {
        this.properties = properties;
        this.gatewayCenterService = registerGatewayService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        // 1. 注册网关服务；每一个用于转换 HTTP 协议泛化调用到 RPC 接口的网关都是一个算力，这些算力需要注册网关配置中心
        gatewayCenterService.doRegister(properties.getAddress(),
                properties.getGroupId(),
                properties.getGatewayId(),
                properties.getGatewayName(),
                properties.getGatewayAddress());
        // 拉取网关配置：每个网关都会在注册中心分配所需要映射的RPC服务，包括系统、
        // 2. 拉取网关配置；每个网关算力都会在注册中心分配上需要映射的RPC服务信息，包括；系统、接口、方法
        ApplicationSystemRichInfo applicationSystemRichInfo = gatewayCenterService.pullApplicationSystemRichInfo(properties.getAddress(), properties.getGatewayId());
        System.out.println(JSON.toJSONString(applicationSystemRichInfo));
    }
}
