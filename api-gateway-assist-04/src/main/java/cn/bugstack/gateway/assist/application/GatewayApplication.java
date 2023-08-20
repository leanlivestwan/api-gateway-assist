package cn.bugstack.gateway.assist.application;

import cn.bugstack.gateway.assist.config.GatewayServiceProperties;
import cn.bugstack.gateway.assist.domain.model.aggregates.ApplicationSystemRichInfo;
import cn.bugstack.gateway.assist.domain.model.vo.ApplicationInterfaceMethodVO;
import cn.bugstack.gateway.assist.domain.model.vo.ApplicationInterfaceVO;
import cn.bugstack.gateway.assist.domain.model.vo.ApplicationSystemVO;
import cn.bugstack.gateway.assist.domain.service.GatewayCenterService;
import cn.bugstack.gateway.core.mapping.HttpCommandType;
import cn.bugstack.gateway.core.mapping.HttpStatement;
import cn.bugstack.gateway.core.session.Configuration;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.List;

/**
 * @description 网关应用；与 Spring 链接，调用网关注册和接口拉取
 * 这里会用到Spring提供的监听服务ApplicationListener监听容器刷新时间之后，调用网关注册中心注册上去，重复注册则是标记服务启动
 * Spring提供了一个扩展方法，即ApplicationContextAware接口，实现容器对象的注入。
 */
public class GatewayApplication implements ApplicationContextAware, ApplicationListener<ContextClosedEvent> {
    private Logger logger = LoggerFactory.getLogger(GatewayApplication.class);
    private GatewayServiceProperties properties;
    private GatewayCenterService gatewayCenterService;
    private Configuration configuration;
    private Channel gatewaySocketServerChannel;
    public GatewayApplication(GatewayServiceProperties properties, GatewayCenterService registerGatewayService, Configuration configuration, Channel gatewaySocketServerChannel) {
        this.properties = properties;
        this.gatewayCenterService = registerGatewayService;
        this.configuration = configuration;
        this.gatewaySocketServerChannel = gatewaySocketServerChannel;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        try {
            if (gatewaySocketServerChannel.isActive()) {
                logger.info("应用容器关闭，Api网关服务关闭。localAddress：{}", gatewaySocketServerChannel.localAddress());
                gatewaySocketServerChannel.close();
            }
        } catch (Exception e) {
            logger.error("应用容器关闭，Api网关服务关闭失败", e);
        }
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            // 1. 注册网关服务；每一个用于转换 HTTP 协议泛化调用到 RPC 接口的网关都是一个算力，这些算力需要注册网关配置中心
            gatewayCenterService.doRegister(properties.getAddress(),
                    properties.getGroupId(),
                    properties.getGatewayId(),
                    properties.getGatewayName(),
                    properties.getGatewayAddress());

            // 2. 拉取网关配置；每个网关算力都会在注册中心分配上需要映射的RPC服务信息，包括；系统、接口、方法
            ApplicationSystemRichInfo applicationSystemRichInfo = gatewayCenterService.pullApplicationSystemRichInfo(properties.getAddress(), properties.getGatewayId());
            List<ApplicationSystemVO> applicationSystemVOList = applicationSystemRichInfo.getApplicationSystemVOList();
            for (ApplicationSystemVO system : applicationSystemVOList) {
                List<ApplicationInterfaceVO> interfaceList = system.getInterfaceList();
                for (ApplicationInterfaceVO itf : interfaceList) {
                    // 2.1 创建配置信息加载注册
                    configuration.registryConfig(system.getSystemId(), system.getSystemRegistry(), itf.getInterfaceId(), itf.getInterfaceVersion());
                    List<ApplicationInterfaceMethodVO> methodList = itf.getMethodList();
                    // 2.2 注册系统服务接口信息
                    for (ApplicationInterfaceMethodVO method : methodList) {
                        HttpStatement httpStatement = new HttpStatement(
                                system.getSystemId(),
                                itf.getInterfaceId(),
                                method.getMethodId(),
                                method.getParameterType(),
                                method.getUri(),
                                HttpCommandType.valueOf(method.getHttpCommandType()),
                                method.isAuth());
                        configuration.addMapper(httpStatement);
                        logger.info("网关服务注册映射 系统：{} 接口：{} 方法：{}", system.getSystemId(), itf.getInterfaceId(), method.getMethodId());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("网关服务启动失败，停止服务。{}", e.getMessage(), e);
            throw e;
        }
    }


}
