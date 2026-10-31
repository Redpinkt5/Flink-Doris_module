#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成 AnalySystem 数仓表结构说明 Word 文档。"""
import os
from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn

OUT_DIR = r"E:\coding\AnalySystem\doc"
OUT_PATH = os.path.join(OUT_DIR, "AnalySystem_数仓表结构说明.docx")
os.makedirs(OUT_DIR, exist_ok=True)

doc = Document()

# ---- 全局样式 ----
style = doc.styles["Normal"]
style.font.name = "微软雅黑"
style.font.size = Pt(10.5)
style.element.rPr.rFonts.set(qn("w:eastAsia"), "微软雅黑")

for lv in range(1, 5):
    hs = doc.styles[f"Heading {lv}"]
    hs.font.name = "微软雅黑"
    hs.element.rPr.rFonts.set(qn("w:eastAsia"), "微软雅黑")
    hs.font.color.rgb = RGBColor(0, 51, 102)
    hs.font.bold = True


def add_table(doc, headers, rows):
    table = doc.add_table(rows=1, cols=len(headers))
    table.style = "Light Grid Accent 1"
    hdr_cells = table.rows[0].cells
    for i, h in enumerate(headers):
        hdr_cells[i].text = h
    for row in rows:
        cells = table.add_row().cells
        for i, v in enumerate(row):
            cells[i].text = str(v)
    return table


# ==================== 封面 ====================
doc.add_paragraph()
doc.add_paragraph()
t = doc.add_paragraph()
t.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = t.add_run("AnalySystem 实时数仓\n表结构说明书")
r.font.size = Pt(22)
r.font.bold = True
r.font.color.rgb = RGBColor(0, 51, 102)

doc.add_paragraph()
info = doc.add_paragraph()
info.alignment = WD_ALIGN_PARAGRAPH.CENTER
info.add_run("Doris 数据库 hadoop_db · 13 张分析表 · 面试速查").font.size = Pt(11)

doc.add_paragraph()
note = doc.add_paragraph()
note.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = note.add_run("用途：快速搞清楚每张表存什么、从哪来、给谁用")
r.font.size = Pt(9)
r.font.color.rgb = RGBColor(150, 150, 150)

doc.add_page_break()

# ==================== 一、数仓概览 ====================
doc.add_heading("一、数仓概览", level=1)

doc.add_heading("1.1 数据库基本信息", level=2)
add_table(doc, ["项目", "内容"], [
    ["数据库类型", "Apache Doris（列存 MPP OLAP）"],
    ["数据库名", "hadoop_db"],
    ["FE 连接地址", "jdbc:mysql://192.168.88.101:9030/hadoop_db"],
    ["用户名/密码", "hadoop / hadoop"],
    ["表总数", "13 张"],
    ["存储引擎", "DUPLICATE KEY（明细模型）"],
    ["分桶策略", "按 timeinfo HASH 分桶，BUCKETS 3"],
    ["副本数", "1（单机环境）"],
])

doc.add_heading("1.2 整体数据流向", level=2)
flow = """业务 MySQL  →  Canal 监听 binlog  →  Kafka（test/topic_db/topic_log 等）
                                     ↓
                         TransferAnaly（Flink 数据路由层）
                                     ↓
          ┌─────────────┼─────────────┼─────────────┐
          ↓             ↓             ↓             ↓
     AB测试作业     订单分析作业     营销分析作业     终端分析作业
          ↓             ↓             ↓             ↓
     Doris 结果表  Doris 结果表  Doris 结果表  Doris 结果表
          ↓             ↓             ↓             ↓
     Spring Boot   Spring Boot   Spring Boot   Spring Boot
     Service/Controller 查询，返回 ECharts 图表数据"""
p = doc.add_paragraph(flow)
p.paragraph_format.left_indent = Cm(0.5)
for run in p.runs:
    run.font.name = "Consolas"
    run.font.size = Pt(9)

doc.add_heading("1.3 表分组速览", level=2)
add_table(doc, ["分组", "表名", "业务含义"], [
    ["AB 测试", "AbTestPvTotal / AbTestUvTotal", "总体 PV / UV 趋势"],
    ["AB 测试", "AbTestPv / AbTestUv", "按产品等级 + 产品类目拆分的明细 PV / UV"],
    ["终端分析", "TerminalLiuliang", "按设备类型统计的流量"],
    ["终端分析", "TerminalNewUser", "按设备类型统计的新增用户"],
    ["订单分析", "OrderTide", "订单量趋势（按天/小时 + 按类目）"],
    ["营销分析", "MarketingLiuliang*", "团购/秒杀/大促活动的浏览量"],
    ["营销分析", "MarketingDeal*", "团购/秒杀/大促活动的成交量"],
])

doc.add_page_break()

