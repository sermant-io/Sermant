/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.hercules.controller.script;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.exception.HerculesException;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * 功能描述：Script因为后端是文件系统svn，没有排序功能，所以这里只能自己排序
 *
 * 
 * @since 2021-11-04
 */
public class ScriptSorter {
    /**
     * 获取对应列比较器
     *
     * @param columnName 列名称
     * @param orderType  排序类型
     * @return 比较器
     */
    private Comparator<JSONObject> initComparator(String columnName, String orderType) {
        return (left, right) -> {
            if (!left.containsKey(columnName) || !right.containsKey(columnName)) {
                throw new HerculesException("Sort column not exist!");
            }
            Object leftValue = left.get(columnName);
            Object rightValue = right.get(columnName);
            if (leftValue instanceof Number && rightValue instanceof Number) {
                return numberCompare((Number) leftValue, (Number) rightValue, orderType);
            }
            String leftStringValue = leftValue == null ? "" : leftValue.toString();
            String rightStringValue = rightValue == null ? "" : rightValue.toString();
            return stringCompare(leftStringValue, rightStringValue, orderType);
        };
    }

    /**
     * 字符串比较器
     *
     * @param leftValue  左侧值
     * @param rightValue 右侧值
     * @param orderType  排序类型
     * @return 比较结果
     */
    private int stringCompare(String leftValue, String rightValue, String orderType) {
        if (StringUtils.isEmpty(orderType) || orderType.equals("descend")) {
            return rightValue.compareTo(leftValue);
        }
        return leftValue.compareTo(rightValue);
    }

    /**
     * 数字比较器
     *
     * @param leftValue  左侧值
     * @param rightValue 右侧值
     * @param orderType  排序类型
     * @return 比较结果
     */
    private int numberCompare(Number leftValue, Number rightValue, String orderType) {
        double rightDoubleValue = rightValue.doubleValue();
        double leftDoubleValue = leftValue.doubleValue();
        if (StringUtils.isEmpty(orderType) || orderType.equals("descend")) {
            return Double.compare(rightDoubleValue, leftDoubleValue);
        }
        return Double.compare(leftDoubleValue, rightDoubleValue);
    }

    /**
     * 对传入的list中数据按照给定的key进行排序
     *
     * @param elements     需要排序的元素
     * @param sortedColumn 需要进行排序的列
     * @param sortedType   排序方式
     * @param pageSize     需要返回数据的条数
     * @param pageNumber   需要返回的数据页数
     * @return 处理之后得到的数据
     */
    public List<JSONObject> sortAndPage(JSONArray elements, String sortedColumn, String sortedType, int pageSize, int pageNumber) {
        if (elements == null || elements.isEmpty()) {
            return Collections.emptyList();
        }
        if (pageSize <= 0 || pageNumber <= 0) {
            throw new HerculesException("Illegal paging parameter.");
        }
        List<JSONObject> result = new ArrayList<>();
        int needPassNumber = pageSize * (pageNumber - 1);
        if (!StringUtils.isEmpty(sortedColumn)) {
            // 如果排序列非空，说明需要排序，根据排序列和排序类型初始化比较器，然后根据比较器初始化堆
            Comparator<JSONObject> comparator = initComparator(sortedColumn, sortedType);
            PriorityQueue<JSONObject> elementPriorityQueue = new PriorityQueue<>(elements.size(), comparator);

            for (int i = 0; i < elements.size(); i++) {
                elementPriorityQueue.offer(elements.getJSONObject(i));
            }
            // 循环把元素从堆中拿出来，根据页码大小和页码计算需要从第几个值开始，放入到结果列表中
            for (int i = 0; i < pageSize * pageNumber; i++) {
                JSONObject element = elementPriorityQueue.poll();
                if (i < needPassNumber) {
                    continue;
                }
                if (element == null) {
                    break;
                }
                result.add(element);
            }
        } else {
            // 如果不需要排序，这里直接对elements进行分页操作
            for (int i = 0; i < pageSize * pageNumber; i++) {
                if (i < needPassNumber) {
                    continue;
                }
                if (i >= elements.size()) {
                    break;
                }
                result.add(elements.getJSONObject(i));
            }
        }
        return result;
    }
}
