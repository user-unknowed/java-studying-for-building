# JavaSE 基础知识点 · 完整梳理

> 面向 Java 初学者 ｜ 配套可运行演示：`javase-demo/`（4 个程序已实际编译运行）
> 整理：Operit AI ｜ 2026-09
> 定位：本篇 + 《Java与SQL数据库-深度融合教学》 = 从 Java 基础到数据库的完整阶梯

---

## 0. 知识地图

| 模块 | 核心内容 | 重要度 |
|---|---|---|
| 运行机制 | JDK / JRE / JVM、字节码 | ★★★★★ |
| 语言基础 | 类型 / 运算符 / 流程 / 数组 | ★★★★★ |
| 面向对象 | 封装 / 继承 / 多态 / 接口 | ★★★★★ |
| 常用 API | String / 包装类 / 日期 / 枚举 | ★★★★★ |
| 集合框架 | List / Set / Map / 泛型 | ★★★★★ |
| 异常处理 | 体系 / try-catch / 自定义 | ★★★★★ |
| 现代特性 | Lambda / Stream / record | ★★★★★ |
| IO 流 | 字节流 / 字符流 / NIO | ★★★ |
| 多线程 | Thread / 锁 / 线程池 | ★★★★ |
| 反射 / 注解 | 框架的基石 | ★★★ |

---

## 1. 运行机制：Java 是怎么跑起来的

### 1.1 编译 + 解释的"混合模式"

```
Hello.java  --javac 编译-->  Hello.class (字节码)  --JVM 解释/JIT-->  机器码执行
```

- `.java` 是人写的源码；`.class` 是 JVM 能懂的"中间语言"（字节码）；
- **跨平台原理**：一次编译、到处运行（WORA）——靠的是"每台机器装自己的 JVM"；
- JIT（即时编译）：热点代码被编译成机器码，越跑越快。

### 1.2 JDK / JRE / JVM 三者关系（面试必问）

| 概念 | 全称 | 包含 | 比喻 |
|---|---|---|---|
| JVM | Java Virtual Machine | 执行字节码的虚拟机 | "发动机" |
| JRE | Java Runtime Environment | JVM + 核心类库 | "整车（能开）" |
| JDK | Java Development Kit | JRE + 编译器/调试工具 | "整车 + 修理厂" |

### 1.3 亲眼看字节码（本次实操）

`int c = 1 + 2;` 在字节码里是这样的（javap 反汇编真实输出）：

```
0: iconst_1     ← 把常量 1 压入操作数栈
1: istore_1     ← 弹出存到局部变量 1
2: iconst_2     ← 压入常量 2
3: istore_2     ← 存到局部变量 2
4: iload_1      ← 取局部变量 1
5: iload_2      ← 取局部变量 2
6: iadd         ← 相加
7: istore_3     ← 结果存变量 3
```

> 体会：Java 源码 → JVM 指令，这就是"编译"的本质。你写的每一行代码，最终都会变成这样的指令序列。

---

## 2. 数据类型与变量

### 2.1 八种基本类型（背下来）

| 类型 | 字节 | 范围/说明 | 字面量示例 |
|---|---|---|---|
| byte | 1 | [-128, 127] | `(byte) 100` |
| short | 2 | [-32768, 32767] | `(short) 100` |
| int | 4 | 约 ±21 亿（最常用） | `100` |
| long | 8 | 时间戳/ID 必用 | `100L` |
| float | 4 | 单精度 | `3.14f` |
| double | 8 | 双精度（小数默认） | `3.14` |
| char | 2 | 一个 Unicode 字符 | `'A'` |
| boolean | 1 | true / false | `true` |

### 2.2 三大经典陷阱（demo 已真实跑出）

1. **整数溢出**（静默事故，不报错）：
   ```
   int 最大值 + 1 = -2147483648   ← 溢出! 静默变成负数
   ```
