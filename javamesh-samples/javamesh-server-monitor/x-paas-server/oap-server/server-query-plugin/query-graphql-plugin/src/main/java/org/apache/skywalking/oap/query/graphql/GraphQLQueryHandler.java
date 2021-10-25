/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.oap.query.graphql;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.huawei.apm.core.drill.DrillThreadLocal;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.oap.server.library.server.jetty.JettyJsonHandler;
import org.apache.skywalking.oap.server.library.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class GraphQLQueryHandler extends JettyJsonHandler {

    private static final Logger logger = LoggerFactory.getLogger(GraphQLQueryHandler.class);

    private static final String QUERY = "query";
    private static final String VARIABLES = "variables";
    private static final String DATA = "data";
    private static final String ERRORS = "errors";
    private static final String MESSAGE = "message";
    private static final String MUTATION = "mutation";
    // huawei update.无损演练：页面查询添加新标签
    private static final String FLAG = "flag";

    //huawei update 增加graphql黑名单，防止出现安全问题
    private static final String[] INTROSPECTION_BLACK_LIST= new String[]{"__"};
    //huawei update 对关联修改状态的方法做白名单校验
    private static final String MUTATION_WHITE_ITEM= "mutation createProfileTask(";

    private final Gson gson = new Gson();
    private final Type mapOfStringObjectType = new TypeToken<Map<String, Object>>() {
    }.getType();

    private final String path;

    private final GraphQL graphQL;

    @Override
    public String pathSpec() {
        return path;
    }

    @Override
    protected JsonElement doGet(HttpServletRequest req) {
        throw new UnsupportedOperationException("GraphQL only supports POST method");
    }

    @Override
    protected JsonElement doPost(HttpServletRequest req) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
        String line;
        StringBuilder request = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            request.append(line);
        }

        JsonObject requestJson = gson.fromJson(request.toString(), JsonObject.class);
        //huawei update 增加参数校验
        checkSecurity(requestJson);
        // huawei update.无损演练：页面查询添加新标签
        if (requestJson.get(FLAG) != null) {
            DrillThreadLocal.set(requestJson.get("flag").getAsString());
        }
        JsonObject result = execute(requestJson.get(QUERY)
                                  .getAsString(), gson.fromJson(requestJson.get(VARIABLES), mapOfStringObjectType));
        DrillThreadLocal.remove();
        return result;
    }

    //huawei update 参数安全校验
    private void checkSecurity(JsonObject requestJson) {
        final String requestString = requestJson.get(QUERY).getAsString();
        if (StringUtils.isBlank(requestString)) {
            return;
        }
        //判断黑名单
        for (String black : INTROSPECTION_BLACK_LIST) {
            if (StringUtils.contains(requestString, black)) {
                throw new IllegalArgumentException("invalid request params, please check your request!");
            }
        }
        //白名单判断
        if (StringUtils.containsIgnoreCase(requestString, MUTATION)
            && !StringUtils.startsWithAny(requestString, MUTATION_WHITE_ITEM)) {
            throw new IllegalArgumentException("invalid request params, please check your request!");
        }
    }

    private JsonObject execute(String request, Map<String, Object> variables) {
        try {
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                                                          .query(request)
                                                          .variables(variables)
                                                          .build();
            ExecutionResult executionResult = graphQL.execute(executionInput);
            logger.debug("Execution result is {}", executionResult);
            Object data = executionResult.getData();
            List<GraphQLError> errors = executionResult.getErrors();
            JsonObject jsonObject = new JsonObject();
            if (data != null) {
                jsonObject.add(DATA, gson.fromJson(gson.toJson(data), JsonObject.class));
            }

            if (CollectionUtils.isNotEmpty(errors)) {
                JsonArray errorArray = new JsonArray();
                errors.forEach(error -> {
                    JsonObject errorJson = new JsonObject();
                    errorJson.addProperty(MESSAGE, error.getMessage());
                    errorArray.add(errorJson);
                });
                jsonObject.add(ERRORS, errorArray);
            }
            return jsonObject;
        } catch (final Throwable e) {
            logger.error(e.getMessage(), e);
            JsonObject jsonObject = new JsonObject();
            JsonArray errorArray = new JsonArray();
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty(MESSAGE, e.getMessage());
            errorArray.add(errorJson);
            jsonObject.add(ERRORS, errorArray);
            return jsonObject;
        }
    }
}
