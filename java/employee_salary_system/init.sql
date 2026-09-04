-- 员工工资管理系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS salary_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE salary_system;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    role VARCHAR(20) DEFAULT 'user' COMMENT '角色：admin/user',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 员工表
CREATE TABLE IF NOT EXISTS employees (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '员工ID',
    employee_code VARCHAR(20) NOT NULL UNIQUE COMMENT '员工编号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender VARCHAR(10) COMMENT '性别',
    age INT COMMENT '年龄',
    phone VARCHAR(20) COMMENT '电话',
    email VARCHAR(100) COMMENT '邮箱',
    department VARCHAR(50) COMMENT '部门',
    position VARCHAR(50) NOT NULL COMMENT '岗位',
    base_salary DECIMAL(10,2) DEFAULT 0 COMMENT '基本工资',
    bonus DECIMAL(10,2) DEFAULT 0 COMMENT '奖金',
    deductions DECIMAL(10,2) DEFAULT 0 COMMENT '扣款',
    hire_date DATE COMMENT '入职日期',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active/inactive',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- 工资记录表
CREATE TABLE IF NOT EXISTS salary_records (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    employee_id INT NOT NULL COMMENT '员工ID',
    employee_code VARCHAR(20) NOT NULL COMMENT '员工编号',
    employee_name VARCHAR(50) NOT NULL COMMENT '员工姓名',
    position VARCHAR(50) NOT NULL COMMENT '岗位',
    department VARCHAR(50) COMMENT '部门',
    base_salary DECIMAL(10,2) NOT NULL COMMENT '基本工资',
    work_hours DECIMAL(10,2) DEFAULT 0 COMMENT '工作时间(技术员)',
    sales_amount DECIMAL(12,2) DEFAULT 0 COMMENT '销售额(销售员/经理)',
    calculated_salary DECIMAL(10,2) NOT NULL COMMENT '计算工资',
    bonus DECIMAL(10,2) DEFAULT 0 COMMENT '奖金',
    deductions DECIMAL(10,2) DEFAULT 0 COMMENT '扣款',
    net_salary DECIMAL(10,2) NOT NULL COMMENT '实发工资',
    salary_month VARCHAR(7) NOT NULL COMMENT '工资月份(YYYY-MM)',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (employee_id) REFERENCES employees(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工资记录表';

-- 学生表（成绩分析用）
CREATE TABLE IF NOT EXISTS students (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '学生ID',
    student_code VARCHAR(20) NOT NULL UNIQUE COMMENT '学号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender VARCHAR(10) COMMENT '性别',
    class_name VARCHAR(50) COMMENT '班级',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- 成绩表（成绩分析用）
CREATE TABLE IF NOT EXISTS scores (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '成绩ID',
    student_id INT NOT NULL COMMENT '学生ID',
    student_code VARCHAR(20) NOT NULL COMMENT '学号',
    student_name VARCHAR(50) NOT NULL COMMENT '学生姓名',
    subject VARCHAR(50) NOT NULL COMMENT '科目',
    score DECIMAL(5,2) NOT NULL COMMENT '分数',
    exam_date DATE COMMENT '考试日期',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (student_id) REFERENCES students(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';

-- 插入默认管理员用户 (密码: admin123)
INSERT INTO users (username, password, real_name, role) VALUES 
('admin', 'admin123', '系统管理员', 'admin'),
('user', 'user123', '普通用户', 'user');

-- 插入示例员工数据
INSERT INTO employees (employee_code, name, gender, age, phone, email, department, position, base_salary, hire_date) VALUES 
('EMP001', '张三', '男', 35, '13800138001', 'zhangsan@company.com', '管理层', '经理', 8000.00, '2020-01-15'),
('EMP002', '李四', '男', 28, '13800138002', 'lisi@company.com', '技术部', '技术员', 0.00, '2021-03-20'),
('EMP003', '王五', '女', 25, '13800138003', 'wangwu@company.com', '销售部', '销售员', 0.00, '2022-05-10'),
('EMP004', '赵六', '男', 40, '13800138004', 'zhaoliu@company.com', '销售部', '销售经理', 5000.00, '2019-08-01');

-- 插入示例工资记录
INSERT INTO salary_records (employee_id, employee_code, employee_name, position, department, base_salary, work_hours, sales_amount, calculated_salary, net_salary, salary_month) VALUES 
(1, 'EMP001', '张三', '经理', '管理层', 8000.00, 0, 0, 8000.00, 8000.00, '2024-01'),
(2, 'EMP002', '李四', '技术员', '技术部', 0.00, 160.00, 0, 16000.00, 16000.00, '2024-01'),
(3, 'EMP003', '王五', '销售员', '销售部', 0.00, 0, 50000.00, 2000.00, 2000.00, '2024-01'),
(4, 'EMP004', '赵六', '销售经理', '销售部', 5000.00, 0, 200000.00, 6000.00, 6000.00, '2024-01');

-- 插入示例学生数据
INSERT INTO students (student_code, name, gender, class_name) VALUES 
('STU001', '王小明', '男', '计算机1班'),
('STU002', '李小红', '女', '计算机1班'),
('STU003', '张小强', '男', '计算机2班'),
('STU004', '刘小芳', '女', '计算机2班');

-- 插入示例成绩数据
INSERT INTO scores (student_id, student_code, student_name, subject, score, exam_date) VALUES 
(1, 'STU001', '王小明', '数学', 85.5, '2024-01-15'),
(1, 'STU001', '王小明', '语文', 92.0, '2024-01-15'),
(1, 'STU001', '王小明', '英语', 88.5, '2024-01-15'),
(2, 'STU002', '李小红', '数学', 95.0, '2024-01-15'),
(2, 'STU002', '李小红', '语文', 88.0, '2024-01-15'),
(2, 'STU002', '李小红', '英语', 91.5, '2024-01-15'),
(3, 'STU003', '张小强', '数学', 72.0, '2024-01-15'),
(3, 'STU003', '张小强', '语文', 65.5, '2024-01-15'),
(3, 'STU003', '张小强', '英语', 58.0, '2024-01-15'),
(4, 'STU004', '刘小芳', '数学', 45.0, '2024-01-15'),
(4, 'STU004', '刘小芳', '语文', 55.0, '2024-01-15'),
(4, 'STU004', '刘小芳', '英语', 38.5, '2024-01-15');
