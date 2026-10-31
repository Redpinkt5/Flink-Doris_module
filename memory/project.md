---
name: project-current-init
description: AIBI 模块已抽离为独立 Maven 模块并推送 GitHub；API key 已外部化
metadata:
  type: project
---

## 当前项目

**名称**: AnalySystem — 大数据实时分析平台（origin: github.com/Redpinkt5/Flink-Doris_module）
**当前阶段**: AIBI 模块抽离完成并已推送（2026-11-01）

## 已完成

- 存量改动 8 个逻辑提交（Analy 包重构 / Common 清理+HBase / Collect 首访识别 / Service 兼容修正 / TestDataGenerator / docs / memory）
- AI BI 能力从 Service 抽离为独立 Maven 模块 `AIBI`（库风格，包名 cn.rp.ai / cn.rp.config 不变，Service 依赖调用，端口 8084 / /CollectService /ai/query 不变）
- Service 裁剪 okhttp/gson/snakeyaml；AIBI pom 复制 Common/starter-web exclusions 防 logback/servlet-api 泄漏
- DeepSeek key 外部化：`deepseek.api.key=${DEEPSEEK_API_KEY:}`，本地用环境变量注入
- 用 filter-branch 重写未推送历史清除明文 key（GitHub Push Protection 拦截后的正确处理），44 个提交推送成功
- 验证：AIBI 单测 6/6，全 reactor clean package 成功

## 待办

- **轮换 DeepSeek API key**（旧 key 出现在本地 git 历史与被拦截的推送中）
- 本地启动 Service 前设置环境变量：`setx DEEPSEEK_API_KEY "sk-新key"`
- doc/resume_text.txt（个人简历）已加 .gitignore，永不入库

## 记忆系统决策

inject-memory 插件 v2.1.0 新结构（_index.md/modules/bugs/progress）与本项目旧版记忆系统冲突，用户决定**跳过注入**，继续使用 user/feedback/project/reference + MEMORY.md 旧版结构。
