/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AgentLoader mainClass
 *
 * @author tangle
 * @since 2023-09-26
 */
public class AgentLoader {
    private static final Logger logger = Logger.getLogger("io.sermant.script.AgentLoader");

    private AgentLoader() {
    }

    /**
     * AgentLoader 的main方法
     *
     * @param args 入参，args[0]:agentpath, args[1]:agentArgs
     */
    public static void main(String[] args) {
        try {
            List<VirtualMachineDescriptor> list = VirtualMachine.list();
            for (VirtualMachineDescriptor vmd : list) {
                if (vmd.displayName().endsWith("agentcore-test-application-1.0.0-jar-with-dependencies.jar")) {
                    VirtualMachine virtualMachine = VirtualMachine.attach(vmd.id());
                    if (args.length == 0) {
                        return;
                    }
                    if (args.length == 1) {
                        String agentPath = args[0];
                        virtualMachine.loadAgent(agentPath);
                        return;
                    }
                    String agentPath = args[0];
                    String agentArgs = args[1];
                    virtualMachine.loadAgent(agentPath, agentArgs);
                    virtualMachine.detach();
                }
            }
        } catch (AgentInitializationException | IOException | AgentLoadException | AttachNotSupportedException e) {
            logger.log(Level.WARNING, "Load sermant agent fail, exception", e);
        }
    }
}
