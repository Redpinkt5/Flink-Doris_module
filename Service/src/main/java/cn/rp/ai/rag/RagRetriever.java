package cn.rp.ai.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * RAG 检索器。
 * 对外屏蔽检索细节：优先全文语义召回，失败或无结果时回退到关键字匹配。
 */
@Component
public class RagRetriever {

    private static final Logger logger = LoggerFactory.getLogger(RagRetriever.class);

    @Autowired
    private RagRepository ragRepository;

    /**
     * 检索与当前问题最相似的历史 QA 示例。
     * 任何异常都不应阻塞主查询流程，返回空列表由语义层独立生成 SQL。
     */
    public List<QAExample> retrieve(String question, int topK) {
        if (question == null || question.trim().isEmpty() || ragRepository == null) {
            return Collections.emptyList();
        }
        try {
            List<QAExample> results = ragRepository.findSimilarQuestions(question, topK);
            if (results.isEmpty()) {
                results = ragRepository.findByKeywordFallback(question, topK);
            }
            return results;
        } catch (Exception e) {
            logger.warn("RAG retrieval failed for question: {}", question, e);
            return Collections.emptyList();
        }
    }

    /**
     * 将检索到的示例格式化为 few-shot Prompt 片段，供 LLM 模仿生成 SQL 与图表配置。
     */
    public String formatForPrompt(List<QAExample> examples) {
        if (examples == null || examples.isEmpty()) {
            return "（暂无历史相似问题）";
        }
        StringBuilder sb = new StringBuilder();
        for (QAExample example : examples) {
            String q = example.getQuestion();
            String sql = example.getGeneratedSql();
            if (q == null || sql == null) {
                continue;
            }
            sb.append("问题：").append(q).append("\n");
            sb.append("SQL：").append(sql).append("\n");
            if (example.getChartType() != null) {
                sb.append("图表类型：").append(example.getChartType()).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }
}
