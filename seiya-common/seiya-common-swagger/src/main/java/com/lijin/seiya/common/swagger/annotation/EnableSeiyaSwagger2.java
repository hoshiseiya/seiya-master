package com.lijin.seiya.common.swagger.annotation;

import com.lijin.seiya.common.swagger.config.GatewaySwaggerAutoConfiguration;
import com.lijin.seiya.common.swagger.config.SwaggerAutoConfiguration;
import com.lijin.seiya.common.swagger.support.SwaggerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * * 开启 swagger
 * @author lj
 * @date 2022/7/4 16:01
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnableConfigurationProperties(SwaggerProperties.class)
@Import({ SwaggerAutoConfiguration.class, GatewaySwaggerAutoConfiguration.class })
public @interface EnableSeiyaSwagger2 {

}
