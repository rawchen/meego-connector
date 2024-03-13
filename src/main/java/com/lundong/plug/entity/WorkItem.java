package com.lundong.plug.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author shuangquan.chen
 * @date 2024-03-11 09:43
 */
@Data
@Accessors(chain = true)
public class WorkItem {

    @JSONField(name = "id")
    private Long id;

    @JSONField(name = "name")
    private String name;

    @JSONField(name = "project_key")
    private String projectKey;

    @JSONField(name = "work_item_status")
    private WorkItemStatus workItemStatus;

    @JSONField(name = "sub_stage")
    private String subStage;

    @JSONField(name = "created_by")
    private String createdBy;

    @JSONField(name = "updated_by")
    private String updatedBy;

    @JSONField(name = "fields")
    List<WorkItemField> workItemFields;


}
