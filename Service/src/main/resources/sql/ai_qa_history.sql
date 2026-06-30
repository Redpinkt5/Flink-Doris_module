CREATE TABLE IF NOT EXISTS ai_qa_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question VARCHAR(1000) NOT NULL,
    matched_metrics VARCHAR(500),
    generated_sql TEXT,
    chart_type VARCHAR(50),
    x_axis_field VARCHAR(100),
    y_axis_field VARCHAR(100),
    is_success TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FULLTEXT INDEX idx_question (question)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
