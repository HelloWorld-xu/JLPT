package com.xuyifei.jlpt.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * JLPT 历史成绩统计响应对象
 */
@Data
public class ExamStatisticsVO {

    private String level;
    private Integer examCount;
    private Integer highestScore;
    private BigDecimal averageScore;
    private Integer passCount;
    private BigDecimal passRate;
    private List<TrendItem> trend;
    private RecentComparison recentComparison;
    private SectionExtremes sectionExtremes;

    @Data
    public static class TrendItem {
        private Long examId;
        private Integer year;
        private Integer month;
        private String label;
        private Integer languageScore;
        private Integer readingScore;
        private Integer listeningScore;
        private Integer totalScore;
        private Boolean pass;
    }

    @Data
    public static class RecentComparison {
        private String previousLabel;
        private String currentLabel;
        private Integer previousLanguageScore;
        private Integer currentLanguageScore;
        private Integer languageChange;
        private Integer previousReadingScore;
        private Integer currentReadingScore;
        private Integer readingChange;
        private Integer previousListeningScore;
        private Integer currentListeningScore;
        private Integer listeningChange;
        private Integer previousTotalScore;
        private Integer currentTotalScore;
        private Integer totalChange;
    }

    @Data
    public static class SectionExtremes {
        private ScoreRange language;
        private ScoreRange reading;
        private ScoreRange listening;
    }

    @Data
    public static class ScoreRange {
        private Integer highest;
        private Integer lowest;
    }
}
