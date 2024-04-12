package com.lundong.plug.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2024-04-10 15:40
 */
@Data
public class ProjectWorkItem {

    @JSONField(name = "name")
    private String name;

    @JSONField(name = "id")
    private String id;

}
