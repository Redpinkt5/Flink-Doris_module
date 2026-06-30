package cn.rp.ai.rag;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class RagRetrieverTest {

    private RagRetriever retriever;

    @Before
    public void setUp() {
        retriever = new RagRetriever();
    }

    @Test
    public void formatForPrompt_shouldIncludeQuestionAndSql() {
        QAExample example = new QAExample();
        example.setQuestion("最近7天订单量");
        example.setGeneratedSql("SELECT timeinfo, SUM(numbers) FROM OrderTide ...");

        String prompt = retriever.formatForPrompt(Arrays.asList(example));
        assertTrue(prompt.contains("最近7天订单量"));
        assertTrue(prompt.contains("SELECT timeinfo, SUM(numbers) FROM OrderTide"));
    }

    @Test
    public void formatForPrompt_shouldReturnEmptyHintWhenNoExamples() {
        String prompt = retriever.formatForPrompt(Arrays.asList());
        assertEquals("（暂无历史相似问题）", prompt);
    }
}
