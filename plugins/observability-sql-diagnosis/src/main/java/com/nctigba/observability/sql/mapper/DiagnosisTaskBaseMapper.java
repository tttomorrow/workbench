package com.nctigba.observability.sql.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.sql.config.DataSourceIniter;
import com.nctigba.observability.sql.model.diagnosis.Task;

@Mapper
@DS(DataSourceIniter.diagnosisTask)
public interface DiagnosisTaskBaseMapper extends BaseMapper<Task> {
}