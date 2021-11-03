package com.huawei.apm.core.lubanops.bootstrap;

import com.huawei.apm.core.agent.definition.TopListener;

import java.util.List;
import java.util.Set;

public interface Listener extends TopListener {

    /**
     * 初始化方法
     */
    void init();

    /**
     * 需要拦截的类
     * @return
     */
    Set<String> getClasses();

    /**
     * 需要拦截的方法
     * @return
     */
    List<TransformerMethod> getTransformerMethod();

    /**
     * 是否添加自定义属性字段 通过com.lubanops.apm.bootstrap.TransformAccess来操作自定义字段
     * @return
     */
    boolean hasAttribute();

    /**
     * 是否添加获取拦截类字段方法 通过com.lubanops.apm.bootstrap.AttributeAccess来获取字段值
     * 按添加顺序放在getLopsFileds获取的数组中
     * @return
     */
    List<String> getFields();

    /**
     * 添加采集器tag
     */
    void addTag();

}
