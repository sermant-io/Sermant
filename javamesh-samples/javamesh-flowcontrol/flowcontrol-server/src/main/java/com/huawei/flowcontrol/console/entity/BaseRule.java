package com.huawei.flowcontrol.console.entity;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 此处部分引用alibaba/Sentinel开源社区代码，诚挚感谢alibaba/Sentinel开源团队的慷慨贡献
 */
@Getter
@Setter
public abstract class BaseRule<T extends AbstractRule> {
    private Long id;
    private String app;
    private String ip;
    private Integer port;
    private Date gmtCreate;
    private Date gmtModified;
    private String extInfo;
    protected String resource;
    protected String limitApp;

    public abstract T toRule();
}
