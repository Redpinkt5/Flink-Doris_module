#!/bin/bash

# ====================== 仅保留核心环境变量 ======================
export JAVA_HOME=/export/server/jdk
export HADOOP_HOME=/export/server/hadoop
export ZK_HOME=/export/server/zookeeper
export KAFKA_HOME=/export/server/kafka
export FLINK_HOME=/export/server/flink
export FLUME_HOME=/export/server/flume
export DORIS_HOME=/export/server/doris
export HBASE_HOME=/export/server/hbase
# ==============================================================

# 简化PATH，只保留核心服务
export PATH=$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$ZK_HOME/bin:$KAFKA_HOME/bin:$FLINK_HOME/bin:$FLUME_HOME/bin:$HBASE_HOME/bin:$PATH

# 仅创建核心服务日志目录
mkdir -p $ZK_HOME/logs $KAFKA_HOME/logs $FLINK_HOME/logs $FLUME_HOME/logs $DORIS_HOME/log

# 颜色输出（保留）
GREEN="\033[32m"
RED="\033[31m"
YELLOW="\033[33m"
NC="\033[0m"

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Doris 连接信息
DORIS_HOST="node1"
DORIS_QUERY_PORT="9030"
DORIS_HEARTBEAT_PORT="9050"
DORIS_USER="hadoop"
DORIS_PASS="hadoop"
DORIS_DB="hadoop_db"

# 等待 Doris FE 在 9030 端口可用
wait_for_fe() {
    local max_wait=${1:-60}
    local waited=0
    log_info "等待 Doris FE 在 ${DORIS_HOST}:${DORIS_QUERY_PORT} 就绪..."
    while [ $waited -lt $max_wait ]; do
        if mysql -h${DORIS_HOST} -P${DORIS_QUERY_PORT} -u${DORIS_USER} -p${DORIS_PASS} -e "SELECT 1;" > /dev/null 2>&1; then
            log_info "Doris FE 已就绪"
            return 0
        fi
        sleep 2
        waited=$((waited + 2))
        echo -n "."
    done
    echo
    log_error "Doris FE 在 ${max_wait}s 内未就绪"
    return 1
}

# 注册 BE 到 FE（如果还没注册）
register_be() {
    log_info "检查 Doris BE 是否已注册..."
    local be_count
    be_count=$(mysql -h${DORIS_HOST} -P${DORIS_QUERY_PORT} -u${DORIS_USER} -p${DORIS_PASS} -e "SHOW PROC '/backends';" 2>/dev/null | wc -l)
    if [ "$be_count" -le 1 ]; then
        log_warn "BE 未注册，执行 ALTER SYSTEM ADD BACKEND..."
        mysql -h${DORIS_HOST} -P${DORIS_QUERY_PORT} -u${DORIS_USER} -p${DORIS_PASS} -e "ALTER SYSTEM ADD BACKEND '${DORIS_HOST}:${DORIS_HEARTBEAT_PORT}';" > /dev/null 2>&1
        if [ $? -ne 0 ]; then
            log_warn "ADD BACKEND 可能已存在或失败，继续等待..."
        fi
    else
        log_info "BE 已注册"
    fi
}

# 等待 Doris BE 状态 Alive=true
wait_for_be_alive() {
    local max_wait=${1:-120}
    local waited=0
    log_info "等待 Doris BE 状态变为 Alive..."
    while [ $waited -lt $max_wait ]; do
        local alive
        alive=$(mysql -h${DORIS_HOST} -P${DORIS_QUERY_PORT} -u${DORIS_USER} -p${DORIS_PASS} -e "SHOW PROC '/backends'\G" 2>/dev/null | grep -i "Alive" | awk '{print $2}' | head -n1)
        if [ "$alive" = "true" ]; then
            log_info "Doris BE 已 Alive"
            return 0
        fi
        sleep 2
        waited=$((waited + 2))
        echo -n "."
    done
    echo
    log_error "Doris BE 在 ${max_wait}s 内未变为 Alive，请检查 be.log"
    return 1
}

