package com.xuyifei.jlpt.mapper;

import com.xuyifei.jlpt.entity.QuestionConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QuestionConfigMapper {

    List<QuestionConfig> selectAll();
    Integer sumFullScoreBySection(String sectionType);
}
