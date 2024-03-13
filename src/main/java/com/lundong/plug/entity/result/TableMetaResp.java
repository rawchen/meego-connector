package com.lundong.plug.entity.result;

import com.lundong.plug.entity.result.Field;
import lombok.Data;

import java.util.List;

/**
 * @author shuangquan.chen
 * @date 2023-11-22 14:06
 */
@Data
public class TableMetaResp {

    private String tableName;

    private List<Field> fields;
}
