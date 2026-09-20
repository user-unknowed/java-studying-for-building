# javase-demo —— JavaSE 全阶段可运行演示（Demo1~6 + JUnit）

7 个可在工作区终端真实运行的 Java 程序 + 1 套 JUnit 5 单元测试，
配合《JavaSE基础知识点-完整梳理》（0~17 章）学习。全部代码已实际编译运行通过（OpenJDK 17）。

覆盖主线：类型陷阱 → 面向对象 → 集合 → 现代语法 → 外部函数库 → 网络编程。

## 文件一览

| 文件 | 主题 | 对应教程 |
|---|---|---|
| `src/Hello.java` | 字节码演示 | 第 1 章：`javap -c` 看 `1+2` 如何变成 JVM 指令 |
| `src/Demo1_Basics.java` | 类型系统与陷阱 | 第 2 章：整数溢出 / 0.1+0.2 / String 常量池 / 装箱缓存 |
| `src/Demo2_Oop.java` | 面向对象 | 第 5 章：封装拦截非法操作 / 多态零 if-else / static 计数 |
| `src/Demo3_Collections.java` | 集合与 Stream | 第 7 章：List/Set/Map 对比 / 流水线 / 迭代删除陷阱 |
| `src/Demo4_ModernJava.java` | 现代语法全家桶 | 第 14 章：var / 文本块 / switch 表达式 / record / sealed / 模式匹配（10 组知识点） |
| `src/Demo5_Libs.java` | 外部函数库 | 第 15 章：Jackson（JSON）/ SLF4J（日志）/ Commons Lang3（字符串） |
| `src/Demo6_Network.java` | 网络编程 | 第 16 章：TCP 回显服务端 + 客户端全流程（连接 → 发送 → 应答 → 关闭） |
| `junit/CalcTest.java` | 单元测试 | 第 15 章：JUnit 5 三用例（含边界与异常断言） |

## 依赖库（`lib/`，用脚本下载）

| jar | 用途 |
|---|---|
| jackson-databind / -core / -annotations | JSON 序列化与反序列化（Demo5） |
| slf4j-api + slf4j-simple | 日志门面 + 简单实现（Demo5） |
| commons-lang3 | 字符串 / 对象工具类（Demo5） |
| junit-platform-console-standalone | JUnit 5 测试运行器 |

```bash
bash download-deps.sh        # 首次：下载全部 7 个 jar 到 lib/
```

## 编译与运行

```bash
cd javase-demo

# ① 全量编译（依赖 lib/ 里的外部库）
javac -encoding UTF-8 -cp "lib/*" -d out src/*.java

# ② 逐个运行
java -Dfile.encoding=UTF-8 -cp out Demo1_Basics
java -Dfile.encoding=UTF-8 -cp out Demo2_Oop
java -Dfile.encoding=UTF-8 -cp out Demo3_Collections
java -Dfile.encoding=UTF-8 -cp out Demo4_ModernJava
java -Dfile.encoding=UTF-8 -cp "out:lib/*" Demo5_Libs     # ← 注意带 lib
java -Dfile.encoding=UTF-8 -cp out Demo6_Network

# ③ 字节码演示
javap -c -cp out Hello
java -Dfile.encoding=UTF-8 -cp out Hello

# ④ JUnit 单元测试
javac -encoding UTF-8 -cp "lib/*" -d out-junit junit/*.java
java -jar lib/junit-platform-console-standalone-1.10.2.jar --class-path out-junit --scan-class-path
```

## 建议的学习方式

1. 先跑一遍看输出（关键输出存档见 `run_output.txt`）；
2. 对照《JavaSE基础知识点-完整梳理》找到对应章节（章号见上表）；
3. **改代码玩**：把 `Demo1` 的 `128` 改成别的数字、给 `Demo2` 加一个新图形、
   给 `Demo4` 的 switch 表达式补一个分支……观察结果变化。