package cn.rp.ai.semantic;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Component
public class MetricSemanticParser {

    private List<MetricDefinition> metrics = new ArrayList<>();

    public MetricSemanticParser() {
    }

    public MetricSemanticParser(List<MetricDefinition> metrics) {
        this.metrics = metrics != null ? metrics : new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        if (metrics.isEmpty()) {
            loadMetrics();
        }
    }

    private void loadMetrics() {
        Path dir = Paths.get("src/main/resources/ai/metrics");
        if (Files.exists(dir) && Files.isDirectory(dir)) {
            try (Stream<Path> paths = Files.list(dir)) {
                paths.filter(p -> p.toString().endsWith(".yml") || p.toString().endsWith(".yaml"))
                     .sorted()
                     .forEach(this::loadMetricFile);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load metric files from " + dir, e);
            }
        } else {
            loadClasspathMetric("ai/metrics/order_count.yml");
            loadClasspathMetric("ai/metrics/abtest_uv.yml");
            loadClasspathMetric("ai/metrics/marketing_deal.yml");
        }
    }

    private void loadMetricFile(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            Yaml yaml = new Yaml(new LoaderOptions());
            MetricDefinition metric = yaml.loadAs(is, MetricDefinition.class);
            metrics.add(metric);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load metric file: " + path, e);
        }
    }

    private void loadClasspathMetric(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            return;
        }
        try (InputStream is = resource.getInputStream()) {
            Yaml yaml = new Yaml(new LoaderOptions());
            MetricDefinition metric = yaml.loadAs(is, MetricDefinition.class);
            metrics.add(metric);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load classpath metric: " + path, e);
        }
    }

    public List<MetricMatchResult> parse(String question) {
        if (question == null || question.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String lowerQuestion = question.toLowerCase();
        List<MetricMatchResult> results = new ArrayList<>();
        for (MetricDefinition metric : metrics) {
            double score = calculateScore(lowerQuestion, metric);
            if (score > 0) {
                results.add(new MetricMatchResult(metric, score));
            }
        }
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return results;
    }

    private double calculateScore(String question, MetricDefinition metric) {
        double score = 0;
        score += countMatches(question, metric.getName());
        score += countMatches(question, metric.getDisplayName()) * 2;
        score += countMatches(question, metric.getDescription());
        if (metric.getSupports() != null) {
            for (String support : metric.getSupports()) {
                score += countMatches(question, support) * 1.5;
            }
        }
        return score;
    }

    private double countMatches(String question, String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        String lowerText = text.toLowerCase();
        int count = 0;
        int idx = 0;
        while ((idx = question.indexOf(lowerText, idx)) != -1) {
            count++;
            idx += lowerText.length();
        }
        return count;
    }

    public String formatForPrompt(List<MetricMatchResult> matches, int topK) {
        if (matches == null || matches.isEmpty()) {
            return "（未命中任何预定义指标，请根据 schema 推断）";
        }
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(topK, matches.size());
        for (int i = 0; i < limit; i++) {
            MetricDefinition m = matches.get(i).getMetric();
            sb.append("指标：").append(m.getDisplayName()).append("(").append(m.getName()).append(")\n");
            sb.append("表：").append(m.getTable()).append("\n");
            sb.append("聚合：").append(m.getAggregation()).append("\n");
            sb.append("时间维度：").append(m.getTimeDimension()).append("\n");
            sb.append("默认时间范围：").append(m.getDefaultTimeRange()).append("\n");
            if (m.getDimensions() != null && !m.getDimensions().isEmpty()) {
                sb.append("可用维度：");
                for (DimensionDefinition d : m.getDimensions()) {
                    sb.append(d.getName()).append("(").append(d.getDisplayName()).append(") ");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }
}
