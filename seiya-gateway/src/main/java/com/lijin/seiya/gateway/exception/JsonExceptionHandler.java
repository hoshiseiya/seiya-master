package com.lijin.seiya.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: lijin
 * @Date: 2022/07/15
 * @Description: 自定义异常处理
 */
@Slf4j
@Order(-1)
@Configuration
public class JsonExceptionHandler implements ErrorWebExceptionHandler {
    //参数1: request response   ex:出现异常时异常对象
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        //1.获取请求、响应对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //2.response是否结束  多个异常处理时候
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        //2.设置响应头类型
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        //3.设置响应状态吗
        int code;
        if (ex instanceof org.springframework.cloud.gateway.support.NotFoundException) {
            code = 404;
            response.setStatusCode(HttpStatus.NOT_FOUND);
        } else if (ex instanceof IllegalStateException) {
            code = 403;
            response.setStatusCode(HttpStatus.FORBIDDEN);
        } else {
            code = 502;
            response.setStatusCode(HttpStatus.BAD_GATEWAY);
        }


        //4.设置响应内容
        int finalCode = code;
        return response
                .writeWith(Mono.fromSupplier(() -> {
                    DataBufferFactory bufferFactory = response.bufferFactory();
                    Map<String, Object> result;
                    if (code == 403) {
                        result = response(finalCode, "token validation failed");
                    } else {
                        result = response(finalCode, this.buildMessage(request, ex));
                    }
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        return bufferFactory.wrap(objectMapper.writeValueAsBytes(result));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return null;
                    }
                }));
    }

    /**
     * 构建异常信息
     *
     * @param request
     * @param ex
     * @return
     */
    private String buildMessage(ServerHttpRequest request, Throwable ex) {
        StringBuilder message = new StringBuilder("Failed to handle request [");
        message.append(request.getMethodValue());
        message.append(" ");
        message.append(request.getURI());
        message.append("]");
        if (ex != null) {
            message.append(": ");
            message.append(ex.getMessage());
        }
        ex.printStackTrace();
        return message.toString();
    }


    /**
     * 构建返回的JSON数据格式
     *
     * @param status       状态码
     * @param errorMessage 异常信息
     * @return
     */
    public static Map<String, Object> response(int status, String errorMessage) {
        Map<String, Object> map = new HashMap<>(3);
        map.put("head", false);
        map.put("code", status);
        map.put("body", errorMessage);
        return map;
    }
}
