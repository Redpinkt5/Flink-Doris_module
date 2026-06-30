package cn.rp.ai.rag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class RagRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    public List<QAExample> findByKeywordFallback(String question, int limit) {
        String sql = "SELECT id, question, matched_metrics, generated_sql, chart_type, x_axis_field, y_axis_field " +
                     "FROM ai_qa_history " +
                     "WHERE is_success = 1 AND question LIKE ? " +
                     "ORDER BY created_at DESC " +
                     "LIMIT ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(QAExample.class), "%" + question + "%", limit);
    }
}
