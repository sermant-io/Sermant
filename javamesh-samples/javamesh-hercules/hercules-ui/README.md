# 安装nodejs
版本v14.17.1及以上
# 下载依赖
```sh
npm i
```
# 编译打包
```sh
npm run build
```
# 获取安装包
安装包位于build目录
# Nginx部署（可选）
工程可以部署在任何web服务器（nginx tomcat...)

将build目录中所有文件放置在ngnix的html根目录