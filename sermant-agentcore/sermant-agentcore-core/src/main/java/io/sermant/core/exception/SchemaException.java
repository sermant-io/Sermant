/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.core.exception;

import java.util.Locale;

/**
 * Schema Exception
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-04
 */
public class SchemaException extends RuntimeException {
    /**
     * Version not found
     */
    public static final MsgParser MISSING_VERSION = new MsgParser() {
        @Override
        public String parse(Object... args) {
            return String.format(Locale.ROOT, "Unable to find version of %s. ", args);
        }
    };

    /**
     * The name found does not match what was expected
     */
    public static final MsgParser UNEXPECTED_NAME = new MsgParser() {
        @Override
        public String parse(Object... args) {
            return String.format(Locale.ROOT, "Name of plugin is wrong, giving %s but expecting %s. ", args);
        }
    };

    /**
     * The version found does not match what was expected
     */
    public static final MsgParser UNEXPECTED_VERSION = new MsgParser() {
        @Override
        public String parse(Object... args) {
            return String.format(Locale.ROOT, "Version of %s is wrong, giving %s but expecting %s. ", args);
        }
    };

    /**
     * External dependency packages are not accepted
     */
    public static final MsgParser UNEXPECTED_EXT_JAR = new MsgParser() {
        @Override
        public String parse(Object... args) {
            return String.format(Locale.ROOT, "External jar %s is not allowed. ", args);
        }
    };

    private static final long serialVersionUID = 3875379572570581867L;

    /**
     * constructor
     *
     * @param parser parser
     * @param args args
     */
    public SchemaException(MsgParser parser, Object... args) {
        super(parser.parse(args));
    }

    /**
     * message parser
     *
     * @since 2021-11-04
     */
    public interface MsgParser {
        /**
         * parse
         *
         * @param args args
         * @return string
         */
        String parse(Object... args);
    }
}
