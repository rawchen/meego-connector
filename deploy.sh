# 打包前端项目
cd front
npm i
npm run build

cp -r dist/* ../src/main/resources/static/

## 制作镜像并推送到镜像仓库
cd ../
mvn package
cp ./target/meego-connector-0.0.1-SNAPSHOT.jar ./meego-connector-0.0.1-SNAPSHOT.jar
#
# docker volume create meego-connector-log
# docker build -t meego-connector .
# docker run -it --name meego-connector -p 6001:6001 -v meego-connector-log:/log --log-driver json-file --log-opt max-size=10m -d meego-connector
#

#docker build --network=host -t lark-base-docker-registry-cn-beijing.cr.volces.com/connector/meego-connector:latest .
#docker push lark-base-docker-registry-cn-beijing.cr.volces.com/connector/meego-connector:latest
#
#kubectl -n connector rollout restart deployment/meego-connector-deployment