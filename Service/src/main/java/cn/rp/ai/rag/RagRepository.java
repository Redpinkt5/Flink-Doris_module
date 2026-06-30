package cn.rp.ai.rag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * RAG 数据访问层。
 * 基于 MySQL FULLTEXT 实现相似问题检索，并提供关键字 LIKE 兜底召回。
 */
@Repository
public class RagRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 使用 MySQL 全文索引 MATCH ... AGAINST 做语义相似检索。
     * 要求 ai_qa_history 表对 question 字段建立 FULLTEXT 索引。
     */
    public List<QAExample> findSimilarQuestions(String question, int limit) {
        String sql = "SELECT id, question, matched_metrics, generated_sql, chart_type, x_axis_field, y_axis_field " +
                     "FROM ai_qa_history " +
                     "WHERE is_success = 1 " +
                     "AND MATCH(question) AGAINST(? IN NATURAL LANGUAGE MODE) " +
                     "ORDER BY created_at DESC " +
                     "LIMIT ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(QAExample.class), question, limit);
    }

    public void save(QAExample example) {
        String sql = "INSERT INTO ai_qa_history " +
                     "(question, matched_metrics, generated_sql, chart_type, x_axis_field, y_axis_field, is_success) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, example.getQuestion(), example.getMatchedMetrics(),
                example.getGeneratedSql(), example.getChartType(), example.getxAxisField(),
                example.getyAxisField(), example.getIsSuccess() != null ? example.getIsSuccess() : 1);
    }

    /**
     * 全文检索无结果时，按问题包含关键字做 LIKE 兜底查询，提高召回率。
     */
    public List<QAExample> findByKeywordFallback(String question, int limit) {
        String sql = "SELECT id, question, matched_metrics, generated_sql, chart_type, x_axis_field, y_axis_field " +
                     "FROM ai_qa_history " +
                     "WHERE is_success = 1 AND question LIKE ? " +
                     "ORDER BY created_at DESC " +
                     "LIMIT ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(QAExample.class), "%" + question + "%", limit);
    }
}
