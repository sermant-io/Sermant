package com.huawei.argus.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.ngrinder.model.User;

import java.io.IOException;

/**
 * @Author: j00466872
 * @Date: 2019/4/26 19:46
 * 只序列化用户名
 */
public class UserSerializer extends JsonSerializer<User> {

	@Override
	public void serialize(User user, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		jsonGenerator.writeString(user.getUserName());
	}
}
