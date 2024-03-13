package com.lundong.plug.service;

/**
 * @author shuangquan.chen
 * @date 2023-11-28 18:39
 */
public interface TenantAuthService {


    /**
     * 租户KEY判断租户能用的行数
     *
     * @param tenantKey
     * @return
     */
    Long rowNumberLimit(String tenantKey);

}
