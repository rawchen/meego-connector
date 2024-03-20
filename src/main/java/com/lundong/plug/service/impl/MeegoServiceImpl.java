package com.lundong.plug.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lundong.plug.config.Constants;
import com.lundong.plug.entity.*;
import com.lundong.plug.entity.param.BitCommonContext;
import com.lundong.plug.entity.param.BitCommonParam;
import com.lundong.plug.entity.param.CommonReq;
import com.lundong.plug.entity.param.MeegoParam;
import com.lundong.plug.entity.result.*;
import com.lundong.plug.enums.FormTypeEnum;
import com.lundong.plug.mapper.TenantAuthMapper;
import com.lundong.plug.service.MeegoService;
import com.lundong.plug.service.TenantAuthService;
import com.lundong.plug.util.ArrUtil;
import com.lundong.plug.util.SignUtil;
import com.lundong.plug.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author shuangquan.chen
 * @date 2023-11-22 10:37
 */
@Slf4j
@Service
public class MeegoServiceImpl implements MeegoService {

    @Autowired
    TenantAuthMapper tenantAuthMapper;

    @Autowired
    TenantAuthService tenantAuthService;

    public List<WorkItem> queryWorkItemList(MeegoParam meegoParam, Long lineLimitNumber) {
        List<WorkItem> workItems = SignUtil.workItemList(meegoParam);
        if (workItems.size() >= lineLimitNumber) {
            workItems = workItems.stream().limit(lineLimitNumber).collect(Collectors.toList());
        }
        return workItems;
    }

