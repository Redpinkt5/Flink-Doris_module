package cn.rp.ai.rag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RagRetriever {

    @Autowired
    private RagRepository ragRepository;

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
            return Collections.emptyList();
        }
    }

    public String formatForPrompt(List<QAExample> examples) {
        if (examples == null || examples.isEmpty()) {
            return "（暂无历史相似问题）";
        }
        StringBuilder sb = new StringBuilder();
        for (QAExample example : examples) {
            sb.append("问题：").append(example.getQuestion()).append("\n");
            sb.append("SQL：").append(example.getGeneratedSql()).append("\n");
            if (example.getChartType() != null) {
                sb.append("图表类型：").append(example.getChartType()).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }
}