# ==================== 二、公共字段说明 ====================
doc.add_heading("二、公共字段说明", level=1)
doc.add_paragraph("以下字段几乎每张表都有，先统一理解，后面每张表不再重复解释。")
add_table(doc, ["字段名", "类型", "含义", "示例", "面试注意点"], [
    ["timeinfo", "VARCHAR(20)", "时间维度，天级为 yyyyMMdd，小时级为 yyyyMMddHH", "20250705 或 2025070509", "同一表同时存天和小时，Service 用 length(timeinfo)=8/10 区分"],
    ["numbers", "BIGINT", "累计计数（PV、UV、订单数、流量数等）", "1234", "不同表业务含义不同"],
    ["testFlag", "INT", "AB 测试版本标识", "1=A版 2=B版 3=基本版", "只在 AB 测试表出现"],
    ["deviceType", "INT", "终端设备类型", "0=其他 1=移动 2=PC", "只在终端分析表出现"],
    ["productypeid", "BIGINT", "产品类目 ID", "101", "订单表和 AB 测试明细表都有"],
    ["productypeleeave", "BIGINT", "产品等级（字段名是历史拼写错误）", "1/2/3", "代码注释里被写成“用户 id”，实际代表等级"],
    ["huodongid", "BIGINT", "大促活动 ID", "1001", "营销表专用"],
    ["tuangouid", "BIGINT", "团购活动 ID", "2001", "营销表专用"],
    ["miaoshaid", "BIGINT", "秒杀活动 ID", "3001", "营销表专用"],
])

doc.add_page_break()

# ==================== 三、AB 测试表 ====================
doc.add_heading("三、AB 测试分析表", level=1)
doc.add_paragraph("对应 Flink 作业：AbTestPvAnaly、AbTestUvAnaly")

doc.add_heading("3.1 AbTestPvTotal / AbTestUvTotal", level=2)
doc.add_paragraph("用途：查看 A/B/基本版三个版本的整体 PV/UV 趋势，不分产品等级和类目。")
add_table(doc, ["字段名", "类型", "含义"], [
    ["timeinfo", "VARCHAR(20)", "时间（yyyyMMdd 或 yyyyMMddHH）"],
    ["testFlag", "INT", "版本标识：1=A版，2=B版，3=基本版"],
    ["numbers", "BIGINT", "累计 PV 或 UV 数"],
])
p = doc.add_paragraph("对应 Service 接口：")
p.add_run("pvTotalDayTide / pvTotalHourTide / uvTotalDayTide / uvTotalHourTide").font.bold = True

doc.add_heading("3.2 AbTestPv / AbTestUv", level=2)
doc.add_paragraph("用途：查看某个版本在指定时间下，按产品等级和产品类目拆分的明细数据。")
add_table(doc, ["字段名", "类型", "含义"], [
    ["timeinfo", "VARCHAR(20)", "时间（yyyyMMdd 或 yyyyMMddHH）"],
    ["testFlag", "INT", "版本标识：1=A版，2=B版，3=基本版"],
    ["productypeleeave", "BIGINT", "产品等级（1/2/3）"],
    ["productypeid", "BIGINT", "产品类目 ID"],
    ["numbers", "BIGINT", "PV 或 UV 数"],
])
p = doc.add_paragraph("对应 Service 接口：")
p.add_run("pvDayDetail / pvHourDetail / uvDayDetail / uvHourDetail（同时返回等级和类目）").font.bold = True

doc.add_heading("3.3 AB 测试表面试要点", level=2)
for item in [
    "为什么分 Total 和明细两张表？Total 表用于趋势图（按版本折线），明细表用于下钻（看某个版本某天的等级/类目分布）。",
    "UV 怎么去重？Flink 中用两阶段 Reduce：先按 user_id 去重，再按维度聚合。",
    "productypeleeave 字段名为什么这么奇怪？这是历史拼写错误，业务含义是“产品等级”，代码里甚至有注释写成“用户id”，但实际存的是等级编码。",
]:
    doc.add_paragraph(item, style="List Bullet")

doc.add_page_break()

# ==================== 四、终端分析表 ====================
doc.add_heading("四、终端分析表", level=1)
doc.add_paragraph("对应 Flink 作业：TerminalLiuliangAnaly、TerminalUserAnaly")

doc.add_heading("4.1 TerminalLiuliang", level=2)
doc.add_paragraph("用途：统计不同设备类型（移动/PC/其他）的流量数。")
add_table(doc, ["字段名", "类型", "含义"], [
    ["timeinfo", "VARCHAR(20)", "时间（yyyyMMdd 或 yyyyMMddHH）"],
    ["deviceType", "INT", "设备类型：0=其他，1=移动，2=PC"],
    ["numbers", "BIGINT", "流量数（PV）"],
])

