package com.lijin.seiya.gateway.filter;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lijin.seiya.gateway.feign.RemoteUserService;
import com.lijin.seiya.gateway.util.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: lzh
 * @Date: 2021/5/8 15:44
 * @Version: 1.0
 * @Description: token过滤器
 */
@Component
@Slf4j
public class TokenFilter implements GlobalFilter, Ordered {
    @Resource
    RedisUtil redisUtil;

    @Resource
    RemoteUserService remoteUserService;

    /**
     * jwt密钥
     */
    private static final String SIGNING_KEY = "abcdefgh";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String requestURI = exchange.getRequest().getURI().getPath();

        //过滤 swagger 文档，登录，获取公钥，刷新token，图片验证码;
        if (requestURI.contains("v2/api-docs") || requestURI.contains("logIn") || requestURI.contains("verifyUser") || requestURI.contains("findAllSystems") || requestURI.contains("generatePublicKey") || requestURI.contains("refreshToken") || requestURI.contains("getImageVerifyCode") || requestURI.contains("getImageVerifyCode") || requestURI.contains("swagger") || requestURI.contains("doc")) {
            return chain.filter(exchange);
        }
        // 获取出请求头
        String headerToken = request.getHeaders().getFirst("token");
        if (headerToken == null || "".equals(headerToken)) {
            log.error("token : {} 缺失", headerToken);
            try {
                return responseMessage(response, HttpStatus.FORBIDDEN, "{\"head\": true,\"code\": \"403\", \"body\": \"there is no request token\"}");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //验证jwt
        Mono<Void> voidMono = verifyJwt(response, headerToken);
        //jwt验证通过菜单权限访问控制
        if (voidMono != null) {
            //jwt验证失败！
            return voidMono;
        }
        return chain.filter(exchange);
    }

    /**
     * 返回响应信息
     *
     * @param response
     * @param message
     * @return
     */
    private Mono<Void> responseMessage(ServerHttpResponse response, HttpStatus statusCode, String message) {
        response.setStatusCode(statusCode);
        JSONObject jsonObject = JSON.parseObject(message);
        byte[] jsonBytes = JSON.toJSONString(jsonObject).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(jsonBytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 1;
    }


    /**
     * 验证jwt是否过期
     *
     * @param response
     * @param headerToken
     */
    private Mono<Void> verifyJwt(ServerHttpResponse response, String headerToken) {
        Map tokenMap = (Map) redisUtil.get(headerToken);
        if (tokenMap == null) {
            //redis中的token过期
            return responseMessage(response, HttpStatus.FORBIDDEN, "{\"head\": true,\"code\": \"403\", \"body\": \"token be overdue\"}");
        }
        String refreshToken = (String) tokenMap.get("refreshToken");
        try {
            //解析没有异常则表示token验证通过，如有必要可根据自身需求增加验证逻辑
            Claims claims = Jwts.parser()
                    //设置签名的秘钥
                    .setSigningKey(SIGNING_KEY)
                    //设置需要解析的token
                    .parseClaimsJws(refreshToken).getBody();
            //String subject = claims.getSubject();
        } catch (ExpiredJwtException expiredJwtEx) {
            log.error("token : {} 过期", refreshToken);
            /*
              jwt过期后（jwt有效期12小时，redis中的token24小时），先到redis 中取数据，如果取不到就重新登录，
              能取到就重新刷新jwt,并更新redis的原有的refreshToken。
            */
            if (tokenMap != null) {
                //重新刷新jwt生成一个新的token给前端
                String systemCode = (String) tokenMap.get("systemCode");
                String loginName = (String) tokenMap.get("loginName");
                String password = (String) tokenMap.get("password");
                refreshToken = (String) remoteUserService.refreshToken(systemCode + "," + loginName, password);
                //覆盖原有的refreshToken
                tokenMap.put("refreshToken", refreshToken);
                //自动登录的时候延长redis的过期时间为24小时
                redisUtil.set(headerToken, tokenMap);
                redisUtil.expire(headerToken, 24, TimeUnit.HOURS);
                log.info("{}:token自动延期", loginName);
            } else {
                return responseMessage(response, HttpStatus.FORBIDDEN, "{\"head\": true,\"code\": \"403\", \"body\": \"token be overdue\"}");
            }
        } catch (Exception ex) {
            log.error("token : {} 验证失败", headerToken);
            return responseMessage(response, HttpStatus.FORBIDDEN, "{\"head\": true,\"code\": \"403\", \"body\": \"token validation failed\"}");
        }
        return null;
    }

}
