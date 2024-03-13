package com.lundong.plug.entity.param;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-11-22 16:52
 */
@Data
public class ScriptArg {

    @Alias("projectURL")
    private String projectURL;

    @Alias("baseOpenID")
    private String baseOpenID;
}