2. **浮点精度**（钱绝对不能碰）：
   ```
   0.1 + 0.2 = 0.30000000000000004   ← 不等于 0.3!
   BigDecimal: 0.1 + 0.2 = 0.3       ← 金额必须这样算
   ```
3. **char 本质是数字**：`'A' + 1 == 66`。

### 2.3 变量、常量、作用域

- 局部变量**必须初始化**；类字段自动有默认值；
- `final` = 常量（赋值后不可改）；
- `var` = 局部变量类型推断（JDK10+）：`var list = new ArrayList<String>();`

---

## 3. 运算符与流程控制

### 3.1 运算符要点

- **整数除法**：`5 / 2 == 2`（不是 2.5！要 `5.0 / 2`）；
- `%` 取模：判奇偶、环形索引；
- `&&` / `||` **短路**：左边能决定结果就不算右边；
- 字符串比较**永远用 `.equals()`**（`==` 比的是引用地址，见 Demo1）；
- 三元运算符：`a > b ? x : y`。

### 3.2 流程控制

- `if / else if / else`；
- **switch**：经典写法 vs **switch 表达式**（JDK14+，能直接返回值）：
  ```java
  String type = switch (day) {
      case 1, 2, 3, 4, 5 -> "工作日";
      case 6, 7 -> "休息日";
      default -> "非法日期";
  };
  ```
- `for` / `while` / `do-while` / 增强 for-each；
- `break` / `continue` / 带标签的循环。

---

## 4. 数组

- 声明与初始化：`int[] a = {1, 2, 3};`；
- **定长**：创建后长度不可变（要"可变长"就用集合）；
- `Arrays` 工具类：`sort` / `toString` / `binarySearch` / `copyOf`；
- 二维数组：`int[][] grid = new int[3][4];`；
- demo 实测：`[5, 2, 8, 1, 9]` 排序后 → `[1, 2, 5, 8, 9]`。

---

## 5. 面向对象（Java 的灵魂）

### 5.1 类与对象

- 类是图纸，对象是成品；
- `new` 做的事：分配内存 → 调用构造方法 → 返回引用。

### 5.2 封装

- `private` 字段 + 公开方法 = 数据只能通过受控入口访问；
- demo 实测：`deposit(-10)` 被类内部校验拦截 → "拦截非法操作: 金额必须为正数!"

### 5.3 继承

- `extends` / `super` / 方法重写（`@Override`）；
- **Java 是单继承**：一个类只能有一个直接父类。

### 5.4 多态（最重要的一句话）

> **父类引用指向子类对象。**

```java
Shape sh = new Circle(2);   // 编译时看 Shape, 运行时看 Circle
sh.area();                  // 动态绑定 → 实际执行 Circle.area()
```

demo 实测（一个循环，三种图形，零个 if/else）：

```
  圆形     面积 = 12.57
  矩形     面积 = 12.00
  三角形   面积 = 6.00
总面积 = 30.57   ← 循环里没有一行 if/else, 全靠多态
```

**多态的价值**：新增一种图形，不需要改老代码 → 开闭原则。

### 5.5 抽象类 vs 接口

| 对比项 | 抽象类 | 接口 |
|---|---|---|
| 关键字 | `abstract class` | `interface` |
| 表达 | "是什么"（is-a） | "能做什么"（can-do） |
| 字段 | 任意 | 常量（默认 public static final） |
| 方法 | 抽象 + 具体 | 抽象 + 默认 + 静态（JDK8+） |
| 继承 | 单继承 | 多实现 |

> 选择原则：有共享状态/代码 → 抽象类；描述能力/跨类型协议 → 接口。

### 5.6 其他关键知识点

- **static**：属于"类"而不是对象（demo 里 `Shape.count` 统计了实例个数）；
- **final**：变量（常量）/ 方法（不可重写）/ 类（不可继承）三种用法；
- **Object 三大方法**：`toString` / `equals` / `hashCode`——**重写 equals 必须重写 hashCode**（否则 HashMap 会出诡异 bug）；
- **内部类**：成员 / 静态 / 局部 / 匿名；编译后会生成 `外部类$内部类.class` 独立文件（本次编译产物里可以看到）；
- **访问修饰符**：`private < 默认 < protected < public`。

