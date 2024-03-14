package com.lundong.plug.entity.result;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author shuangquan.chen
 * @date 2024-03-13 16:38
 */
@Data
@Accessors(chain = true)
public class Property {

    @Alias("formatter")
    private String formatter;
}
