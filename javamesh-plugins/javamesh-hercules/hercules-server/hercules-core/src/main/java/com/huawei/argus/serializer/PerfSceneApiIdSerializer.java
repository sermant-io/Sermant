package com.huawei.argus.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.ngrinder.model.PerfScene;
import org.ngrinder.model.PerfSceneApi;

import java.io.IOException;

/**
 * @Author: j00466872
 * @Date: 2019/4/26 19:46
 * 只序列化Id
 */
public class PerfSceneApiIdSerializer extends JsonSerializer<PerfSceneApi> {

	@Override
	public void serialize(PerfSceneApi perfSceneApi, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		if(perfSceneApi.getId() != null) {
			jsonGenerator.writeNumber(perfSceneApi.getId());
		}else{
			jsonGenerator.writeNumber(-1);
		}

	}
}
