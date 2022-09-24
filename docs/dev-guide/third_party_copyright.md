# Third Party Copyright Guide

[简体中文](third_party_copyright-zh.md) | [English](third_party_copyright.md)

This document focus on the **copyright** of **third party** source code or binary packages involved in the development process.

## Source Code Reference

If the following situation exists in the code, it is considered as a **reference** to **third party** source code:

- **Copy Overall**: Copy files directly from **third party** source code and make changes based on them.
- **Partial Copy**: Copy some methods or inner classes in **third party** source code and use them in the self-developed code.
- **Reference Design**: If developers refer to the architecture of a **third party** when designing the architecture, and there is the same content in the two architectures, it is also considered as **reference**.

In all three cases, developers are required to complete the following work with the files involved:

- Add instructions for copying **third party** source code to your `LICENSE` file, like this:
  ```txt
  The following files contain a portion of ${THIRD PARTY PROJECT NAME} project.
  
  ${RELATED FILE A} in this product is copied from ${THIRD PARTY FILE A} of ${THIRD PARTY PROJECT NAME} project.
  
  ${RELATED FILE B} in this product is copied from ${THIRD PARTY FILE B} of ${THIRD PARTY PROJECT NAME} project.
  
  ...
  
  ${THIRD PARTY PROJECT NAME} project is published at ${THIRD PARTY PROJECT CODEBASES URL} and its license is ${THIRD PARTY PROJECT LICENSE NAME}.
  ```
  Note:
  - `THIRD PARTY PROJECT NAME` represents the name of the **third party** project.
  - `RELATED FILE` is the **related file** for this project: if it is a class, type the path of full qualified class name; Otherwise, type the project relative path.
  - `THIRD PARTY FILE` represents the **copied file** of the **third party**: if it is a class, type the path of full qualified class name; Otherwise, type the project relative path. If the **third party** project is a single module project, you can also type the relative path to the source directory.
  - `THIRD PARTY PROJECT CODEBASES URL` represents the address of the **third party** project repository; If you can't find the address of source code, you can change it to the official website address or source code download address. In short, the principle is to be traceable.
  - `THIRD PARTY PROJECT LICENSE NAME` represents to the `LICENSE` name of the **third party** project, which is usually referred to the `licenses` label in the `pom` file, or pluralized if multiple `LICENSE` exist:
    
    ```txt
    ...
    and its licenses are ${LICENSE A}, ${LICENSE B}, ..., and ${LICENSE C}.
    ```
  - If there is already an entry for the target **third party** project, just cut back and fill in the copy information in the middle.
- Type the header of the **copied file** (if exists) in the **file in question** and add the copy source information, like this:
  ```txt
  Based on ${THIRD PARTY FILE} from the ${THIRD PARTY PROJECT NAME} project.
  ```
- If a **third party** project contains a `NOTICE` file, append it to the end of the `NOTICE` file of current project. If it is already included, there is no need to append it more than once.

## Jar Package with Dependencies

If developers:

- have not modified the content of the `resources` label.
- develop the module of where the `sermant.basedir` parameter correctly points to the top-level directory.
- package the project without jar packages with dependencies or package the jar packages with dependencies via `shade` and have not modified `transformers` label.

There is no need to make any adjustments to the output `jar` package, otherwise please read the instructions below and take it as it is.

In the default packaging process, the current project's default `LICENSE` file and `NOTICE` file need to be inserted. These two files are stored in the `resources/META-INF` directory of the `sermant-package` module and are specifically pointed to by the `resources` label.

In general, as long as the `sermant.basedir` parameter in the packaged module (the `packaging` label is not `pom`) points to the top-level directory of the project, these files will be added by default and don't need to be concerned.

When using the `shade` `assembly` or `spring` package plugin to package a jar package with dependencies, if the `NOTICE` file is included in the **third-party ** `jar` package, it is best to merge it with the default `NOTICE ` file of current project. `ApacheNoticeResourceTransformer` of ` shade ` plugin just can do this. This is configured in the top-level project's `pom` file. And it is not recommended to override the top-level project's settings of `shade` plugin unless you need to modify the `Transformer`.

Note: The default `LICENSE` file and `NOTICE` file mentioned in this section refer to files that only contain information about current project. The `LICENSE` file and `NOTICE` file stored in the top-level directory of the project are the files after sorting out the source code copy information, containing information related to current project and the copied **third party** project information.

## RELEASE Package

The `RELEASE` package needs to include the `LICENSE` file of the project source code and the `NOTICE` file. The former also needs to add the `LICENSE` information of all the **third party** `jar` packages included in the `RELEASE` package. The `RELEASE` package also requires **third party** `LICENSE`, which are different from the project's `LICENSE`, to be placed in the `licenses` directory. The directory is located in the `resources` directory of the `sermant-package` module.

To summarize, the internal structure of the `RELEASE` package looks like this:
- `agent` directory: core enhancement logic.
- `server` directory: supporting server sides.
- `licenses ` directory: where `LICENSE` of **third-party-open-source dependencies** that are different from the project's `LICENSE` locates in.
- `LICENSE` file: the project's `LICENSE` file, which appends a copy of the LICENSE statement of source code of **third-party-open-source** project, and the `LICENSE` description of all `jar` packages of **third-party open-source dependencies** included in the `RELEASE` package.
- `NOTICE` file: the `NOTICE` file of this project, appends the `NOTICE` file of source code of **third-party-open source** project.

This project generates a `LICENSE` file, a `NOTICE` file, and a `licenses` directory for the `RELEASE`package as follows:
- Generate the `LICENSE` information for all third-party `jar` packages involved in the project via the `license-maven-plugin`:
  ```shell
  mvn license:aggregate-add-third-party
  ```
  The resulting file, `LICENSE-binary-suffix.txt`, is stored in the `resources` directory of the `sermant-package` module. This process takes a long time to execute for the first time, so be patient.
- Project components are packaged and exported to a temporary directory.
- When`sermant-package`module is packaging, it will：
  - copy the `LICENSE` file, `NOTICE` file, and `licenses` directory of source code of current project into a temporary directory.
  - run a script to append the `LICENSE-binary-suffix.txt` file to the `LICENSE` file in the temporary directory.
  - compress the temporary directory to a 'RELEASE' package.

To sum up, developers can compile and release the `RELEASE` package with the following command:
```shell
mvn license:aggregate-add-third-party clean package -Dmaven.test.skip
```

[Back to README of **Sermant** ](../README.md)

