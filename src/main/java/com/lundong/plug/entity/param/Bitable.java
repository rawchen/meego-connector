package com.lundong.plug.entity.param;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-11-22 16:50
 */
@Data
public class Bitable {

    @Alias("token")
    private String token;

    @Alias("logID")
    private String logID;

    @Alias("tenantKey")
    private String tenantKey;

}
