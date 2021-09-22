package com.lubanops.apm.core.master;

/**
 * @author
 * @date 2020/10/22 19:06
 */
public interface ConfigListener {

    void process(ConfigChangeEvent event);
}
