package com.xuyifei.jlpt.mapper;

import com.xuyifei.jlpt.entity.Exam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 考试记录数据库映射接口
 * 使用 MyBatis 框架实现，负责对 exam 表进行增删改查操作
 */
@Mapper
public interface ExamMapper {

    /**
     * 插入一条新的考试记录
     * @param exam 包含考试信息的实体对象
     * @return 受影响的行数（通常为 1）
     */
    int insert(Exam exam);

    /**
     * 根据主键 ID 查询特定的考试记录
     * @param id 考试记录 ID
     * @return 匹配的 Exam 实体对象，若未找到则返回 null
     */
    Exam selectById(Long id);

    /**
     * 根据 ID 更新考试记录的信息
     * 常用于保存评分结果、修改通过状态等
     * @param exam 包含更新信息及目标 ID 的实体对象
     */
    void updateById(Exam exam);

    /**
     * 根据 ID 删除指定的考试记录
     * @param examId 考试记录 ID
     */
    void deleteById(Long examId);

    /**
     * 查询数据库中所有的考试记录
     * 用于获取考试历史列表
     * @return 包含所有考试记录的 List 集合
     */
    List<Exam> selectAll();
}
