# 版本管理手册

本文档主要对**Sermant**的代码版本管理做介绍。

**Sermant**直接使用[versions-maven-plugin](https://github.com/mojohaus/versions-maven-plugin)做版本管理，常用的命令如下：

- 更新版本号为`${version}`：
  ```shell
  mvn versions:set -DnewVersion=${version}
  ```
  该命令默认保留原`pom`文件备份。
- 回滚版本号：
  ```shell
  mvn versions:revert
  ```
- 提交新版本号更新，即删除原`pom`文件备份：
  ```shell
  mvn versions:commit
  ```
- 更新版本号为`${version}`并提交：
  ```shell
  mvn versions:set -DnewVersion=${version} -DgenerateBackupPoms=false
  ```
  该命令不会备份原`pom`文件，使用时要注意版本号别写错。

以上更新版本的命令中，只会修改项目中与顶级模块的版本相同的模块，如果需要单独对某个模块进行更新，可以使用`-pl`参数指定，比如：
```shell
mvn versions:set -DnewVersion=${version} -DgenerateBackupPoms=false -pl ${module}
```
其中`${module}`可以传递`${groupId}:${artifactId}`，也可以传递相对路径。多个模块的情况下，使用`','`号连接。

关于设置版本命令`versions:set`的更多信息可以查看[Versions Maven Plugin versions:set](http://www.mojohaus.org/versions-maven-plugin/set-mojo.html)。

更多`versions-maven-plugin`的命令可以查看[Versions Maven Plugin Introduction](http://www.mojohaus.org/versions-maven-plugin/index.html)。

[返回**Sermant**说明文档](../README.md)