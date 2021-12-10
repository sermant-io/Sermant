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

package com.huawei.javamesh.core.lubanops.bootstrap.log;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogFactory {

    private static Logger logger = null;

    private static boolean methodLogError = false;

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger lOG) {
        logger = lOG;
    }

    public static void log(Throwable e) {
        if (!methodLogError) {
            getLogger().log(Level.SEVERE, "", e);
            methodLogError = true;
        }
    }

}
