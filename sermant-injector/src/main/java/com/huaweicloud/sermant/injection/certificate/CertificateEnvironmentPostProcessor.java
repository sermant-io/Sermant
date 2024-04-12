/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.injection.certificate;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Certificate processor
 *
 * @author provenceee
 * @since 2022-07-29
 */
public class CertificateEnvironmentPostProcessor implements EnvironmentPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateEnvironmentPostProcessor.class);

    private static final String CERTIFICATE_PATH_KEY = "server.ssl.certificate";

    private static final String WHITE_PATH = "/home";

    private static final String DEFAULT_CERTIFICATE_PATH = "/home/config/sermant-injector.pem";

    private static final String PRIVATE_KEY_PATH_KEY = "server.ssl.certificate-private-key";

    private static final String DEFAULT_PRIVATE_KEY_PATH = "/home/config/sermant-injector.key";

    private static final List<String> BASE64_DECODER_FLAG = Arrays.asList("BEGIN CERTIFICATE", "BEGIN RSA PRIVATE KEY");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // In the k8s environment, this certificate is base64 encoded, so it needs to be decoded
        String certificatePath = getNotEmptyText(environment, CERTIFICATE_PATH_KEY, DEFAULT_CERTIFICATE_PATH);
        Optional<String> certificate = read(certificatePath);
        certificate.ifPresent(data -> write(data, certificatePath));

        // In the k8s environment, this private key is base64 encoded, so it needs to be decoded
        String privateKeyPath = getNotEmptyText(environment, PRIVATE_KEY_PATH_KEY, DEFAULT_PRIVATE_KEY_PATH);
        Optional<String> privateKey = read(privateKeyPath);
        privateKey.ifPresent(data -> write(data, privateKeyPath));
    }

    private Optional<String> read(String path) {
        String normalizePath = Normalizer.normalize(path, Normalizer.Form.NFKC);
        if (!isValid(normalizePath)) {
            return Optional.empty();
        }
        try (FileReader fileReader = new FileReader(normalizePath)) {
            return Optional.ofNullable(IOUtils.toString(fileReader));
        } catch (IOException e) {
            LOGGER.error("Cannot load the file[{}]", replacePath(path));
            return Optional.empty();
        }
    }

    private void write(String data, String path) {
        // Data that is already non-base64 encoded does not need to be decoded
        for (String flag : BASE64_DECODER_FLAG) {
            if (data.contains(flag)) {
                return;
            }
        }
        try (FileOutputStream outputStream = new FileOutputStream(path)) {
            IOUtils.write(Base64.getDecoder().decode(data.getBytes(StandardCharsets.UTF_8)), outputStream);
        } catch (IOException e) {
            LOGGER.error("Cannot save the file[{}]", replacePath(path));
        }
    }

    private String getNotEmptyText(ConfigurableEnvironment env, String key, String defaultValue) {
        String value = env.getProperty(key);
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private boolean isValid(String path) {
        File file = new File(path);
        try {
            String canonicalPath = file.getCanonicalPath();
            if (!canonicalPath.startsWith(WHITE_PATH)) {
                return false;
            }
        } catch (IOException e) {
            LOGGER.error("Cannot load the file[{}]", replacePath(path));
            return false;
        }
        return true;
    }

    private String replacePath(String path) {
        return path.replace(System.lineSeparator(), "_");
    }
}