    @Override
    public TableMetaResp tableMeta(CommonReq req) {
        TableMetaResp resp = new TableMetaResp();
        BitCommonParam params = JSONUtil.toBean(req.getParams(), BitCommonParam.class);
        MeegoParam meegoParam = JSONUtil.toBean(params.getDatasourceConfig(), MeegoParam.class);
        switch (FormTypeEnum.toType(meegoParam.getDataType())) {
            case WORK_ITEM:
                List<Field> stockfields = new ArrayList<>();
                if (StrUtil.isEmpty(meegoParam.getTypeKey())) {
                    resp.setTableName("工作项");
                }
                List<ItemEntity> itemEntities = itemList(req);
                if (!ArrUtil.isEmpty(itemEntities)) {
                    for (ItemEntity itemEntity : itemEntities) {
                        if (itemEntity.getTypeKey().equals(meegoParam.getTypeKey())) {
                            resp.setTableName(itemEntity.getName());
                            break;
                        }
                    }
                }
//                stockfields.add(new Field().setFieldName(resp.getTableName() + "名称").setFieldId("id2").setFieldType(Constants.biTableText).setIsPrimary(false).setDescription(""));
//                stockfields.add(new Field().setFieldName("所属空间").setFieldId("id3").setFieldType(Constants.biTableText).setIsPrimary(false).setDescription(""));
//                stockfields.add(new Field().setFieldName("当前状态").setFieldId("id4").setFieldType(Constants.biTableSingleSelect).setIsPrimary(false).setDescription(""));
//                stockfields.add(new Field().setFieldName("创建人user_key").setFieldId("id5").setFieldType(Constants.biTableSingleSelect).setIsPrimary(false).setDescription(""));
//                stockfields.add(new Field().setFieldName("更新人user_key").setFieldId("id6").setFieldType(Constants.biTableSingleSelect).setIsPrimary(false).setDescription(""));
//                stockfields.add(new Field().setFieldName("状态是否到达").setFieldId("id7").setFieldType(Constants.biTableCheckBox).setIsPrimary(false).setDescription(""));
//                stockfields.add(new Field().setFieldName("状态key").setFieldId("id8").setFieldType(Constants.biTableText).setIsPrimary(false).setDescription(""));
//                stockfields.add(new Field().setFieldName("是否初始状态").setFieldId("id9").setFieldType(Constants.biTableCheckBox).setIsPrimary(false).setDescription(""));
//
//                stockfields.add(new Field().setFieldName("模版").setFieldId("id10").setFieldType(Constants.biTableNum).setIsPrimary(false).setDescription("").setProperty(new Property().setFormatter("0")));
//                stockfields.add(new Field().setFieldName("终止").setFieldId("id11").setFieldType(Constants.biTableCheckBox).setIsPrimary(false).setDescription(""));
//                stockfields.add(new Field().setFieldName("删除").setFieldId("id12").setFieldType(Constants.biTableCheckBox).setIsPrimary(false).setDescription(""));
//                stockfields.add(new Field().setFieldName("描述").setFieldId("id13").setFieldType(Constants.biTableText).setIsPrimary(false).setDescription(""));
//                stockfields.add(new Field().setFieldName("完成状态").setFieldId("id14").setFieldType(Constants.biTableCheckBox).setIsPrimary(false).setDescription(""));
//                stockfields.add(new Field().setFieldName("分组类型").setFieldId("id15").setFieldType(Constants.biTableText).setIsPrimary(false).setDescription(""));
//                stockfields.add(new Field().setFieldName("归属").setFieldId("id16").setFieldType(Constants.biTableText).setIsPrimary(false).setDescription(""));
//                stockfields.add(new Field().setFieldName("优先事项").setFieldId("id17").setFieldType(Constants.biTableSingleSelect).setIsPrimary(false).setDescription(""));
//                stockfields.add(new Field().setFieldName("角色归属").setFieldId("id18").setFieldType(Constants.biTableMultipleSelect).setIsPrimary(false).setDescription(""));
//                stockfields.add(new Field().setFieldName("开始时间").setFieldId("id19").setFieldType(Constants.biTableDate).setIsPrimary(false).setDescription(""));
//                stockfields.add(new Field().setFieldName("标签").setFieldId("id20").setFieldType(Constants.biTableMultipleSelect).setIsPrimary(false).setDescription(""));
//                stockfields.add(new Field().setFieldName("关注人").setFieldId("id21").setFieldType(Constants.biTableMultipleSelect).setIsPrimary(false).setDescription(""));
//                stockfields.add(new Field().setFieldName("WIKI").setFieldId("id22").setFieldType(Constants.biTableText).setIsPrimary(false).setDescription(""));

                stockfields.add(new Field().setFieldName(resp.getTableName() + "ID").setFieldId("id1").setFieldType(Constants.biTableNum).setIsPrimary(true).setDescription("").setProperty(new Property().setFormatter("0")));
                List<WorkItemField> workItemFields = SignUtil.fieldAll(meegoParam);
                List<WorkItemField> collectSomeOne = workItemFields.stream().filter(a -> a.getWorkItemScopes().contains(meegoParam.getTypeKey())).collect(Collectors.toList());
                for (WorkItemField workItemField : collectSomeOne) {
                    stockfields.add(new Field().setFieldName(workItemField.getFieldName()).setFieldType(StringUtil.coventFieldType(workItemField.getFieldTypeKey())).setIsPrimary(false).setDescription(""));
                }
                List<WorkItemField> collectAll = workItemFields.stream().filter(a -> a.getWorkItemScopes().contains("_all")).collect(Collectors.toList());
                for (WorkItemField workItemField : collectAll) {
                    stockfields.add(new Field().setFieldName(workItemField.getFieldName()).setFieldType(StringUtil.coventFieldType(workItemField.getFieldTypeKey())).setIsPrimary(false).setDescription(""));
                }

                List<RoleField> roleFields = SignUtil.fieldAllRole(meegoParam);
                dealRoleFields(roleFields, stockfields);

                stockfields = stockfields.stream().filter(s -> s.getFieldType() != -1).collect(Collectors.toList());
                for (int i = 0; i < stockfields.size(); i++) {
                    if (i == 0) {
                        continue;
                    }
                    stockfields.get(i).setFieldId("id" + (i + 1));
                }
                resp.setFields(stockfields);
                break;
        }
        return resp;
    }

