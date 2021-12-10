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

package com.huawei.argus.restcontroller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.controller.BaseController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

public class RestBaseController extends BaseController {

	public JSONObject pageToJson(Page page) {
		JSONObject result = new JSONObject();
		if (page == null) {
			return result;
		}
		List content = page.getContent();
		result.put("content", listToJsonArray(Arrays.asList(content.toArray())));
		result.put("total", page.getTotalElements());
		result.put("page", page.getNumber());
		result.put("size", page.getSize());
		Sort sort = page.getSort();
		if (sort != null) {
			Iterator<Sort.Order> iterator = sort.iterator();
			if (iterator.hasNext()) {
				Sort.Order sortProp = iterator.next();
				result.put("sort", sortProp.getProperty() + "," + sortProp.getDirection());
			}
		}
		return result;
	}

	public JSONArray listToJsonArray(List<Object> list) {
		JSONArray array = new JSONArray();
		if (list == null || list.isEmpty()) {
			return array;
		}
		for (Object item : list) {
			array.add(modelStrToJson(item.toString()));
		}
		return array;
	}

	public JSONObject strToJson(String str) {
		JSONObject result = new JSONObject();
		while (true) {
			String before = getKey(str);
			String after = getValue(str);
			if (before == null) {
				break;
			}
			if ("null".equals(after) || "<null>".equals(after)) {
				result.put(before, null);
			} else {
				result.put(before, after);
			}
			str = str.replace(before + "=" + after, "");
		}
		return result;
	}

	public JSONObject modelStrToJson(String str) {
		if (StringUtils.isEmpty(str)) {
			return new JSONObject();
		}
		if (str.contains("{") && str.contains("}")) {
			str = str.replaceAll("\\{", "[");
			str = str.replaceAll("}", "]");
		}
		String keyStr = "Json";
		Map<String, String> allObjInfos = new HashMap<>();
		int count = 0;
		while (str.contains("[")) {
			int startIndex = str.lastIndexOf("[");
			int endIndex = str.indexOf("]", startIndex);
			if (startIndex < endIndex) {
				String objInfo = str.substring(startIndex, endIndex + 1);
				int lastIndexOf = str.substring(0, startIndex).lastIndexOf("=");
				String key = "";
				if (lastIndexOf > 0 && lastIndexOf < startIndex) {
					key = str.substring(lastIndexOf + 1, startIndex);
					String value = strToJson(objInfo).toJSONString();
					allObjInfos.put(keyStr + count, value);
				} else {
					break;
				}
				str = str.replace(key + objInfo, keyStr + (count++));
			} else {
				break;
			}
		}
		JSONObject object = strToJson(str);
		String objInfo = object.toJSONString();
		for (int i = count - 1; i >= 0; i--) {
			String key = keyStr + i;
			objInfo = objInfo.replace("\"" + key + "\"", allObjInfos.get(key));
		}
		return JSONObject.parseObject(objInfo, JSONObject.class);
	}

	protected void putPageIntoModelMap(JSONObject modelInfos, Pageable pageable) {
		modelInfos.put("page", pageable.getPageNumber());
		modelInfos.put("size", pageable.getPageSize());
		final Iterator<Sort.Order> iterator = pageable.getSort().iterator();
		if (iterator.hasNext()) {
			Sort.Order sortProp = iterator.next();
			modelInfos.put("sort", sortProp.getProperty() + "," + sortProp.getDirection());
		}
	}


	protected Pageable getPageable(String pages) {
		Pageable pageable = new PageRequest(0, 10,new Sort(Sort.Direction.DESC, "id"));
		if (pages != null && !StringUtils.isEmpty(pages)) {
			JSONObject jsonObject = JSONObject.parseObject(pages);
			Integer page = jsonObject.getInteger("page");
			Integer size = jsonObject.getInteger("size");
			String sort = jsonObject.getString("sort");
			Sort.Direction direction = Sort.Direction.DESC;
			String properties = "id";
			if (sort !=null && !StringUtils.isEmpty(sort)) {
				String[] split = sort.split(",");
				if (split.length == 2) {
					properties = split[0].trim();
					direction = Sort.Direction.fromString(split[1].trim());
				}
			}
			pageable = new PageRequest(page, size,new Sort(direction, properties));
		}
		return pageable;
	}

	private String getKey(String str) {
		int i = str.indexOf("=");
		int j = i;
		if (i == -1) {
			return null;
		}
		while (true) {
			String tempStr = null;
			if (j - 1 >= 0) {
				tempStr = str.substring(j - 1, j);
			} else {
				break;
			}
			if ("[],".indexOf(tempStr) > -1) {
				break;
			}
			j--;
		}
		return str.substring(j, i);
	}

	private static String getValue(String str) {
		int i = str.indexOf("=");
		int j = i + 1;
		int length = str.length();
		if (i == -1 || j >= length) {
			return null;
		}
		while (true) {
			String tempStr1 = null;
			if (j + 1 < length) {
				tempStr1 = str.substring(j, j + 1);
			} else {
				break;
			}
			if ("]".equals(tempStr1)) {
				break;
			}
			if (",".equals(tempStr1)) {
				int i1 = str.indexOf(",", j + 1);
				int i2 = str.indexOf("=", j + 1);
				if ((i1 >= i2 && i2 > 0) || (i1 < 0 && i2 > 0)) {
					break;
				}
			}
			j++;
		}
		return str.substring(i + 1, j);
	}
}