---

## 6. 常用 API

### 6.1 String 家族（使用频率第一）

- **不可变**：每次"修改"都是产生新对象；
- **常量池**：字面量复用，`new` 不复用（demo 实测：`s1==s2` 为 true，`s1==s3` 为 false）；
- 常用方法：`length` / `substring` / `indexOf` / `split` / `replace` / `startsWith` / `format`；
- 字符串拼接：**循环里绝不用 `+`**（每次循环都创建新对象）→ 用 `StringBuilder`。

| 类 | 可变性 | 线程安全 | 场景 |
|---|---|---|---|
| String | 不可变 | 安全 | 常量、Key |
| StringBuilder | 可变 | 不安全 | 单线程拼接（首选）|
| StringBuffer | 可变 | 安全（synchronized）| 多线程拼接（少见）|

### 6.2 包装类

- 自动装箱/拆箱：`int ↔ Integer`；
- **缓存陷阱**：`Integer.valueOf` 缓存 [-128, 127]；
  ```
  127 == 127 : true    ← 缓存内, 同一对象
  128 == 128 : false   ← 超出缓存, 不同对象
  ```
- 包装类比较：**永远用 `.equals()`**。

### 6.3 其他常用

- `BigDecimal`：金额计算唯一正确答案；
- `Math` / `Random`；
- **日期时间**：用 `LocalDate` / `LocalDateTime`（线程安全），**不要用旧的** `Date` / `Calendar`（可变、线程不安全、API 反人类）；
- **枚举 enum**：比 `static final int` 常量更优雅、更类型安全。

---

## 7. 集合框架（重中之重）

### 7.1 体系全图

```
Collection
├── List (有序, 可重复): ArrayList / LinkedList
├── Set  (去重):        HashSet / LinkedHashSet / TreeSet
└── Queue (队列):       ArrayDeque / PriorityQueue

Map (键值对):           HashMap / LinkedHashMap / TreeMap
```

### 7.2 List：ArrayList vs LinkedList

| 对比 | ArrayList | LinkedList |
|---|---|---|
| 底层 | 动态数组 | 双向链表 |
| 随机访问 `get(i)` | O(1) 快 | O(n) 慢 |
| 头部插删 | 慢（搬移元素）| 快 |
| 内存 | 紧凑 | 每个元素额外两个指针 |

> 结论：**99% 场景用 ArrayList**（CPU 缓存友好），LinkedList 几乎只在"频繁头尾操作"时有优势。

### 7.3 Set：去重的艺术

- `HashSet`：哈希去重，O(1)，无序；
- `TreeSet`：红黑树，自动排序，O(log n)；
- `LinkedHashSet`：保持插入顺序；
- **去重原理**：先比 `hashCode`，再比 `equals`——这也是"重写 equals 必须重写 hashCode"的原因。

### 7.4 Map：HashMap（面试靶心）

- 底层结构：**数组 + 链表 + 红黑树**（JDK8+，链表长度 ≥ 8 且容量 ≥ 64 转树）；
- 扩容：默认容量 16、负载因子 0.75、翻倍扩容、rehash；
- key 的 `equals` / `hashCode` 必须成对重写；
- **HashMap 线程不安全** → 并发场景用 `ConcurrentHashMap`。

### 7.5 泛型

- `<T>` 类型参数：编译期类型检查，免去强转（demo 里 `List<Person>` 放错类型直接编译报错）；
- 通配符 PECS 原则：`? extends T` 适合"只读（生产者）"，`? super T` 适合"只写（消费者）"；
- **类型擦除**：泛型只存在于编译期，运行时是 Object。

### 7.6 遍历与陷阱（demo 实测）

