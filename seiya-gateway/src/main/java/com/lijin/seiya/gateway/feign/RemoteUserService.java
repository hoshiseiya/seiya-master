package com.lijin.seiya.gateway.feign;

import com.lijin.seiya.core.constant.ServiceNameConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author lj
 * @date 2022/7/15 12:32
 */
@FeignClient(contextId = "remoteUserService", value = ServiceNameConstants.LOGIN_SERVICE)
public interface RemoteUserService {
    /**
     * 刷新token令牌
     * @param loginName
     * @param password
     * @return
     */
    @PostMapping("/refreshToken")
    Object refreshToken(@RequestParam("loginName") String loginName, @RequestParam("password") String password);
}