    @Override
    public RecordResp records(CommonReq req) {

        Long lineLimitNumber = 0L;
        log.info("req params: {}", req.getParams());
        BitCommonParam params = JSONUtil.toBean(req.getParams(), BitCommonParam.class);
        MeegoParam meegoParam = JSONUtil.toBean(params.getDatasourceConfig(), MeegoParam.class);
        BitCommonContext context = JSONUtil.toBean(req.getContext(), BitCommonContext.class);
        String tenantKey = context.getTenantKey();
        log.info("records tenantKey: {}", tenantKey);
        log.info("userTenantKey: {}", context.getUserTenantKey());
        TenantAuth tenantAuth = tenantAuthMapper.selectOne(
                new LambdaQueryWrapper<TenantAuth>().eq(TenantAuth::getTenantKey, context.getUserTenantKey()).last("limit 1"));
        if (tenantAuth != null) {
            lineLimitNumber = tenantAuthService.rowNumberLimit(tenantAuth.getTenantKey());
        } else {
            log.error("根据租户获取不到行数规则");
            lineLimitNumber = 100000L;
        }

        // 1工作项列表 2插件工时 3任务工时
        Integer formType = meegoParam.getDataType();

        RecordResp recordResp = new RecordResp();
        switch (FormTypeEnum.toType(formType)) {
            case WORK_ITEM:
                // 封装字段和文本
                List<WorkItem> workItems = queryWorkItemList(meegoParam, lineLimitNumber);
                List<Record> stockRecords = new ArrayList<>();
                List<SpaceEntity> spaceList = spaceList(req);
                List<Field> stockfields = new ArrayList<>();
                stockfields.add(new Field().setFieldId("id1").setFieldKey("id").setFieldType(Constants.biTableNum));
                List<WorkItemField> workItemFields = SignUtil.fieldAll(meegoParam);
                List<WorkItemField> collectSomeOne = workItemFields.stream().filter(a -> a.getWorkItemScopes().contains(meegoParam.getTypeKey())).collect(Collectors.toList());
                for (WorkItemField workItemField : collectSomeOne) {
                    stockfields.add(new Field().setFieldKey(workItemField.getFieldKey()).setFieldType(StringUtil.coventFieldType(workItemField.getFieldTypeKey())).setFieldName(workItemField.getFieldTypeKey()));
                }
                List<WorkItemField> collectAll = workItemFields.stream().filter(a -> a.getWorkItemScopes().contains("_all")).collect(Collectors.toList());
                for (WorkItemField workItemField : collectAll) {
                    stockfields.add(new Field().setFieldKey(workItemField.getFieldKey()).setFieldType(StringUtil.coventFieldType(workItemField.getFieldTypeKey())).setFieldName(workItemField.getFieldTypeKey()));
                }

                List<RoleField> roleFields = SignUtil.fieldAllRole(meegoParam);
                dealRoleFields(roleFields, stockfields);

                stockfields = stockfields.stream().filter(s -> s.getFieldType() != -1).collect(Collectors.toList());
                for (int i = 0; i < stockfields.size(); i++) {
                    if (i == 0) {
                        continue;
                    }
                    stockfields.get(i).setFieldId("id" + (i + 1));
                }

                // 处理所有用户映射
                List<String> userId = new ArrayList<>();
                for (WorkItem workItem : workItems) {
                    userId.add(workItem.getCreatedBy());
                    userId.add(workItem.getUpdatedBy());
                    userId.add(workItem.getDeletedBy());
                    for (WorkItemField workItemField : workItem.getWorkItemFields()) {
                        if ("user".equals(workItemField.getFieldTypeKey())) {
                            userId.add(workItemField.getFieldValue());
                        }
                        if ("role_owners".equals(workItemField.getFieldTypeKey())) {
                            if (workItemField.getFieldValue() != null) {
                                JSONArray roleOwnersJsonArray = JSONArray.parseArray(workItemField.getFieldValue());
                                List<RoleFieldValue> list = roleOwnersJsonArray.toJavaList(RoleFieldValue.class);
                                for (RoleFieldValue roleFieldValue : list) {
                                    if (!ArrUtil.isEmpty(roleFieldValue.getOwners())) {
                                        userId.addAll(roleFieldValue.getOwners());
                                    }
                                }
                            }
                        }
                    }
                }
                List<String> distinctUserIds = userId.stream().filter(a -> StrUtil.isNotEmpty(a) && !"0".equals(a)).distinct().collect(Collectors.toList());
                List<ProjectUser> projectUsers  = SignUtil.user(meegoParam, distinctUserIds);

                for (int i = 0; i < workItems.size(); i++) {
                    Record employeeRecord = new Record();
                    Map<String, Object> map = new HashMap<>();

                    List<WorkItemField> workItemFieldTemps = workItems.get(i).getWorkItemFields();
                    for (int j = 0; j < stockfields.size(); j++) {
                        if ("id".equals(stockfields.get(j).getFieldKey())) {
                            map.put(stockfields.get(j).getFieldId(), workItems.get(i).getId());
                        } else if ("created_at".equals(stockfields.get(j).getFieldKey())) {
                            map.put(stockfields.get(j).getFieldId(), workItems.get(i).getCreatedAt());
                        } else if ("updated_at".equals(stockfields.get(j).getFieldKey())) {
                            map.put(stockfields.get(j).getFieldId(), workItems.get(i).getUpdatedAt());
                        } else if ("deleted_at".equals(stockfields.get(j).getFieldKey())) {
                            map.put(stockfields.get(j).getFieldId(), StringUtil.dealTimestamp(workItems.get(i).getDeletedAt()));
                        } else if ("created_by".equals(stockfields.get(j).getFieldKey())) {
//                            System.out.println(workItems.get(i).getCreatedBy());
                            map.put(stockfields.get(j).getFieldId(), StringUtil.dealUserName(projectUsers, workItems.get(i).getCreatedBy()));
                        } else if ("updated_by".equals(stockfields.get(j).getFieldKey())) {
                            map.put(stockfields.get(j).getFieldId(), StringUtil.dealUserName(projectUsers, workItems.get(i).getUpdatedBy()));
                        } else if ("deleted_by".equals(stockfields.get(j).getFieldKey())) {
                            map.put(stockfields.get(j).getFieldId(), StringUtil.dealUserName(projectUsers, workItems.get(i).getDeletedBy()));
                        } else if ("name".equals(stockfields.get(j).getFieldKey())) {
                            map.put(stockfields.get(j).getFieldId(), workItems.get(i).getName());
                        } else if ("work_item_status".equals(stockfields.get(j).getFieldKey()) || "work_item_type_key".equals(stockfields.get(j).getFieldKey())) {
                            out:
                            for (WorkItemField workItemField : workItemFields) {
                                if (workItemField.getFieldKey().equals("work_item_status") || workItemField.getFieldKey().equals("work_item_type_key")) {
                                    List<Option> options = workItemField.getOptions();
                                    for (Option option : options) {
                                        if (option.getValue().equals(workItems.get(i).getWorkItemStatus().getStateKey())) {
                                            map.put(stockfields.get(j).getFieldId(), option.getLabel());
                                            break out;
                                        }
                                    }
                                }
                            }
                        } else if ("owned_project".equals(stockfields.get(j).getFieldKey())) {
                            String projectKey = workItems.get(i).getProjectKey();
                            for (SpaceEntity spaceEntity : spaceList) {
                                if (spaceEntity.getProjectKey().equals(projectKey)) {
                                    map.put(stockfields.get(j).getFieldId(), spaceEntity.getName());
                                    break;
                                }
                            }
                        } else if ("template_type".equals(stockfields.get(j).getFieldKey())) {
                            map.put(stockfields.get(j).getFieldId(), workItems.get(i).getTemplateType());
                        }
//                        } else if ("work_item_type_key".equals(stockfields.get(j).getFieldKey())) {
//                            map.put(stockfields.get(j).getFieldId(), workItems.get(i).getWorkItemTypeKey());
//                        }
                        for (WorkItemField workItemFieldTemp : workItemFieldTemps) {
                            if (stockfields.get(j).getFieldKey().equals(workItemFieldTemp.getFieldKey())) {
                                if ("user".equals(stockfields.get(j).getFieldName())) {
                                    map.put(stockfields.get(j).getFieldId(), StringUtil.dealUserName(projectUsers, workItemFieldTemp.getFieldValue()));
                                } else if ("multi_user".equals(stockfields.get(j).getFieldName())) {
                                    map.put(stockfields.get(j).getFieldId(), StringUtil.dealUserNameMulti(projectUsers, workItemFieldTemp.getFieldValue()));
                                } else {
                                    map.put(stockfields.get(j).getFieldId(), StringUtil.dealField(workItemFieldTemp));
                                }
                            }
                            if ("role_owners".equals(workItemFieldTemp.getFieldKey())) {
                                List<Field> collect = stockfields.stream().filter(a -> a.getIsRoleField() != null && a.getIsRoleField()).collect(Collectors.toList());
                                for (Field field : collect) {
                                    map.put(field.getFieldId(), StringUtil.dealFieldRole(projectUsers, field.getFieldKey(), workItemFieldTemp));
                                }
                            }
                        }
                    }
                    employeeRecord.setData(map);
                    employeeRecord.setPrimaryID("fid_" + (i + 1));
                    stockRecords.add(employeeRecord);

                }



//                for (int i = 0; i < workItems.size(); i++) {
//                    Record employeeRecord = new Record();
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("id3", "");
//                    String projectKey = workItems.get(i).getProjectKey();
//                    for (SpaceEntity spaceEntity : spaceList) {
//                        if (spaceEntity.getProjectKey().equals(projectKey)) {
//                            map.put("id3", spaceEntity.getName());
//                            break;
//                        }
//                    }
//                    map.put("id1", workItems.get(i).getId());
//                    map.put("id2", workItems.get(i).getName());
//                    map.put("id4", workItems.get(i).getSubStage());
//                    map.put("id5", workItems.get(i).getCreatedBy());
//                    map.put("id6", workItems.get(i).getUpdatedBy());
//                    map.put("id7", workItems.get(i).getWorkItemStatus().getIsArchivedState());
//                    map.put("id8", workItems.get(i).getWorkItemStatus().getStateKey());
//                    map.put("id9", workItems.get(i).getWorkItemStatus().getIsInitState());
//                    map.put("id10", StringUtil.dealField(workItems.get(i).getWorkItemFields(), "template"));
//                    map.put("id11", StringUtil.dealField(workItems.get(i).getWorkItemFields(), "aborted"));
//                    map.put("id12", StringUtil.dealField(workItems.get(i).getWorkItemFields(), "deleted"));
//                    map.put("id13", StringUtil.dealField(workItems.get(i).getWorkItemFields(), "description"));
//                    map.put("id14", StringUtil.dealField(workItems.get(i).getWorkItemFields(), "finish_status"));
//                    map.put("id15", StringUtil.dealField(workItems.get(i).getWorkItemFields(), "group_type"));
//                    map.put("id16", StringUtil.dealField(workItems.get(i).getWorkItemFields(), "owner"));
//                    map.put("id17", StringUtil.dealField(workItems.get(i).getWorkItemFields(), "priority"));
//                    map.put("id18", StringUtil.dealField(workItems.get(i).getWorkItemFields(), "role_owners"));
//                    map.put("id19", StringUtil.dealField(workItems.get(i).getWorkItemFields(), "start_time"));
//                    map.put("id20", StringUtil.dealField(workItems.get(i).getWorkItemFields(), "tags"));
//                    map.put("id21", StringUtil.dealField(workItems.get(i).getWorkItemFields(), "watchers"));
//                    map.put("id22", StringUtil.dealField(workItems.get(i).getWorkItemFields(), "wiki"));
//                    employeeRecord.setData(map);
//                    employeeRecord.setPrimaryID("fid_" + (i + 1));
//                    stockRecords.add(employeeRecord);
//                }
                recordResp.setRecords(stockRecords);
                break;
        }
        return page(recordResp.getRecords(), meegoParam.getPageToken(), meegoParam.getMaxPageSize());
    }