- for-each 底层就是迭代器；
- **迭代中删除元素** → 抛 `ConcurrentModificationException`（demo 里真实触发了！注意 3 个元素时可能"侥幸"不抛，4 个元素才稳定复现——这本身就是著名隐蔽点）；
- 正确姿势：`removeIf(...)` 或显式 `Iterator.remove()`。

### 7.7 选型速查

| 需求 | 选择 |
|---|---|
| 普通列表 | ArrayList |
| 去重 | HashSet |
| 去重且保序 | LinkedHashSet |
| 键值对 | HashMap |
| 键值对且有序 | TreeMap / LinkedHashMap |
| 并发 Map | ConcurrentHashMap |
| 队列 | ArrayDeque |

---

## 8. 异常处理

### 8.1 异常体系

```
Throwable
├── Error                    (系统级, 一般不处理: OOM / StackOverflowError)
└── Exception
    ├── RuntimeException     (非受检: NullPointerException / 越界 / 算术异常)
    └── 其他                 (受检: IOException / SQLException, 必须处理)
```

- **受检异常**：编译器强制你 try-catch 或 throws；
- **非受检异常**：多是代码 bug，应该"修代码"而不是"抓异常"。

### 8.2 try-catch-finally

- catch 按顺序匹配，**子类异常写前面**；
- finally：无论是否异常都执行（除非 `System.exit`）；
- **JDK7+ try-with-resources**（现代写法）：实现了 `AutoCloseable` 的资源自动关闭：
  ```java
  try (Connection conn = DriverManager.getConnection(url)) {
      // 自动关闭 conn, 不用写 finally
  }
  ```

### 8.3 throw vs throws

- `throw`：抛出一个异常对象（动作）；
- `throws`：在方法签名上声明"我可能抛出什么"（契约）。

### 8.4 最佳实践

- 不要吞异常（空 catch 块是"程序员之耻"）；
- 不要用异常做流程控制（慢且语义错）；
- 异常信息要带上下文（"用户 123 下单失败" 优于 "error"）；
- 记日志用 `logger.error("...", e)`，别只 `printStackTrace`。

---

## 9. IO 流（速览）

### 9.1 分类

| 维度 | 分类 |
|---|---|
| 方向 | 输入流 / 输出流 |
| 单位 | **字节流** `InputStream/OutputStream`（万能，处理二进制）｜ **字符流** `Reader/Writer`（文本，自动处理编码）|
| 功能 | 节点流（直接连数据源）/ 处理流（`Buffered...` 包装加速）|

### 9.2 现代写法（Files 工具类，优先使用）

```java
String content = Files.readString(Path.of("a.txt"));          // 一次读全
Files.writeString(Path.of("b.txt"), "内容");                  // 一次写全
try (var lines = Files.lines(Path.of("big.txt"))) {           // 大文件流式读
    lines.filter(s -> s.contains("Java")).forEach(System.out::println);
}
```

- 缓冲流（`BufferedInputStream`）的意义：减少系统调用次数；
- 序列化：`Serializable` 接口（现代工程更推荐 JSON）；
- NIO：`Path` / `Files` / `Channel`，非阻塞 IO 的基础。

---

## 10. 多线程基础

### 10.1 三种创建方式

1. 继承 `Thread`（不推荐，占用继承位）；
2. 实现 `Runnable`（推荐）；
3. 实现 `Callable` + `FutureTask`（有返回值）。

### 10.2 线程安全

- `synchronized`：互斥锁（方法级/代码块）；
- `volatile`：保证可见性、禁止重排序（不保证原子性！）；
- 原子类：`AtomicInteger`（CAS 无锁）；
- 并发容器：`ConcurrentHashMap` / `CopyOnWriteArrayList`。

### 10.3 线程池（工业必须）

