# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) about working with this codebase.

## Project Overview

- **Type**: Hadoop/Flink 大数据实时分析平台
- **Language**: Java (Flink/MapReduce jobs)
- **Build**: Maven
- **Purpose**: 处理埋点、PV/UV、转化率等业务指标，支持 ABTest、营销分析、订单分析等实时流计算任务

## Collaboration Style

- **节奏**: 独立自主完成，频繁同步汇报
- **沟通**: 决策前先确认，不要静默处理
- **提交**: 每次修改后主动汇报进度

## Development Workflow

1. 理解需求 → 方案确认 → 代码实现 → 验证
2. 改动涉及多模块时，先问用户确认方案
3. 涉及配置、SQL、路径等关键路径，必须明确后再操作

## Memory System

AI 记忆存储在 `memory/` 目录下：
- `memory/user.md` — 用户角色、偏好、知识
- `memory/feedback.md` — 用户给的修正和确认
- `memory/project.md` — 项目当前目标、进度、 initiative
- `memory/reference.md` — 外部系统引用（监控面板、issue 追踪等）
- `MEMORY.md` — 记忆索引

记忆会在每次对话末尾自动同步更新。不要在 CLAUDE.md 中写操作手册或决策日志。

## Code Conventions

- MapReduce/Flink 风格：Map → Reduce → Sink 三段式
- 埋点数据字段：event、user_id、device_id、timestamp、properties
- 配置外部化：尽量避免硬编码
