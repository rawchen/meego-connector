package com.lundong.plug.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author shuangquan.chen
 * @date 2024-03-11 09:43
 */
@Data
@Accessors(chain = true)
public class WorkItemField {

    @JSONField(name = "field_type_key")
    private String fieldTypeKey;

    @JSONField(name = "field_alias")
    private String fieldAlias;

    @JSONField(name = "help_description")
    private String helpDescription;

    @JSONField(name = "field_key")
    private String fieldKey;

    @JSONField(name = "field_value")
    private String fieldValue;

}
