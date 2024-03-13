## 多维表格连接器插件金蝶云
> 此项目使用 [Spring Boot](https://spring.io/projects/spring-boot/) / [React](https://react.dev/) 架构。以下是有关如何使用的快速指南。


## Docker启动条件
安装 `mvn` / jdk8

## 环境变量
```properties
MEEGO_CONNECTOR_URL = jdbc:mysql://xxx.ivolces.com:3306/meego_connector?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true

MEEGO_CONNECTOR_USERNAME = root

MEEGO_CONNECTOR_PASSWORD = root
```

### 前端根目录文件

front/.env.production

### 端口
```bash
8088
```

## 启动

```bash
chmod +x deploy.sh
./deploy.sh
```