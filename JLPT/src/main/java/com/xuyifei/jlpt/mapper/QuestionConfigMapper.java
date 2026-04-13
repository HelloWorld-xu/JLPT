package com.xuyifei.jlpt.mapper;

import com.xuyifei.jlpt.entity.QuestionConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 题目分值配置数据库映射接口
 * 负责从配置表中读取 JLPT 各板块、各题型的原始分计算标准
 */
@Mapper
public interface QuestionConfigMapper {

    /**
     * 获取所有题目的分值配置列表
     * 通常在系统初始化或计算总分前，获取完整的题型与分值对应关系
     * @return 包含所有题目配置信息的 List 集合
     */
    List<QuestionConfig> selectAll();

    /**
     * 根据板块类型计算该板块的总原始分（满分）
     * 例如：输入 "Listening"，返回听力板块所有题目分值累加后的结果
     * 用于计算得分率或校验数据合法性
     * @param sectionType 板块类型（如：Language Knowledge, Reading, Listening）
     * @return 该板块的原始分总计（满分值）
     */
    Integer sumFullScoreBySection(String sectionType);
}
