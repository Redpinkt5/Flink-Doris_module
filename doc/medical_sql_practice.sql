-- ============================================================
-- 医疗场景 MySQL 练习库
-- 建表 + 插入数据
-- 用法：mysql -uroot -p < medical_sql_practice.sql
-- 表：dept(科室) / doctor(医生) / patient(患者)
--     visit(就诊) / drug(药品) / prescription(处方明细)
-- ============================================================
DROP DATABASE IF EXISTS medical;
CREATE DATABASE medical DEFAULT CHARSET utf8mb4;
USE medical;

-- ---------------- 1. 科室表 ----------------
CREATE TABLE dept (
    dept_id   INT PRIMARY KEY COMMENT '科室ID',
    dept_name VARCHAR(50) NOT NULL COMMENT '科室名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室表';

INSERT INTO dept VALUES
(1,'内科'),(2,'外科'),(3,'儿科'),(4,'妇产科'),(5,'骨科');

-- ---------------- 2. 医生表 ----------------
CREATE TABLE doctor (
    doctor_id   INT PRIMARY KEY COMMENT '医生ID',
    doctor_name VARCHAR(50) NOT NULL COMMENT '医生姓名',
    dept_id     INT NOT NULL COMMENT '所属科室ID',
    title       VARCHAR(20) COMMENT '职称：主任医师/副主任医师/主治医师/住院医师',
    hire_date   DATE COMMENT '入职日期',
    FOREIGN KEY (dept_id) REFERENCES dept(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生表';

INSERT INTO doctor VALUES
(1,'张伟',1,'主任医师','2010-01-15'),
(2,'李娜',1,'主治医师','2015-06-01'),
(3,'王强',2,'副主任医师','2008-03-20'),
(4,'刘洋',2,'住院医师','2019-09-01'),
(5,'陈静',3,'主治医师','2016-11-12'),
(6,'赵磊',4,'主任医师','2005-04-18'),
(7,'孙丽',4,'副主任医师','2012-08-25'),
(8,'周明',5,'主治医师','2017-02-28');

-- ---------------- 3. 患者表 ----------------
CREATE TABLE patient (
    patient_id   INT PRIMARY KEY COMMENT '患者ID',
    patient_name VARCHAR(50) NOT NULL COMMENT '患者姓名',
    gender       CHAR(1) COMMENT '性别：M男 F女',
    birth_date   DATE COMMENT '出生日期',
    phone        VARCHAR(20) COMMENT '手机号',
    create_date  DATE COMMENT '建档日期'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者表';

INSERT INTO patient VALUES
(1,'王小明','M','2010-05-12','13800000001','2023-03-01'),
(2,'李红','F','1988-09-23','13800000002','2023-04-15'),
(3,'张强','M','1995-01-08','13800000003','2023-05-20'),
(4,'刘芳','F','1975-12-30','13800000004','2023-06-10'),
(5,'陈杰','M','1960-07-19','13800000005','2023-07-01'),
(6,'赵敏','F','2005-02-14','13800000006','2023-08-12'),
(7,'孙建国','M','1990-11-03','13800000007','2023-09-05'),
(8,'周晓','F','1982-06-27','13800000008','2023-10-18'),
(9,'吴丽丽','F','1998-04-02','13800000009','2023-11-22'),
(10,'郑宇','M','1970-03-11','13800000010','2023-12-08');

-- ---------------- 4. 就诊记录表 ----------------
CREATE TABLE visit (
    visit_id   INT PRIMARY KEY COMMENT '就诊ID',
    patient_id INT NOT NULL COMMENT '患者ID',
    doctor_id  INT NOT NULL COMMENT '医生ID',
    dept_id    INT NOT NULL COMMENT '科室ID',
    visit_date DATE COMMENT '就诊日期',
    diagnosis  VARCHAR(100) COMMENT '诊断结果',
    total_fee  DECIMAL(10,2) COMMENT '总费用',
    status     TINYINT COMMENT '结算状态：1已结算 0未结算',
    FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
    FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就诊记录表';

INSERT INTO visit VALUES
(1,1,1,1,'2024-01-10','上呼吸道感染',120.00,1),
(2,2,2,1,'2024-01-15','高血压',350.50,1),
(3,3,3,2,'2024-01-20','骨折',8000.00,1),
(4,4,4,2,'2024-02-02','阑尾炎',6500.00,0),
(5,5,5,3,'2024-02-10','肺炎',2200.00,1),
(6,1,1,1,'2024-03-05','支气管炎',180.00,1),
(7,6,6,4,'2024-03-12','产检',500.00,1),
(8,7,7,4,'2024-03-20','妇科炎症',880.00,0),
(9,8,8,5,'2024-04-01','颈椎病',1200.00,1),
(10,2,2,1,'2024-04-15','糖尿病复查',210.00,1),
(11,3,3,2,'2024-05-08','骨折复查',300.00,1),
(12,6,6,4,'2024-05-18','产检',520.00,1),
(13,5,5,3,'2024-06-01','咳嗽',160.00,1),
(14,10,1,1,'2024-06-10','冠心病',3400.00,1),
(15,2,2,1,'2024-07-12','高血压复诊',220.00,0),
(16,4,4,2,'2023-12-20','阑尾炎术后复查',150.00,1),
(17,7,7,4,'2024-07-20','产检',560.00,1),
(18,8,8,5,'2024-08-02','腰椎间盘突出',1800.00,1),
(19,1,1,1,'2024-08-15','感冒',90.00,1),
(20,10,1,1,'2024-08-22','冠心病复查',200.00,1);

-- ---------------- 5. 药品表 ----------------
CREATE TABLE drug (
    drug_id   INT PRIMARY KEY COMMENT '药品ID',
    drug_name VARCHAR(50) NOT NULL COMMENT '药品名称',
    price     DECIMAL(10,2) COMMENT '单价',
    type      VARCHAR(20) COMMENT '类型：西药/中药/中成药'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品表';

INSERT INTO drug VALUES
(1,'阿莫西林胶囊',15.00,'西药'),
(2,'布洛芬缓释片',12.50,'西药'),
(3,'复方板蓝根颗粒',8.00,'中成药'),
(4,'阿司匹林肠溶片',6.80,'西药'),
(5,'云南白药气雾剂',28.00,'中成药'),
(6,'丹参滴丸',32.00,'中成药'),
(7,'葡萄糖注射液',5.00,'西药'),
(8,'银翘解毒片',10.00,'中成药');

-- ---------------- 6. 处方明细表 ----------------
CREATE TABLE prescription (
    pres_id     INT PRIMARY KEY COMMENT '处方明细ID',
    visit_id    INT NOT NULL COMMENT '就诊ID',
    drug_id     INT NOT NULL COMMENT '药品ID',
    quantity    INT COMMENT '数量',
    create_time DATETIME COMMENT '开方时间',
    FOREIGN KEY (visit_id) REFERENCES visit(visit_id),
    FOREIGN KEY (drug_id) REFERENCES drug(drug_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方明细表';

INSERT INTO prescription VALUES
(1,1,1,2,'2024-01-10 10:20:00'),
(2,1,3,1,'2024-01-10 10:20:00'),
(3,2,2,1,'2024-01-15 09:30:00'),
(4,2,4,3,'2024-01-15 09:30:00'),
(5,3,5,1,'2024-01-20 14:00:00'),
(6,3,6,2,'2024-01-20 14:00:00'),
(7,5,1,3,'2024-02-10 11:15:00'),
(8,6,3,2,'2024-03-05 16:40:00'),
(9,7,7,1,'2024-03-12 09:00:00'),
(10,8,8,2,'2024-03-20 10:30:00'),
(11,9,2,3,'2024-04-01 15:20:00'),
(12,10,4,2,'2024-04-15 08:50:00'),
(13,11,5,1,'2024-05-08 13:30:00'),
(14,12,7,2,'2024-05-18 09:10:00'),
(15,13,3,1,'2024-06-01 10:00:00'),
(16,14,4,4,'2024-06-10 11:45:00'),
(17,15,4,2,'2024-07-12 09:25:00'),
(18,17,7,1,'2024-07-20 08:30:00'),
(19,18,6,2,'2024-08-02 14:50:00'),
(20,19,3,1,'2024-08-15 09:05:00');
