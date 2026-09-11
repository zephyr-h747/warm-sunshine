package com.eldercare.core.config;

import com.github.pagehelper.PageHelper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * MyBatis + PageHelper 配置
 * <p>
 * PageHelper 通过 pagehelper-spring-boot-starter 自动注册拦截器，
 * 此处补充兜底配置：开启驼峰转换校验与分页合理化参数。
 */
@Configuration
public class MyBatisConfig {

    /**
     * MyBatis 全局配置定制：映射下划线转驼峰
     */
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            configuration.setMapUnderscoreToCamelCase(true);
            configuration.setCacheEnabled(true);
        };
    }

    /**
     * PageHelper 兜底属性（与 application.yml 中 pagehelper 配置等价，
     * yml 已通过 starter 自动装配生效，此处仅作为代码级备忘）
     */
    public static Properties pageHelperProperties() {
        Properties props = new Properties();
        props.setProperty("helperDialect", "mysql");
        props.setProperty("reasonable", "true");
        props.setProperty("supportMethodsArguments", "true");
        props.setProperty("params", "count=countSql");
        return props;
    }
}
