# sermant打包后文件夹名后缀版本号
sermantVersion=1.0.0
# sermant-agent镜像名称
imageName=xxx.huawei.com/sermant/sermant-agent
# sermant-agent镜像版本
imageVersion=1.0.0

name=${imageName}:${imageVersion}

echo "=====================rename sermant-agent package=================================="

if [ -f "sermant-agen-${sermantVersion}.tar.gz" ]; then
  cp -f sermant-agent-${sermantVersion}.tar.gz sermant-agent.tar.gz
else
  echo "sermant-agent-${sermantVersion}.tar.gz doesn't exist"
  exit 1
fi

echo "=====================build sermant-agent image=================================="

docker build -f ./Sermant.Dockerfile -t ${name} .

# docker push ${name}