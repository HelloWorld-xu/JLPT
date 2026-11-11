package com.xuyifei.jlpt.mapper;

import com.xuyifei.jlpt.entity.QuestionConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目分值配置数据库映射接口
 * 所有配置均按 JLPT 等级读取，避免 N1、N2、N3 的题型编号相互覆盖
 */
@Mapper
public interface QuestionConfigMapper {

    /**
     * 根据考试等级获取该等级的全部题型配置
     * @param level 考试等级（N1、N2、N3）
     */
    List<QuestionConfig> selectByLevel(@Param("level") String level);

    /**
     * 根据考试等级和板块类型计算该板块原始分满分
     * @param level 考试等级
     * @param sectionType 板块类型（language、reading、listening）
     */
    Integer sumFullScoreBySection(
            @Param("level") String level,
            @Param("sectionType") String sectionType
    );
}
