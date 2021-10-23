package com.huawei.argus.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.ngrinder.model.PerfScene;
import org.ngrinder.model.User;

import java.io.IOException;

/**
 * @Author: j00466872
 * @Date: 2019/4/26 19:46
 * 只序列化Id
 */
public class PerfSceneIdSerializer extends JsonSerializer<PerfScene> {

	@Override
	public void serialize(PerfScene perfScene, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		if(perfScene.getId() != null) {
			jsonGenerator.writeNumber(perfScene.getId());
		}else{
			jsonGenerator.writeNumber(-1);
		}

	}
}