- 不用 `Executors` 快捷工厂（队列无界易 OOM）——**手写 `ThreadPoolExecutor`**，参数可控：
  ```java
  new ThreadPoolExecutor(
      4,                    // 核心线程数
      8,                    // 最大线程数
      60L, TimeUnit.SECONDS,
      new ArrayBlockingQueue<>(100),   // 有界队列
      new ThreadPoolExecutor.CallerRunsPolicy());  // 拒绝策略
  ```
- 展望：JDK21 的**虚拟线程**正在改变高并发 IO 的写法（详见《学习报告》）。

---

## 11. 反射 / 注解（框架的基石）

- **反射**：运行时获取类信息、创建对象、调用方法：
  ```java
  Class<?> clazz = Class.forName("com.example.User");
  Object obj = clazz.getDeclaredConstructor().newInstance();
  ```
- **注解**：`@Override` 是编译期检查；自定义注解 + 反射 = 框架配置的模式；
- 一句话：**框架 = 反射 + 注解 + 泛型**（Spring / MyBatis 的底层魔法）。

---

## 12. 现代 Java（8+）必会清单

| 特性 | 版本 | 一句话 |
|---|---|---|
| Lambda | 8 | 函数作为参数传递 |
| Stream | 8 | 集合的声明式流水线 |
| Optional | 8 | 显式表达"可能为空" |
| 接口默认方法 | 8 | 接口也能进化 |
| var | 10 | 局部类型推断 |
| switch 表达式 | 14 | 能返回值的 switch |
| 文本块 | 15 | 多行字符串 `"""` |
| instanceof 模式匹配 | 16 | `if (o instanceof Dog d)` |
| record | 16 | 不可变数据类（demo 里的 Person） |
| sealed 类 | 17 | 受限继承 |

> 全部实测见 `javase-demo/` 运行输出（Demo1/2/3）。

---

## 13. 学习路线与常见误区

### 13.1 四阶段路线

| 阶段 | 内容 | 过关标准 |
|---|---|---|
| 1 | 基础语法 + OOP（本篇） | 不看资料写出 5.4 的多态 demo |
| 2 | 集合 + 异常 + IO + 多线程 | 说清 ArrayList/HashMap 原理 |
| 3 | 现代特性 + 反射注解 | 会用 Stream 处理数据 |
| 4 | Maven → JUnit → JDBC → Spring | 衔接《Java与SQL数据库-深度融合教学》|

### 13.2 十大常见误区（对照自查）

1. `==` 比较字符串 ✗ → `.equals()`；
2. 忽视整数溢出；
3. 金额用 double ✗ → BigDecimal；
4. 循环里 `+` 拼接字符串 ✗ → StringBuilder；
5. 迭代中直接删元素 ✗ → removeIf；
6. 空 catch 吞异常；
7. 重写 equals 不重写 hashCode；
8. 封装类用 `==` 比较 ✗ → equals；
9. 该用集合的地方硬用数组；
10. 局部变量未初始化就使用（编译器会拦，但笔试常考）。

---

## 附录：配套演示与运行方式

```
javase-demo/
├── src/Hello.java              # 字节码演示（javac → javap → java）
├── src/Demo1_Basics.java       # 类型系统与 7 个陷阱（已实测）
├── src/Demo2_Oop.java          # 封装/继承/多态/接口/static（已实测）
├── src/Demo3_Collections.java  # 集合/泛型/Stream/迭代陷阱（已实测）
└── out/                        # 编译产物
```

```bash
cd javase-demo
javac -encoding UTF-8 -d out src/*.java
java -Dfile.encoding=UTF-8 -cp out Demo1_Basics     # 类型与陷阱
java -Dfile.encoding=UTF-8 -cp out Demo2_Oop        # OOP 三大特性
java -Dfile.encoding=UTF-8 -cp out Demo3_Collections # 集合与 Stream
javap -c -cp out Hello                               # 看字节码
```

**下一步学习衔接**：集合原理深入 → Stream 实战 → 多线程 → JDBC（已有实战篇）→ Maven/Spring。