package com.lundong.plug.entity.result;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author shuangquan.chen
 * @date 2023-11-22 11:11
 */
@Data
@Accessors(chain = true)
public class Field {

    @Alias("description")
    private String description;

    @Alias("fieldID")
    private String fieldId;

    @Alias("fieldName")
    private String fieldName;

    @Alias("fieldType")
    private Integer fieldType;

    @Alias("isPrimary")
    private Boolean isPrimary;

    @Alias("field_key")
    private String fieldKey;

    @Alias("property")
    private Property property;
}
