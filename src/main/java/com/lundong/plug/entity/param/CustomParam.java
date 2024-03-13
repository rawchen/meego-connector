package com.lundong.plug.entity.param;

import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2024-03-13 11:39
 */
@Data
public class CustomParam {

    private Integer dataType;

    private String projectKey;

    private String typeKey;

    private String userKey;

    private String pluginId;

    private String pluginSecret;

}
