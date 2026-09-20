# JavaSE 基础演示（javase-demo）

4 个可在工作区终端真实运行的 Java 程序，配合《JavaSE基础知识点-完整梳理.md》学习。
全部代码已实际编译运行通过（OpenJDK 17）。

## 文件一览

| 文件 | 主题 | 看点 |
|---|---|---|
| `src/Hello.java` | 字节码演示 | `javap -c` 看 `1+2` 如何变成 JVM 指令 |
| `src/Demo1_Basics.java` | 类型系统与陷阱 | 整数溢出 / 0.1+0.2 / String 常量池 / 装箱缓存 |
| `src/Demo2_Oop.java` | 面向对象 | 封装拦截非法操作 / 多态零 if-else / static 计数 |
| `src/Demo3_Collections.java` | 集合与 Stream | List/Set/Map 对比 / 流水线 / 迭代删除陷阱 |

## 编译与运行

```bash
cd javase-demo
javac -encoding UTF-8 -d out src/*.java

# 运行各演示
java -Dfile.encoding=UTF-8 -cp out Demo1_Basics
java -Dfile.encoding=UTF-8 -cp out Demo2_Oop
java -Dfile.encoding=UTF-8 -cp out Demo3_Collections

# 字节码演示
javap -c -cp out Hello
java -Dfile.encoding=UTF-8 -cp out Hello
```

## 建议的学习方式

1. 先跑一遍看输出；
2. 对照《JavaSE基础知识点-完整梳理.md》找到对应知识点；
3. **改代码玩**：把 `Demo1` 的 `128` 改成别的数字、给 `Demo2` 加一个新图形、给 `Demo3` 加一个人——观察结果变化。