    private void dealRoleFields(List<RoleField> roleFields, List<Field> stockfields) {
        if (!ArrUtil.isEmpty(roleFields)) {
            for (RoleField roleField : roleFields) {
                stockfields.add(new Field().setIsRoleField(true).setFieldKey(roleField.getId()).setFieldType(Constants.biTableText).setFieldName(roleField.getName()));
            }
        }
    }

    @Override
    public R<RecordResp> meegoLogin(MeegoParam meegoParam) {
        try {
//            BitCommonParam params = JSONUtil.toBean(req.getParams(), BitCommonParam.class);
//            MeegoParam meegoParam = JSONUtil.toBean(params.getDatasourceConfig(), MeegoParam.class);
//            String context = req.getContext();
            String login = SignUtil.tokenBody(meegoParam);
            log.info("login: {}", login);

            if (login.startsWith("p-")) {
                String spaceVerify = SignUtil.spaceVerify(meegoParam);
                if (StrUtil.isEmpty(spaceVerify)) {
                    return R.ok();
                } else {
                    return R.fail("同步失败，" + spaceVerify, null);
                }

            } else {
                return R.fail("同步失败，" + login, null);
            }
        } catch (Exception e) {
            log.error("登录异常: ", e);
            return R.fail("同步失败，网络连接异常。", null);
        }

//        log.info("stockEntityList size: {}", stockEntityList.size());
    }

