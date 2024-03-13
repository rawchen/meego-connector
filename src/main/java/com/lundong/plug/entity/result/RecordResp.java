package com.lundong.plug.entity.result;

import cn.hutool.core.annotation.Alias;
import com.lundong.plug.entity.result.Record;
import lombok.Data;

import java.util.List;

/**
 * @author shuangquan.chen
 * @date 2023-11-22 14:06
 */
@Data
public class RecordResp {

    @Alias("nextPageToken")
    private String nextPageToken;

    @Alias("hasMore")
    private Boolean hasMore;

    @Alias("records")
    private List<Record> records;
}
