package com.lundong.plug.entity.param;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-11-22 16:45
 */
@Data
public class BitCommonParam {

    @Alias("datasourceConfig")
    private String datasourceConfig;

}
