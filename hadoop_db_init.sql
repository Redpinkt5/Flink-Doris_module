-- ============================================================
-- hadoop_db 初始化脚本
-- 1. 删除 hadoop_db 下所有业务表
-- 2. 根据当前 Java 业务代码重建表结构
--
-- 注意：DROP TABLE 不可恢复，执行前请确认数据已备份！
-- 执行方式：mysql -h 192.168.88.101 -P 9030 -uhadoop -p hadoop_db < hadoop_db_init.sql
-- ============================================================

USE hadoop_db;

-- ---------------- 1. AB 测试 ----------------
DROP TABLE IF EXISTS AbTestPvTotal;
CREATE TABLE AbTestPvTotal (
    timeinfo    VARCHAR(20) COMMENT '时间，格式 yyyyMMdd 或 yyyyMMddHH',
    testFlag    INT COMMENT '版本标识 1=A版 2=B版 3=基本版',
    numbers     BIGINT COMMENT 'PV 累计数'
)
DUPLICATE KEY(timeinfo, testFlag)
DISTRIBUTED BY HASH(timeinfo) BUCKETS 3
PROPERTIES ("replication_num" = "1");

DROP TABLE IF EXISTS AbTestUvTotal;
CREATE TABLE AbTestUvTotal (
    timeinfo    VARCHAR(20) COMMENT '时间，格式 yyyyMMdd 或 yyyyMMddHH',
    testFlag    INT COMMENT '版本标识 1=A版 2=B版 3=基本版',
    numbers     BIGINT COMMENT 'UV 累计数'
)
DUPLICATE KEY(timeinfo, testFlag)
DISTRIBUTED BY HASH(timeinfo) BUCKETS 3
PROPERTIES ("replication_num" = "1");

DROP TABLE IF EXISTS AbTestPv;
CREATE TABLE AbTestPv (
    timeinfo        VARCHAR(20) COMMENT '时间，格式 yyyyMMdd 或 yyyyMMddHH',
    testFlag        INT COMMENT '版本标识 1=A版 2=B版 3=基本版',
    productypeleeave BIGINT COMMENT '产品等级',
    productypeid    BIGINT COMMENT '产品类目 ID',
    numbers         BIGINT COMMENT 'PV 数'
)
DUPLICATE KEY(timeinfo, testFlag, productypeleeave, productypeid)
DISTRIBUTED BY HASH(timeinfo) BUCKETS 3
PROPERTIES ("replication_num" = "1");

DROP TABLE IF EXISTS AbTestUv;
CREATE TABLE AbTestUv (
    timeinfo        VARCHAR(20) COMMENT '时间，格式 yyyyMMdd 或 yyyyMMddHH',
    testFlag        INT COMMENT '版本标识 1=A版 2=B版 3=基本版',
    productypeleeave BIGINT COMMENT '产品等级',
    productypeid    BIGINT COMMENT '产品类目 ID',
    numbers         BIGINT COMMENT 'UV 数'
)
DUPLICATE KEY(timeinfo, testFlag, productypeleeave, productypeid)
DISTRIBUTED BY HASH(timeinfo) BUCKETS 3
PROPERTIES ("replication_num" = "1");

-- ---------------- 2. 终端分析 ----------------
DROP TABLE IF EXISTS TerminalLiuliang;
CREATE TABLE TerminalLiuliang (
    timeinfo    VARCHAR(20) COMMENT '时间，格式 yyyyMMdd 或 yyyyMMddHH',
    deviceType  INT COMMENT '设备类型 0=其他 1=移动 2=PC',
    numbers     BIGINT COMMENT '流量数'
)
DUPLICATE KEY(timeinfo, deviceType)
DISTRIBUTED BY HASH(timeinfo) BUCKETS 3
PROPERTIES ("replication_num" = "1");

DROP TABLE IF EXISTS TerminalNewUser;
CREATE TABLE TerminalNewUser (
    timeinfo    VARCHAR(20) COMMENT '时间，格式 yyyyMMdd 或 yyyyMMddHH',
    deviceType  INT COMMENT '设备类型 0=其他 1=移动 2=PC',
    numbers     BIGINT COMMENT '新增用户数'
)
DUPLICATE KEY(timeinfo, deviceType)
DISTRIBUTED BY HASH(timeinfo) BUCKETS 3
PROPERTIES ("replication_num" = "1");

