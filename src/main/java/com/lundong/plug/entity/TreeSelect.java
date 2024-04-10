package com.lundong.plug.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2024-04-10 14:27
 */
@Data
public class TreeSelect {

    @JSONField(name = "label")
    private String label;

    @JSONField(name = "value")
    private String value;

    @JSONField(name = "children")
    private TreeSelect children;

}
