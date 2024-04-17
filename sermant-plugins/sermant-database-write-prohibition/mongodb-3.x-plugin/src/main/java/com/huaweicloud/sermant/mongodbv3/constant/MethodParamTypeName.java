/*
 *  Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.sermant.mongodbv3.constant;

/**
 * Fully qualified name constant class of mongodb3.x method parameter type
 *
 * @author daizhenyu
 * @since 2024-01-27
 **/
public class MethodParamTypeName {
    /**
     * String Class Fully Qualified Name
     */
    public static final String STRING_CLASS_NAME = "java.lang.String";

    /**
     * WriteBinding Class Fully Qualified Name
     */
    public static final String WRITE_BINDING_CLASS_NAME = "com.mongodb.binding.WriteBinding";

    /**
     * ReadPreference Class Fully Qualified Name
     */
    public static final String READ_PREFERENCE_CLASS_NAME = "com.mongodb.ReadPreference";

    /**
     * FieldNameValidator Class Fully Qualified Name
     */
    public static final String FIELD_NAME_VALIDATOR_CLASS_NAME = "org.bson.FieldNameValidator";

    /**
     * Decoder Class Fully Qualified Name
     */
    public static final String DECODER_CLASS_NAME = "org.bson.codecs.Decoder";

    /**
     * CommandCreator Class Fully Qualified Name
     */
    public static final String COMMAND_CREATOR_CLASS_NAME =
            "com.mongodb.operation.CommandOperationHelper$CommandCreator";

    /**
     * CommandWriteTransformer Interface Fully Qualified Name
     */
    public static final String COMMAND_WRITE_TRANSFORMER_CLASS_NAME =
            "com.mongodb.operation.CommandOperationHelper$CommandWriteTransformer";

    /**
     * CommandTransformer Interface Fully Qualified Name
     */
    public static final String COMMAND_TRANSFORMER_CLASS_NAME =
            "com.mongodb.operation.CommandOperationHelper$CommandTransformer";

    /**
     * Function Class Fully Qualified Name
     */
    public static final String FUNCTION_CLASS_NAME = "com.mongodb.Function";

    /**
     * BsonDocument Class Fully Qualified Name
     */
    public static final String BSON_DOCUMENT_CLASS_NAME = "org.bson.BsonDocument";

    /**
     * Connection Class Fully Qualified Name
     */
    public static final String CONNECTION_CLASS_NAME = "com.mongodb.connection.Connection";

    private MethodParamTypeName() {
    }
}
