package com.lundong.plug.entity;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.util.List;

/**
 * @author shuangquan.chen
 * @date 2024-04-25 18:15
 */
@Data
public class WorkItemPage {

    @Alias("nextPageToken")
    private String nextPageToken;

    @Alias("hasMore")
    private Boolean hasMore;

    @Alias("workItemList")
    List<WorkItem> workItemList;

}
