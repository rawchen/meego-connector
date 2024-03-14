package com.lundong.plug.config;

/**
 * @author shuangquan.chen
 * @date 2023-11-22 09:57
 */
public class Constants {

    public final static Integer biTableText             = 1;    //多行文本
    public final static Integer biTableNum              = 2;    //数字
    public final static Integer biTableSingleSelect     = 3;    //单选
    public final static Integer biTableMultipleSelect   = 4;    //多选
    public final static Integer biTableDate             = 5;    //日期
    public final static Integer biTableBarcode          = 6;    //条码
    public final static Integer biTableCheckBox         = 7;    //复选框
    public final static Integer biTableCurrency         = 8;    //货币
    public final static Integer biTablePhone            = 9;    //电话号码
    public final static Integer biTableLink             = 10;   //超链接

    public final static String SECRET_KEY = "ldkj6666";

    public final static String MEEGO_URL = "https://project.feishu.cn";

    public final static String PLUGIN_TOKEN = "/bff/v2/authen/plugin_token";

    public static final String PROJECTS = "/open_api/projects";

    public static final String PROJECTS_DETAIL = "/open_api/projects/detail";

    public static final String WORK_ITEM_FILTER = "/work_item/filter";

    public static final String FIELD_ALL = "/field/all";

    public static final String USER_QUERY = "/open_api/user/query";
}