-- ---------------- 3. 订单分析 ----------------
DROP TABLE IF EXISTS OrderTide;
CREATE TABLE OrderTide (
    timeinfo    VARCHAR(20) COMMENT '时间，格式 yyyyMMdd 或 yyyyMMddHH',
    productypeid BIGINT COMMENT '产品类目 ID（当前 sink 未写入，但 Service 已查询）',
    numbers     BIGINT COMMENT '订单数'
)
DUPLICATE KEY(timeinfo, productypeid)
DISTRIBUTED BY HASH(timeinfo) BUCKETS 3
PROPERTIES ("replication_num" = "1");

-- ---------------- 4. 营销流量 ----------------
DROP TABLE IF EXISTS MarketingLiulianghuodong;
CREATE TABLE MarketingLiulianghuodong (
    timeinfo    VARCHAR(20) COMMENT '时间，格式 yyyyMMdd 或 yyyyMMddHH',
    huodongid   BIGINT COMMENT '大促活动 ID',
    numbers     BIGINT COMMENT '活动浏览数'
)
DUPLICATE KEY(timeinfo, huodongid)
DISTRIBUTED BY HASH(timeinfo) BUCKETS 3
PROPERTIES ("replication_num" = "1");

DROP TABLE IF EXISTS MarketingLiuliangtuangou;
CREATE TABLE MarketingLiuliangtuangou (
    timeinfo    VARCHAR(20) COMMENT '时间，格式 yyyyMMdd 或 yyyyMMddHH',
    tuangouid   BIGINT COMMENT '团购活动 ID',
    numbers     BIGINT COMMENT '团购浏览数'
)
DUPLICATE KEY(timeinfo, tuangouid)
DISTRIBUTED BY HASH(timeinfo) BUCKETS 3
PROPERTIES ("replication_num" = "1");

DROP TABLE IF EXISTS MarketingLiuliangmiaosha;
CREATE TABLE MarketingLiuliangmiaosha (
    timeinfo    VARCHAR(20) COMMENT '时间，格式 yyyyMMdd 或 yyyyMMddHH',
    miaoshaid   BIGINT COMMENT '秒杀活动 ID',
    numbers     BIGINT COMMENT '秒杀浏览数'
)
DUPLICATE KEY(timeinfo, miaoshaid)
DISTRIBUTED BY HASH(timeinfo) BUCKETS 3
PROPERTIES ("replication_num" = "1");

-- ---------------- 5. 营销成交 ----------------
DROP TABLE IF EXISTS MarketingDealhuodong;
CREATE TABLE MarketingDealhuodong (
    timeinfo    VARCHAR(20) COMMENT '时间，格式 yyyyMMdd 或 yyyyMMddHH',
    huodongid   BIGINT COMMENT '大促活动 ID',
    numbers     BIGINT COMMENT '活动成交数'
)
DUPLICATE KEY(timeinfo, huodongid)
DISTRIBUTED BY HASH(timeinfo) BUCKETS 3
PROPERTIES ("replication_num" = "1");

DROP TABLE IF EXISTS MarketingDealtuangou;
CREATE TABLE MarketingDealtuangou (
    timeinfo    VARCHAR(20) COMMENT '时间，格式 yyyyMMdd 或 yyyyMMddHH',
    tuangouid   BIGINT COMMENT '团购活动 ID',
    numbers     BIGINT COMMENT '团购成交数'
)
DUPLICATE KEY(timeinfo, tuangouid)
DISTRIBUTED BY HASH(timeinfo) BUCKETS 3
PROPERTIES ("replication_num" = "1");

DROP TABLE IF EXISTS MarketingDealmiaosha;
CREATE TABLE MarketingDealmiaosha (
    timeinfo    VARCHAR(20) COMMENT '时间，格式 yyyyMMdd 或 yyyyMMddHH',
    miaoshaid   BIGINT COMMENT '秒杀活动 ID',
    numbers     BIGINT COMMENT '秒杀成交数'
)
DUPLICATE KEY(timeinfo, miaoshaid)
DISTRIBUTED BY HASH(timeinfo) BUCKETS 3
PROPERTIES ("replication_num" = "1");
