package com.huawei.argus.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.ngrinder.model.User;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: j00466872
 * @Date: 2019/4/26 20:18
 */
public class TimestampDatetimeSerializer extends JsonSerializer<Date> {
	@Override
	public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		jsonGenerator.writeString(sdf.format(date));
	}
}
