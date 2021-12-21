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

package com.huawei.emergency.mapper;

import com.huawei.emergency.entity.EmergencyServer;
import com.huawei.emergency.entity.EmergencyServerExample;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    List<EmergencyServer> selectByKeyword(@Param("server") EmergencyServer server,
                                          @Param("keyword") String keyword,
                                          @Param("excludeServerIds") int[] excludeServerIds);
}