# ====================== 启动集群（ZK→Hadoop→Kafka→Flink→Doris→HBase→Flume） ======================
start_cluster() {
    log_info "======================================================"
    log_info "  启动顺序：ZK → Hadoop → Kafka → Flink → Doris → HBase → Flume"
    log_info "======================================================"

    # 1. 启动 Zookeeper
    log_info "1/9 启动 Zookeeper"
    pkill -f QuorumPeerMain 2>/dev/null
    sleep 5
    $ZK_HOME/bin/zkServer.sh start
    sleep 15

    # 2. 启动 Hadoop (HDFS + YARN)
    log_info "2/9 启动 Hadoop HDFS"
    start-dfs.sh
    sleep 30

    log_info "3/9 启动 Hadoop YARN"
    start-yarn.sh
    sleep 20

    # 3. 启动 Kafka
    log_info "4/9 启动 Kafka"
    pkill -f "kafka.Kafka" 2>/dev/null
    sleep 5
    nohup $KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties > $KAFKA_HOME/logs/server.log 2>&1 &
    sleep 20

    # 4. 启动 Flink
    log_info "5/9 启动 Flink 集群"
    pkill -f "flink.*standalonesession" 2>/dev/null
    pkill -f "flink.*taskexecutor" 2>/dev/null
    sleep 5
    nohup $FLINK_HOME/bin/start-cluster.sh > $FLINK_HOME/logs/start-cluster.log 2>&1 &
    sleep 15

    # 5. 启动 Doris (FE → BE)
    log_info "6/9 启动 Doris FE"
    pkill -f "DorisFE" 2>/dev/null
    rm -f $DORIS_HOME/fe/bin/fe.pid
    sleep 5
    $DORIS_HOME/fe/bin/start_fe.sh --daemon
    wait_for_fe 60 || exit 1

    log_info "7/9 启动 Doris BE"
    pkill -f "doris_be" 2>/dev/null
    rm -f $DORIS_HOME/be/bin/be.pid
    sleep 5
    $DORIS_HOME/be/bin/start_be.sh --daemon
    sleep 10
    register_be
    wait_for_be_alive 120 || exit 1

    # 6. 启动 HBase
    log_info "8/9 启动 HBase"
    $HBASE_HOME/bin/start-hbase.sh
    sleep 15

    # 7. 启动 Flume
    log_info "9/9 启动 Flume"
    pkill -f "flume-node1.conf" 2>/dev/null
    sleep 3
    nohup flume-ng agent \
        --name hadoop \
        --conf $FLUME_HOME/conf \
        --conf-file ~/flume-node1.conf \
        > $FLUME_HOME/logs/flume-hadoop.log 2>&1 &
    sleep 5

    log_info "=========================================="
    log_info "        核心集群启动完成✅"
    log_info "=========================================="
    status_check
}

# ====================== 停止集群（Flume→HBase→Doris→Flink→Kafka→Hadoop→ZK） ======================
stop_cluster() {
    log_info "========================================================="
    log_info "  停止顺序：Flume → HBase → Doris → Flink → Kafka → Hadoop → ZK"
    log_info "========================================================="

    # 1. 停止 Flume
    log_info "1/9 停止 Flume"
    pkill -f "flume-node1.conf" 2>/dev/null
    sleep 5

    # 2. 停止 HBase
    log_info "2/9 停止 HBase"
    $HBASE_HOME/bin/stop-hbase.sh
    sleep 15

    # 3. 停止 Doris (BE → FE)
    log_info "3/9 停止 Doris BE"
    $DORIS_HOME/be/bin/stop_be.sh 2>/dev/null
    sleep 10

    log_info "4/9 停止 Doris FE"
    $DORIS_HOME/fe/bin/stop_fe.sh 2>/dev/null
    sleep 10

    # 4. 停止 Flink
    log_info "5/9 停止 Flink"
    pkill -f "flink.*standalonesession" 2>/dev/null
    pkill -f "flink.*taskexecutor" 2>/dev/null
    sleep 5

    # 5. 停止 Kafka
    log_info "6/9 停止 Kafka"
    pkill -f "kafka.Kafka" 2>/dev/null
    sleep 10

    # 6. 停止 Hadoop (YARN → HDFS)
    log_info "7/9 停止 Hadoop YARN"
    stop-yarn.sh
    sleep 10

    log_info "8/9 停止 Hadoop HDFS"
    stop-dfs.sh
    sleep 10

    # 7. 停止 Zookeeper
    log_info "9/9 停止 Zookeeper"
    $ZK_HOME/bin/zkServer.sh stop
    sleep 5
    pkill -f QuorumPeerMain 2>/dev/null

    log_info "=========================================="
    log_info "  核心集群已停止！✅"
    log_info "=========================================="
}

