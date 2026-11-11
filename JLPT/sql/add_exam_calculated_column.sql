-- JLPT 成绩统计功能：为 exam 表增加“是否已计算成绩”标记
-- 请在项目数据库中执行一次。
-- 新建考试默认为未计算；点击“计算成绩”后由后端更新为 1。

ALTER TABLE exam
    ADD COLUMN calculated TINYINT(1) NOT NULL DEFAULT 0
    COMMENT '是否已计算成绩：0-未计算，1-已计算';

-- 兼容迁移前已经计算过的历史考试。
-- 旧数据中总分大于 0 或 pass=1 的记录视为已计算。
UPDATE exam
SET calculated = CASE
    WHEN COALESCE(total_scaled_score, 0) > 0 OR pass = 1 THEN 1
    ELSE 0
END;