    @Override
    public List<SpaceEntity> spaceList(CommonReq req) {
        try {
            BitCommonParam params = JSONUtil.toBean(req.getParams(), BitCommonParam.class);
            MeegoParam meegoParam = JSONUtil.toBean(params.getDatasourceConfig(), MeegoParam.class);
            String context = req.getContext();
            String token = SignUtil.token(meegoParam);
            String paramDetailJson = "{\n" +
                    "    \"user_key\": \"" + meegoParam.getUserKey() + "\"\n" +
                    "}";
            String resultString = HttpRequest.post(Constants.MEEGO_URL + Constants.PROJECTS)
                    .body(paramDetailJson)
                    .header("X-PLUGIN-TOKEN", token)
                    .header("X-USER-KEY", meegoParam.getUserKey())
                    .execute().body();

//            log.info("金蝶组织列表查询参数: {}", paramDetailJson);
            log.info("spaceList方法空间列表列表查询接口: {}", resultString);
            JSONObject resultObject = JSONObject.parseObject(resultString);
            JSONArray resultArray = resultObject.getJSONArray("data");
            List<String> projectKeys = resultArray.toJavaList(String.class);

            if (!ArrUtil.isEmpty(projectKeys)) {
                List<SpaceEntity> entities = new ArrayList<>();
//                String join = String.join(",", projectKeys);
                String join = JSONObject.toJSONString(projectKeys);
                String spaceDetailJson = "{\n" +
                        "    \"user_key\": \"" + meegoParam.getUserKey() + "\",\n" +
                        "     \"project_keys\": " + join + "\n" +
                        "}";
//                System.out.println(spaceDetailJson);
                String spaceDetailResultString = HttpRequest.post(Constants.MEEGO_URL + Constants.PROJECTS_DETAIL)
                        .body(spaceDetailJson)
                        .header("X-PLUGIN-TOKEN", token)
                        .header("X-USER-KEY", meegoParam.getUserKey())
                        .execute().body();
                log.info("spaceList方法空间详情接口: {}", spaceDetailResultString);
                JSONObject spaceDetailResultObject = JSONObject.parseObject(spaceDetailResultString);
                if (spaceDetailResultObject.getInteger("err_code") == 0) {
                    JSONObject dataObject = spaceDetailResultObject.getJSONObject("data");
                    for (String projectKey : projectKeys) {
                        String simpleName = dataObject.getJSONObject(projectKey).getString("simple_name");
                        String name = dataObject.getJSONObject(projectKey).getString("name");
                        SpaceEntity spaceEntity = new SpaceEntity().setSimpleName(simpleName).setName(name).setProjectKey(projectKey);
                        entities.add(spaceEntity);
                    }
                    return entities;
                }
            }
        } catch (Exception e) {
            log.error("接口调用失败: ", e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<ItemEntity> itemList(CommonReq req) {
        try {
            BitCommonParam params = JSONUtil.toBean(req.getParams(), BitCommonParam.class);
            MeegoParam meegoParam = JSONUtil.toBean(params.getDatasourceConfig(), MeegoParam.class);
            String token = SignUtil.token(meegoParam);
            String itemResultString = HttpRequest.get(Constants.MEEGO_URL + "/open_api/" + meegoParam.getProjectKey() + "/work_item/all-types")
                    .header("X-PLUGIN-TOKEN", token)
                    .header("X-USER-KEY", meegoParam.getUserKey())
                    .execute().body();
            log.info("itemList方法获取空间下工作项类型接口: {}", itemResultString);
            JSONObject resultObject = JSONObject.parseObject(itemResultString);
            if (resultObject.getInteger("err_code") == 0) {
                JSONArray dataArray = resultObject.getJSONArray("data");
                List<ItemEntity> itemEntities = JSONArray.parseArray(JSONArray.toJSONString(dataArray), ItemEntity.class);
                return itemEntities;
            }
        } catch (Exception e) {
            log.error("itemList方法异常", e);
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    public RecordResp page(List<Record> records, String pageToken, String maxPageSizeStr) {
        int maxPageSize;
        int currentPage;
        if (StrUtil.isEmpty(maxPageSizeStr)) {
            maxPageSize = 1000;
        } else {
            maxPageSize = Integer.valueOf(maxPageSizeStr);
        }
        int maxPage = records.size() / maxPageSize;
        RecordResp recordResp = new RecordResp();
        if (StrUtil.isEmpty(pageToken)) {
            currentPage = 1;
        } else {
            currentPage = Integer.valueOf(pageToken);
        }
        if (records.size() / maxPageSize > 0 && currentPage <= maxPage) {
            // 至少有下一页
            recordResp.setNextPageToken(String.valueOf(currentPage + 1));
        }
        recordResp.setHasMore(currentPage <= maxPage);
        recordResp.setRecords(getPageData(records, maxPageSize, currentPage));
        return recordResp;
    }

    public static List<Record> getPageData(List<Record> data, int pageSize, int currentPage) {
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, data.size());
        if (startIndex >= data.size()) {
            return new ArrayList<>();
        }
        return data.subList(startIndex, endIndex);
    }

}