# ====================== 服务状态检查（核心服务） ======================
status_check() {
    log_info "========== 核心服务状态检查 =========="

    if pgrep -f "QuorumPeerMain" > /dev/null; then
        log_info "✓ Zookeeper 正在运行"
    else
        log_info "✗ Zookeeper 未运行"
    fi

    if pgrep -f "NameNode" > /dev/null; then
        log_info "✓ HDFS NameNode 正在运行"
    else
        log_info "✗ HDFS NameNode 未运行"
    fi

    if pgrep -f "kafka.Kafka" > /dev/null; then
        log_info "✓ Kafka 正在运行"
    else
        log_info "✗ Kafka 未运行"
    fi

    if pgrep -f "flink.*standalonesession" > /dev/null; then
        log_info "✓ Flink JobManager 正在运行"
    else
        log_info "✗ Flink JobManager 未运行"
    fi

    if pgrep -f "DorisFE" > /dev/null; then
        log_info "✓ Doris FE 正在运行"
    else
        log_info "✗ Doris FE 未运行"
    fi

    if pgrep -f "doris_be" > /dev/null; then
        log_info "✓ Doris BE 进程正在运行"
    else
        log_info "✗ Doris BE 进程未运行"
    fi

    # 通过 MySQL 协议检查 FE/BE 真实状态
    if mysql -h${DORIS_HOST} -P${DORIS_QUERY_PORT} -u${DORIS_USER} -p${DORIS_PASS} -e "SELECT 1;" > /dev/null 2>&1; then
        log_info "✓ Doris FE 查询端口 ${DORIS_QUERY_PORT} 可连接"
        local be_alive
        be_alive=$(mysql -h${DORIS_HOST} -P${DORIS_QUERY_PORT} -u${DORIS_USER} -p${DORIS_PASS} -e "SHOW PROC '/backends'\G" 2>/dev/null | grep -i "Alive" | awk '{print $2}' | head -n1)
        if [ "$be_alive" = "true" ]; then
            log_info "✓ Doris BE 状态 Alive"
        else
            log_error "✗ Doris BE 未 Alive（可能未注册或启动失败）"
        fi
    else
        log_error "✗ Doris FE 查询端口 ${DORIS_QUERY_PORT} 无法连接"
    fi

    if pgrep -f "HMaster" > /dev/null; then
        log_info "✓ HBase Master 正在运行"
    else
        log_info "✗ HBase Master 未运行"
    fi

    if pgrep -f "flume-node1.conf" > /dev/null; then
        log_info "✓ Flume 正在运行"
    else
        log_info "✗ Flume 未运行"
    fi
}

# 命令入口
case "$1" in
    start) start_cluster ;;
    stop) stop_cluster ;;
    status) status_check ;;
    *)
    echo "用法: $0 {start|stop|status}"
    echo "  start  启动核心集群(ZK→Hadoop→Kafka→Flink→Doris→HBase→Flume)"
    echo "  stop   停止核心集群"
    echo "  status 检查核心服务状态"
    exit 1
    ;;
esac
