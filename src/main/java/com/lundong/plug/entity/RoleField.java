package com.lundong.plug.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2024-03-20 10:29
 */
@Data
public class RoleField {

    @JSONField(name = "id")
    private String id;

    @JSONField(name = "name")
    private String name;
}