doc.add_heading("4.2 TerminalNewUser", level=2)
doc.add_paragraph("用途：统计不同设备类型的新增用户数。")
add_table(doc, ["字段名", "类型", "含义"], [
    ["timeinfo", "VARCHAR(20)", "时间（yyyyMMdd 或 yyyyMMddHH）"],
    ["deviceType", "INT", "设备类型：0=其他，1=移动，2=PC"],
    ["numbers", "BIGINT", "新增用户数"],
])

doc.add_heading("4.3 终端表面试要点", level=2)
for item in [
    "两张表结构几乎一样，区别只是 numbers 的业务含义：TerminalLiuliang 是流量，TerminalNewUser 是新增用户。",
    "Service 用 deviceType 字段映射成中文：1→移动端，2→PC端，0→其他。",
    "查询趋势时会按 deviceType 拆成多条折线，方便前端做分组对比。",
]:
    doc.add_paragraph(item, style="List Bullet")

doc.add_page_break()

# ==================== 五、订单分析表 ====================
doc.add_heading("五、订单分析表", level=1)
doc.add_paragraph("对应 Flink 作业：OrderAnaly")

doc.add_heading("5.1 OrderTide", level=2)
doc.add_paragraph("用途：统计订单量趋势，支持按天/小时查看总体趋势，也支持按产品类目拆分。")
add_table(doc, ["字段名", "类型", "含义"], [
    ["timeinfo", "VARCHAR(20)", "时间（yyyyMMdd 或 yyyyMMddHH）"],
    ["productypeid", "BIGINT", "产品类目 ID（总体趋势时也可能写入默认值）"],
    ["numbers", "BIGINT", "订单数"],
])
p = doc.add_paragraph("对应 Service 接口：")
p.add_run("orderDayTide / orderHourTide / orderCategoryDayTide / orderCategoryHourTide").font.bold = True

doc.add_heading("5.2 订单表面试要点", level=2)
for item in [
    "OrderTide 一条数据既能代表总体趋势，也能代表类目趋势，关键看 productypeid 是否有意义。",
    "Flink 作业里通过 HBase 维表（productInfo）补全产品类目信息，再按 timeinfo + productypeid 聚合。",
    "HBase 同步查询是当前性能瓶颈，优化方向是 Async I/O 或 open() 预加载维表。",
]:
    doc.add_paragraph(item, style="List Bullet")

doc.add_page_break()

# ==================== 六、营销分析表 ====================
doc.add_heading("六、营销分析表", level=1)
doc.add_paragraph("对应 Flink 作业：MarketingLiuliangAnaly（流量）、MarketingDealAnaly（成交）")

doc.add_heading("6.1 流量表：MarketingLiuliang*", level=2)
doc.add_paragraph("用途：统计团购、秒杀、大促活动的浏览/曝光次数。")
add_table(doc, ["表名", "字段", "含义"], [
    ["MarketingLiulianghuodong", "timeinfo, huodongid, numbers", "大促活动浏览量"],
    ["MarketingLiuliangtuangou", "timeinfo, tuangouid, numbers", "团购活动浏览量"],
    ["MarketingLiuliangmiaosha", "timeinfo, miaoshaid, numbers", "秒杀活动浏览量"],
])

doc.add_heading("6.2 成交表：MarketingDeal*", level=2)
doc.add_paragraph("用途：统计团购、秒杀、大促活动的成交订单数。")
add_table(doc, ["表名", "字段", "含义"], [
    ["MarketingDealhuodong", "timeinfo, huodongid, numbers", "大促活动成交量"],
    ["MarketingDealtuangou", "timeinfo, tuangouid, numbers", "团购活动成交量"],
    ["MarketingDealmiaosha", "timeinfo, miaoshaid, numbers", "秒杀活动成交量"],
])

doc.add_heading("6.3 营销表面试要点", level=2)
for item in [
    "6 张表结构完全一致，只是“活动ID”字段名不同：huodongid/tuangouid/miaoshaid。",
    "一个 MarketingInfoResult 实体同时包含三种 ID，Sink 里判断哪个 ID > 0 就写入对应表。",
    "流量表和成交表可以算转化率：转化 = 成交数 / 浏览数，按活动 ID 关联。",
    "这是典型的“窄表”设计：每张表只描述一个维度，查询简单、扩展方便。",
]:
    doc.add_paragraph(item, style="List Bullet")

doc.add_page_break()

# ==================== 七、表关系总图 ====================
doc.add_heading("七、表关系与查询模式", level=1)

