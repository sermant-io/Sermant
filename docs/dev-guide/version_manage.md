# Version Management

[简体中文](version_manage-zh.md) | [English](version_manage.md)

This document is about **Version Management of Sermant**.

**Sermant** manages versions via [versions-maven-plugin](https://github.com/mojohaus/versions-maven-plugin). Common commands are as follows:

- Update current version to `${version}`：
  ```shell
  mvn versions:set -DnewVersion=${version}
  ```
  This command keeps the original `pom` file backup by default.
  
- Rollback the version:
  ```shell
  mvn versions:revert
  ```
  
- Commit the new version updated (delete the original `pom` file backup):
  ```shell
  mvn versions:commit
  ```
  
- Update current version to `${version}` and commit:
  ```shell
  mvn versions:set -DnewVersion=${version} -DgenerateBackupPoms=false
  ```
  This command will not back up the original `pom` file, be careful not to write the wrong version number when executing it.

After executing the above update commands, only the modules with the same version as the top-level module in the project will be modified. If you need to update a module separately, you can specify it with `-pl`, for example:
```shell
mvn versions:set -DnewVersion=${version} -DgenerateBackupPoms=false -pl ${module}
```
Where `${module}` can be `${groupId}:${artifactId}`. Or you can input relative path of the module. In the case of multiple modules, please use `','`.

For more information on setting versions with the `versions:set` command, refer to [Versions Maven Plugin versions:set](http://www.mojohaus.org/versions-maven-plugin/set-mojo.html).

Refer to [Versions Maven Plugin Introduction](http://www.mojohaus.org/versions-maven-plugin/index.html) for more `versions-maven-plugin` commands.

[Back to README of **Sermant** ](../README.md)