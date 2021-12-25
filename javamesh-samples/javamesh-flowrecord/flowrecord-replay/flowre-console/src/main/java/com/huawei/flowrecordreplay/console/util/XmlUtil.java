/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */
package com.huawei.flowrecordreplay.console.util;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * xml解析
 *
 * @author lihongjiang
 * @version 0.0.1
 * @since 2021-12-10
 */
public class XmlUtil {

    static Map<String, String> xmlMap = new HashMap<String, String>();

    /**
     * xml字符串转换成Map
     * 获取标签内属性值和text值
     * @param xml
     * @return
     * @throws Exception
     */
    public static Map<String, String> xmlToMap(String xml) throws Exception {
        StringReader reader=new StringReader(xml);
        InputSource source=new InputSource(reader);
        SAXReader sax = new SAXReader(); // 创建一个SAXReader对象
        Document document=sax.read(source); // 获取document对象,如果文档无节点，则会抛出Exception提前结束
        Element root = document.getRootElement(); // 获取根节点
        Map<String, String> map = XmlUtil.getNodes(root); // 从根节点开始遍历所有节点
        return map;
    }

    /**
     * 从指定节点开始,递归遍历所有子节点
     *
     * @author lihongjiang
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getNodes(Element node) {
        xmlMap.put(node.getName().toLowerCase(),node.getTextTrim());
        List<Attribute> listAttr = node.attributes(); // 当前节点的所有属性的list
        for (Attribute attr : listAttr) { // 遍历当前节点的所有属性
            String name = attr.getName(); // 属性名称
            String value = attr.getValue(); // 属性的值
            xmlMap.put(name, value.trim());
        }

// 递归遍历当前节点所有的子节点
        List<Element> listElement = node.elements(); // 所有一级子节点的list
        for (Element e : listElement) { // 遍历所有一级子节点
            XmlUtil.getNodes(e); // 递归
        }
        return xmlMap;

    }
}