doc.add_heading("7.1 表与表之间的关系", level=2)
relations = [
    "AbTestPvTotal / AbTestUvTotal 是 AbTestPv / AbTestUv 的汇总版：前者不分维度，后者按 productypeleeave + productypeid 拆分。",
    "TerminalLiuliang 与 TerminalNewUser 是平行表：同一维度（deviceType + timeinfo），不同指标（流量 vs 新增用户）。",
    "MarketingLiuliang* 与 MarketingDeal* 是同一营销活动的“浏览-成交”对：可以按活动 ID 计算转化率。",
    "OrderTide 与 AbTestPv/AbTestUv 都用到 productypeid，可以横向对比“浏览-订单”转化。",
    "所有表都通过 timeinfo 关联时间维度，这是整个数仓的核心连接键。",
]
for r in relations:
    doc.add_paragraph(r, style="List Bullet")

doc.add_heading("7.2 Service 层通用查询套路", level=2)
code = """-- 天级趋势（最近 10 条）
SELECT timeinfo, numbers FROM TableName
WHERE length(timeinfo) = 8
ORDER BY timeinfo DESC LIMIT 10;

-- 小时级趋势
SELECT timeinfo, numbers FROM TableName
WHERE length(timeinfo) = 10
ORDER BY timeinfo DESC LIMIT 10;

-- 多序列拆分（如按 deviceType / testFlag / productypeid）
SELECT timeinfo, numbers, split_field
FROM (
  SELECT *,
    row_number() over(partition by split_field order by timeinfo desc) as rn
  FROM TableName
  WHERE length(timeinfo) = 8
) t WHERE rn <= 10;"""
p = doc.add_paragraph(code)
p.paragraph_format.left_indent = Cm(0.5)
for run in p.runs:
    run.font.name = "Consolas"
    run.font.size = Pt(9)

doc.add_page_break()

# ==================== 八、面试重点 ====================
doc.add_heading("八、面试重点速记", level=1)

doc.add_heading("8.1 被问到“你数仓里有哪些表”时", level=2)
doc.add_paragraph("按业务场景回答：AB 测试 4 张、终端 2 张、订单 1 张、营销 6 张，共 13 张。先讲分组，再挑熟悉的表展开。")

doc.add_heading("8.2 被问到“表怎么设计”时", level=2)
for item in [
    "所有表都是 DUPLICATE KEY 明细模型，按 timeinfo HASH 分桶，适合追加写入的实时指标。",
    "时间字段统一用 VARCHAR 存 yyyyMMdd/yyyyMMddHH，天和小时共用同一张表，用长度区分。",
    "营销表采用“窄表拆分”：每种活动类型一张表，避免 NULL 泛滥。",
]:
    doc.add_paragraph(item, style="List Bullet")

doc.add_heading("8.3 被问到“数据来源”时", level=2)
for item in [
    "源头是业务 MySQL，Canal 监听 binlog 推到 Kafka。",
    "TransferAnaly 按表名路由到不同 Kafka Topic。",
    "下游 Flink 作业消费 Topic，做窗口聚合后写入 Doris。",
    "Spring Boot 通过 JDBC 读 Doris，返回图表数据给前端。",
]:
    doc.add_paragraph(item, style="List Bullet")

doc.add_heading("8.4 容易踩的坑", level=2)
for item in [
    "productypeleeave 字段名容易看错，面试时可以说“这是产品等级，字段名是历史拼写”。",
    "OrderTide 的 productypeid 在总体趋势接口里不一定有业务意义，需要看具体写入逻辑。",
    "所有表都没有 dt 分区字段，Doris 里是按 DUPLICATE KEY 和分桶组织，不是 Hive 分区。",
]:
    doc.add_paragraph(item, style="List Bullet")

doc.add_page_break()

# ==================== 九、常用验证 SQL ====================
doc.add_heading("九、常用验证 SQL", level=1)
code = """-- 查看所有表
USE hadoop_db;
SHOW TABLES;

-- 查看表结构
DESC AbTestPv;

-- 查看最近 5 条 AB 测试 PV 明细
SELECT * FROM AbTestPv ORDER BY timeinfo DESC LIMIT 5;

-- 查看某天各版本总 PV
SELECT testFlag, SUM(numbers) FROM AbTestPvTotal
WHERE timeinfo = '20250705' GROUP BY testFlag;

-- 查看各设备类型流量
SELECT deviceType, SUM(numbers) FROM TerminalLiuliang
WHERE length(timeinfo)=8 GROUP BY deviceType;

-- 查看营销活动转化率（浏览/成交）
SELECT a.tuangouid, a.numbers AS liulan, b.numbers AS chengjiao
FROM MarketingLiuliangtuangou a
JOIN MarketingDealtuangou b ON a.tuangouid = b.tuangouid
WHERE a.timeinfo = b.timeinfo;"""
p = doc.add_paragraph(code)
p.paragraph_format.left_indent = Cm(0.5)
for run in p.runs:
    run.font.name = "Consolas"
    run.font.size = Pt(9)

# ==================== 保存 ====================
doc.save(OUT_PATH)
print(f"文档已生成: {OUT_PATH}")
print(f"文件大小: {os.path.getsize(OUT_PATH)} bytes")
