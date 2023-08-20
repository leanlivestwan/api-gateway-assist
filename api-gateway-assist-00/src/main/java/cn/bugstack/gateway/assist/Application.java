package cn.bugstack.gateway.assist;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @description 测试启动服务
 * DataSourceAutoConfiguration.class 会自动查找 application.yml 或者 properties 文件里的 spring.datasource.* 相关属性并自动配置单数据源「注意这里提到的单数据源」。
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@Configurable
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
