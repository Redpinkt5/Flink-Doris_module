package cn.rp.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * AI 看板问答接口
 * 提供自然语言查询入口
 */
@RestController
@CrossOrigin
@RequestMapping("/ai")
public class AiControl {

    @Autowired
    private AiService aiService;

    /**
     * 自然语言查询接口
     *
     * @param question 用户问题，如"最近7天订单量趋势"
     * @return 查询结果，包含 SQL、列信息、数据行
     */
    @PostMapping("/query")
    public AiService.AiQueryResult query(@RequestBody AiQueryRequest request) {
        return aiService.ask(request.getQuestion());
    }

    /**
     * GET 方式的查询接口（方便测试）
     */
    @GetMapping("/query")
    public AiService.AiQueryResult queryGet(@RequestParam String q) {
        return aiService.ask(q);
    }

    public static class AiQueryRequest {
        private String question;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }
    }
}
