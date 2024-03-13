package com.lundong.plug.entity.result;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.util.Map;

/**
 * @author shuangquan.chen
 * @date 2023-11-22 17:09
 */
@Data
public class Record {

    @Alias("primaryID")
    private String primaryID;

    @Alias("data")
    private Map<String, Object> data;
}
