package org.ngrinder.common.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class JsonStringAttributeConverter<X extends Object> implements AttributeConverter<X, String> {

	Class<X> clazz;

	public JsonStringAttributeConverter(){
		ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
		this.clazz = (Class<X>) ((ParameterizedType)pt.getActualTypeArguments()[0]).getRawType();
	}

	@Override
	public String convertToDatabaseColumn(X o) {
		if (o == null)
			return null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public X convertToEntityAttribute(String jsonStr) {
		if (jsonStr == null)
			return null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(jsonStr, this.clazz);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
