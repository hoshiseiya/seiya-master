package com.lijin.seiya.core.util;

import com.lijin.seiya.core.constant.CommonConstants;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author lj
 * @date 2022/7/8 15:23
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SeiyaResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private int code;

    @Getter
    @Setter
    private String msg;

    @Getter
    @Setter
    private T data;

    public static <T> SeiyaResult<T> ok() {
        return restResult(null, CommonConstants.SUCCESS, null);
    }

    public static <T> SeiyaResult<T> ok(T data) {
        return restResult(data, CommonConstants.SUCCESS, null);
    }

    public static <T> SeiyaResult<T> ok(T data, String msg) {
        return restResult(data, CommonConstants.SUCCESS, msg);
    }

    public static <T> SeiyaResult<T> failed() {
        return restResult(null, CommonConstants.FAIL, null);
    }

    public static <T> SeiyaResult<T> failed(String msg) {
        return restResult(null, CommonConstants.FAIL, msg);
    }

    public static <T> SeiyaResult<T> failed(T data) {
        return restResult(data, CommonConstants.FAIL, null);
    }

    public static <T> SeiyaResult<T> failed(T data, String msg) {
        return restResult(data, CommonConstants.FAIL, msg);
    }

    private static <T> SeiyaResult<T> restResult(T data, int code, String msg) {
        SeiyaResult<T> apiResult = new SeiyaResult<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }

}
