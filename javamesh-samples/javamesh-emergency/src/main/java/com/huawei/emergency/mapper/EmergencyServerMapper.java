/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.mapper;

import com.huawei.emergency.entity.EmergencyServer;
import com.huawei.emergency.entity.EmergencyServerExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 服务器信息mapper
 *
 * @author y30010171
 * @since 2021-11-15
 **/
@Mapper
public interface EmergencyServerMapper {
    long countByExample(EmergencyServerExample example);

    int deleteByExample(EmergencyServerExample example);

    int deleteByPrimaryKey(Integer serverId);

    int insert(EmergencyServer record);

    int insertSelective(EmergencyServer record);

    List<EmergencyServer> selectByExample(EmergencyServerExample example);

    EmergencyServer selectByPrimaryKey(Integer serverId);

    int updateByExampleSelective(@Param("record") EmergencyServer record, @Param("example") EmergencyServerExample example);

    int updateByExample(@Param("record") EmergencyServer record, @Param("example") EmergencyServerExample example);

    int updateByPrimaryKeySelective(EmergencyServer record);

    int updateByPrimaryKey(EmergencyServer record);
}