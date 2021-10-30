/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.mapper;

import com.huawei.emergency.dto.DownloadLogDto;
import com.huawei.emergency.dto.ListDetailsDto;
import com.huawei.emergency.dto.ListHistoryParam;
import com.huawei.emergency.entity.HistoryDetailEntity;
import com.huawei.emergency.entity.HistoryEntity;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 任务管理Mapper
 *
 * @since 2021-10-30
 */
@Mapper
public interface HistoryMapper {
    int insertHistory(HistoryEntity entity);

    List<HistoryDetailEntity> getDetailsFromRelation(int sceneId, int countScript);

    int insertDetail(List<HistoryDetailEntity> historyDetails);

    List<HistoryEntity> listHistory(ListHistoryParam param);

    List<ListDetailsDto> listHistoryDetails(int historyId);

    DownloadLogDto selectDetailById(int id);

    int updateHistoryDetailsStatusAndLog(HistoryDetailEntity historyDetailEntity);
}
