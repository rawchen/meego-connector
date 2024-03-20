package com.lundong.plug.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lundong.plug.config.Constants;
import com.lundong.plug.entity.ProjectUser;
import com.lundong.plug.entity.RoleFieldValue;
import com.lundong.plug.entity.WorkItemField;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author shuangquan.chen
 * @date 2023-11-24 14:40
 */
@Slf4j
public class StringUtil {

    public static Object dealField(List<WorkItemField> workItemFields, String str) {
        if (StrUtil.isEmpty(str)) {
            return null;

        }
        List<WorkItemField> collect = workItemFields.stream().filter(a -> str.equals(a.getFieldKey())).collect(Collectors.toList());
        if (collect.size() != 1) {
            return null;
        } else {
            WorkItemField field = collect.get(0);
            switch (field.getFieldTypeKey()) {
                case "text":
                case "link":
                    return field.getFieldValue();
                case "date":
                    // 毫秒级别时间戳
                    return Long.valueOf(field.getFieldValue());
                case "schedule":
                    JSONObject object = JSONObject.parseObject(field.getFieldValue());
                    Long startTime = object.getLong("start_time");
                    Long endTime = object.getLong("end_time");
                    String startTimeStr = timestampToYearMonthDayHourMinuteSecond(startTime, "yyyy/MM/dd HH:mm:ss");
                    String endTimeStr = timestampToYearMonthDayHourMinuteSecond(endTime, "yyyy/MM/dd HH:mm:ss");
                    return startTimeStr + "-" + endTimeStr;
                case "number":
                    return Double.valueOf(field.getFieldValue());
                case "work_item_related_select":
                    // 关联工作项
                    return field.getFieldValue();
                case "work_item_related_multi_select":
                    return JSONArray.parseArray(field.getFieldValue(), String.class).stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(","));
                case "signal":
                case "bool":
                case "deleted":
                    return Boolean.valueOf(field.getFieldValue());
                case "radio":
                case "select":
                    JSONObject radioObject = JSONObject.parseObject(field.getFieldValue());
                    return radioObject.getString("label");
                case "multi_select":
                    JSONArray jsonArray = JSONArray.parseArray(field.getFieldValue());
                    List<String> multiSelectList = jsonArray.stream().map(o -> (JSONObject) o)
                            .map(o -> o.getString("label"))
                            .collect(Collectors.toList());
                    return multiSelectList;
                case "user":
                    return field.getFieldValue();
                case "multi_user":
                    if (field.getFieldValue() == null) {
                        return Collections.emptyList();
                    }
                    List<String> stringList = JSONArray.parseArray(field.getFieldValue()).toJavaList(String.class);
                    return stringList;
//                    if (field.getFieldValue() == null) {
//                        return "";
//                    }
//                    return String.join(",", JSONArray.parseArray(field.getFieldValue(), String.class));
                case "multi_text":
                    return field.getFieldValue();
                case "file":
                    JSONObject fileObject = JSONObject.parseObject(field.getFieldValue());
                    return fileObject.getString("name");
                case "aborted":
                    JSONObject abortedObject = JSONObject.parseObject(field.getFieldValue());
                    return abortedObject.getBoolean("is_aborted");
                case "linked_work_item":
                    JSONObject linkedWorkItemObject = JSONObject.parseObject(field.getFieldValue());
                    return linkedWorkItemObject.getString("name");
                case "business":
                case "chat_group":
                case "group_id":
                case "group_type":
                    return field.getFieldValue();
                case "work_item_template":
                    JSONObject workItemTemplateObject = JSONObject.parseObject(field.getFieldValue());
                    return workItemTemplateObject.getInteger("id");
                case "role_owners":
                    if (field.getFieldValue() == null) {
                        return Collections.emptyList();
                    }
                    JSONArray roleOwnersJsonArray = JSONArray.parseArray(field.getFieldValue());
                    List<String> roleOwnersMultiSelectList = roleOwnersJsonArray.stream().map(o -> (JSONObject) o)
                            .map(o -> o.getString("role"))
                            .collect(Collectors.toList());
                    return roleOwnersMultiSelectList;
            }
        }
        return null;
    }

    /**
     * 13位时间戳转自定义格式日期字符串
     *
     * @param instanceOperateTime   13位时间戳
     * @param pattern               yyyy/MM/dd HH:mm:ss
     * @return
     */
    public static String timestampToYearMonthDayHourMinuteSecond(Long instanceOperateTime, String pattern) {
        LocalDateTime operateTime;
        if (StrUtil.isEmpty(String.valueOf(instanceOperateTime)) || String.valueOf(instanceOperateTime).length() != 13) {
            log.error("13位时间戳格式出错：{}", instanceOperateTime);
            operateTime = LocalDateTime.now();
        } else {
            operateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(instanceOperateTime), ZoneId.systemDefault());
        }
        return operateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String yearMonthDayHourMinuteSecondToTimestamp(String instanceOperateTime) {
        LocalDateTime operateTime = LocalDateTime.now();
        try {
            operateTime = LocalDateTime.parse(instanceOperateTime, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        } catch (Exception e) {
            log.error("时间解析出错", e);
        }
        long milliseconds = operateTime.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli();
        return String.valueOf(milliseconds);
    }

    public static Integer coventFieldType(String fieldTypeKey) {
        switch (fieldTypeKey) {
            case "text":
            case "link":
            case "work_item_related_select":
            case "user":
            case "multi_user":
            case "multi_text":
            case "business":
            case "chat_group":
            case "group_id":
            case "group_type":
            case "schedule":
            case "work_item_related_multi_select":
            case "file":
            case "linked_work_item":
            case "owned_project":
            case "multi_file":
            case "_work_item_role":
            case "sub_stage":
            case "template_type":
                return Constants.biTableText;
            case "date":
                // 毫秒级别时间戳
                return Constants.biTableDate;
            case "number":
            case "work_item_template":
                return Constants.biTableNum;
            case "signal":
            case "bool":
            case "deleted":
            case "aborted":
                return Constants.biTableCheckBox;
            case "radio":
            case "select":
            case "work_item_status":
                return Constants.biTableSingleSelect;
            case "multi_select":
            case "role_owners":
                return Constants.biTableMultipleSelect;
            case "compound_field":
            case "vote_boolean":
                return -1;
            default:
                log.error("无该类型字段与之匹配，请检查：{}", fieldTypeKey);
                return 0;
        }

    }

    public static Long dealTimestamp(Long timestamp) {
        if (timestamp == null || timestamp == 0) {
            return null;
        } else {
            return timestamp;
        }
    }

    public static Object dealField(WorkItemField field) {
        if (field == null) {
            return null;
        } else {
            if (field.getFieldValue() == null) {
                return null;
            }
            switch (field.getFieldTypeKey()) {
                case "text":
                case "link":
                case "work_item_related_select":
                case "user":
//                    if (field.getFieldValue() == null) {
//                        return "";
//                    }
//                    return String.join(",", JSONArray.parseArray(field.getFieldValue(), String.class));
                case "multi_text":
                case "business":
                case "chat_group":
                case "group_id":
                case "group_type":
                    // 关联工作项
                    return field.getFieldValue();
                case "date":
                    // 毫秒级别时间戳
                    if (field.getFieldValue() == "0") {
                        return null;
                    }
                    return Long.valueOf(field.getFieldValue());
                case "schedule":
                    JSONObject object = JSONObject.parseObject(field.getFieldValue());
                    Long startTime = object.getLong("start_time");
                    Long endTime = object.getLong("end_time");
                    String startTimeStr = timestampToYearMonthDayHourMinuteSecond(startTime, "yyyy/MM/dd HH:mm:ss");
                    String endTimeStr = timestampToYearMonthDayHourMinuteSecond(endTime, "yyyy/MM/dd HH:mm:ss");
                    return startTimeStr + "-" + endTimeStr;
                case "number":
                    return Double.valueOf(field.getFieldValue());
                case "work_item_related_multi_select":
                    return JSONArray.parseArray(field.getFieldValue(), String.class).stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(","));
                case "signal":
                case "bool":
                case "deleted":
                    return Boolean.valueOf(field.getFieldValue());
                case "radio":
                case "select":
                    JSONObject radioObject = JSONObject.parseObject(field.getFieldValue());
                    return radioObject.getString("label");
                case "multi_select":
                    JSONArray jsonArray = JSONArray.parseArray(field.getFieldValue());
                    return jsonArray.stream().map(o1 -> (JSONObject) o1)
                            .map(o1 -> o1.getString("label"))
                            .collect(Collectors.toList());
                case "multi_user":
                    return JSONArray.parseArray(field.getFieldValue()).toJavaList(String.class);
                case "file":
                case "linked_work_item":
                    JSONObject fileObject = JSONObject.parseObject(field.getFieldValue());
                    return fileObject.getString("name");
                case "aborted":
                    JSONObject abortedObject = JSONObject.parseObject(field.getFieldValue());
                    return abortedObject.getBoolean("is_aborted");
                case "work_item_template":
                    JSONObject workItemTemplateObject = JSONObject.parseObject(field.getFieldValue());
                    return workItemTemplateObject.getInteger("id");
                case "role_owners":
//                    JSONArray roleOwnersJsonArray = JSONArray.parseArray(field.getFieldValue());
//                    return roleOwnersJsonArray.stream().map(o1 -> (JSONObject) o1)
//                            .map(o1 -> o1.getString("role"))
//                            .collect(Collectors.toList());
                case "multi_file":
                    JSONArray multiFileJsonArray = JSONArray.parseArray(field.getFieldValue());
                    List<String> multiFileList = multiFileJsonArray.stream().map(o -> (JSONObject) o)
                            .map(o -> o.getString("url"))
                            .collect(Collectors.toList());
                    return String.join(",", multiFileList);
            }
        }
        return null;
    }

    public static String dealUserName(List<ProjectUser> projectUsers, String s) {
        if (ArrUtil.isEmpty(projectUsers)) {
            return s;
        }
        if (StrUtil.isEmpty(s)) {
            return null;
        } else {
            List<ProjectUser> collect = projectUsers.stream().filter(u -> s.equals(u.getUserKey())).collect(Collectors.toList());
            if (collect.size() != 1) {
                log.error("匹配数量不唯一：{}", collect);
                return s;
            } else {
                return collect.get(0).getNameCn();
            }
        }
    }

    public static Object dealUserNameMulti(List<ProjectUser> projectUsers, String fieldValue) {
        try {
            if (StrUtil.isEmpty(fieldValue)) {
                return null;
            }
            JSONArray jsonArray = JSONArray.parseArray(fieldValue);
            if (ArrUtil.isEmpty(jsonArray)) {
                return null;
            }
            List<String> javaList = jsonArray.toJavaList(String.class);
            if (ArrUtil.isEmpty(javaList)) {
                return null;
            }
            List<ProjectUser> collect = projectUsers.stream().filter(u -> javaList.contains(u.getUserKey())).collect(Collectors.toList());
            if (ArrUtil.isEmpty(collect)) {
                return null;
            }
            List<String> stringList = collect.stream().map(ProjectUser::getNameCn).collect(Collectors.toList());
            return String.join(",", stringList);
        } catch (Exception e) {
            log.error("dealUserNameMulti方法异常：", e);
            return null;
        }
    }

    public static String subLog(String resultStr) {
        return subLog(resultStr, 100);
    }

    public static String subLog(String resultStr, int number) {
        if (StrUtil.isEmpty(resultStr)) {
            return "";
        }
        return resultStr.length() > number ? resultStr.substring(0, number) + "..." : resultStr;
    }

    public static Object dealFieldRole(List<ProjectUser> projectUsers, String fieldId, WorkItemField field) {
        if (fieldId == null) {
            return null;
        }
        if (field == null) {
            return null;
        } else {
            if (field.getFieldValue() == null) {
                return null;
            } else {
                JSONArray roleOwnersJsonArray = JSONArray.parseArray(field.getFieldValue());
                List<RoleFieldValue> list = roleOwnersJsonArray.toJavaList(RoleFieldValue.class);
                for (RoleFieldValue roleFieldValue : list) {
                    if (fieldId.equals(roleFieldValue.getRole())) {
                        if (roleFieldValue.getOwners() == null) {
                            return null;
                        } else {
                            List<String> userName = new ArrayList<>();
                            for (String ownerId : roleFieldValue.getOwners()) {
                                userName.add(dealUserName(projectUsers, ownerId));
                            }
                            return String.join(",", userName);
                        }
                    }
                }
            }
        }
        return null;
    }
}
