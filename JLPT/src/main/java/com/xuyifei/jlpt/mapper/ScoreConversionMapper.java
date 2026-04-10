package com.xuyifei.jlpt.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ScoreConversionMapper {

    Integer selectScaledScore(
            @Param("level") String level,
            @Param("sectionType") String sectionType,
            @Param("rawScore") Integer rawScore
    );
}