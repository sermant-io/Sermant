# sermant-injector镜像名称
imageName=xxx.huawei.com/sermant/sermant-injector
# sermant-injector镜像版本
imageVersion=1.0.0

name=${imageName}:${imageVersion}

echo "=====================build sermant-injector image=================================="

docker build -f ./Injector.Dockerfile -t ${name} .

# docker push ${imageName}