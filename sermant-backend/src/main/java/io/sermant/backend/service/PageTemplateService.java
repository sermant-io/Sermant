/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.backend.service;

import io.sermant.backend.Backend;
import io.sermant.backend.common.conf.DynamicConfig;
import io.sermant.backend.entity.config.Result;
import io.sermant.backend.entity.config.ResultCodeType;
import io.sermant.backend.entity.template.PageTemplateInfo;
import io.sermant.backend.yaml.FieldMappingConstructor;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Template service for configuration management page
 *
 * @author zhp
 * @since 2024-08-22
 */
@Service
public class PageTemplateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageTemplateService.class);

    private final Map<String, PageTemplateInfo> pageTemplateInfoMap = new HashMap<>();

    private final Yaml yaml = new Yaml(new FieldMappingConstructor(new LoaderOptions()));

    @Resource
    private DynamicConfig dynamicConfig;

    /**
     * Get the template information for configuration management page
     *
     * @return the template information
     */
    public Result<List<PageTemplateInfo>> getTemplateList() {
        return new Result<>(ResultCodeType.SUCCESS, new ArrayList<>(pageTemplateInfoMap.values()));
    }

    /**
     * Get the template information for the specified plugin on the configuration management page
     *
     * @param pluginName plugin name
     * @return the template information
     */
    public Result<PageTemplateInfo> getTemplate(String pluginName) {
        PageTemplateInfo pageTemplateInfo = pageTemplateInfoMap.get(pluginName);
        if (pageTemplateInfo == null) {
            return new Result<>(ResultCodeType.FAIL);
        }
        return new Result<>(ResultCodeType.SUCCESS, pageTemplateInfo);
    }

    @PostConstruct
    private void init() {
        if (StringUtils.isEmpty(dynamicConfig.getTemplatePath())) {
            ApplicationHome applicationHome = new ApplicationHome(Backend.class);
            loadTemplateFile(applicationHome.getSource().getParent() + "/config-ui-template");
            return;
        }
        loadTemplateFile(dynamicConfig.getTemplatePath());
    }

    /**
     * Retrieve the template file from the specified path
     *
     * @param templatePath template path
     */
    private void loadTemplateFile(String templatePath) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(templatePath), "*.yml")) {
            for (Path entry : stream) {
                if (!Files.isRegularFile(entry)) {
                    continue;
                }
                try (InputStream inputStream = Files.newInputStream(entry)) {
                    PageTemplateInfo pageTemplateInfo = yaml.loadAs(inputStream, PageTemplateInfo.class);
                    if (pageTemplateInfo == null || pageTemplateInfo.getPlugin() == null) {
                        LOGGER.warn("The page template file {} is missing plugin information.", entry.getFileName());
                        continue;
                    }
                    pageTemplateInfoMap.put(pageTemplateInfo.getPlugin().getEnglishName(), pageTemplateInfo);
                }
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while retrieving template file information", e);
        }
    }
}
