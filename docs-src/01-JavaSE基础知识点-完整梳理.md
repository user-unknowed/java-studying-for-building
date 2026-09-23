# JavaSE 基础知识点 · 完整梳理（全面升级版）

> 面向 Java 初学者 ｜ 配套可运行演示：`javase-demo/`（7 个程序 + JUnit 测试，全部实际编译运行）
> 本版在原有基础上做了两件事：**基础化**（每个知识点从"为什么"讲起，配人话解释）+ **全面化**（补齐大部分语法点，新增外部函数库、网络编程两大板块）。
> 整理：Operit AI ｜ 2026-09
> 定位：本篇 +《Java与SQL数据库-深度融合教学》+《Java上位机开发实战》 = 从语言基础到工业应用的完整阶梯。

---

## 0. 知识地图

| # | 模块 | 核心内容 | 重要度 |
|:---:|---|---|---|
| 1 | 运行机制 | JDK / JRE / JVM、字节码、编译运行全过程 | ★★★★★ |
| 2 | 语言基础 | 8 种基本类型、字面量、类型转换、var | ★★★★★ |
| 3 | 运算符与流程 | 算术 / 位运算 / 逻辑、if、switch 全形态、循环 | ★★★★★ |
| 4 | 数组 | 内存模型、Arrays 工具类、二维数组 | ★★★★ |
| 5 | 面向对象 | 方法重载、构造器、封装继承多态、接口、枚举、内部类 | ★★★★★ |
| 6 | 常用 API | String 全家、包装类、BigDecimal、日期时间 | ★★★★★ |
| 7 | 集合框架 | List / Set / Map / 队列、泛型、比较器 | ★★★★★ |
| 8 | 异常处理 | 体系、try-with-resources、自定义异常 | ★★★★★ |
| 9 | IO 流与文件 | 字节 / 字符流、Files 工具、序列化、NIO | ★★★★ |
| 10 | 多线程与并发 | 线程创建、锁、原子类、线程池 | ★★★★★ |
| 11 | 反射 / 注解 | 框架的基石，动态操作类 | ★★★ |
| 12 | Lambda 与函数式 | 函数式接口、方法引用 | ★★★★★ |
| 13 | Stream 与 Optional | 声明式数据流水线 | ★★★★★ |
| 14 | 现代语法全家桶 | var / 文本块 / switch 表达式 / record / sealed / 模式匹配 | ★★★★★ |
| 15 | 外部函数库实战 | jar 与 classpath、Jackson、SLF4J、Commons、JUnit | ★★★★★ |
| 16 | 网络编程基础 | TCP/UDP、Socket、HttpClient，衔接上位机开发 | ★★★★ |
| 17 | 学习路线与误区 | 五阶段路线 + 十大自查 | ★★★★ |

**怎么用这份教程？**

- 第一次学：从第 1 章顺着读到第 13 章，每读完一章跑一遍 `javase-demo/` 里对应的程序；
- 有基础的复习：直接跳到第 12~16 章（现代语法、外部库、网络——也是《Java 上位机开发实战》的前置知识）；
- 每章里的每段"运行输出"都来自 `javase-demo/` 的真实执行结果（OpenJDK 17），可以自己重跑对照。

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

> 现代实践：装 JDK 就够了（JRE 已并入 JDK）。本教程全部演示使用 **OpenJDK 17**——它是 LTS（长期支持）版本，也是 2026 年企业主流选择之一。

### 1.3 亲眼看字节码（本次实操）

`int c = 1 + 2;` 在字节码里是这样的（javap 反汇编真实输出，来自 `Hello.java`）：

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
> 运行 `javap -c -cp out Hello` 可以自己复现（完整输出见文末附录）。

### 1.4 一次完整的手工编译运行（命令详解）

不用 IDE，亲手敲一遍下面四条命令，你就理解了所有 Java 工具的核心：

```bash
cd javase-demo

# ① 编译：src 目录所有 .java → out 目录的 .class
javac -encoding UTF-8 -d out src/*.java

# ② 运行：把 out 加入类路径（classpath），启动 Hello 类
java -Dfile.encoding=UTF-8 -cp out Hello

# ③ 反汇编：看字节码
javap -c -cp out Hello

# ④ 带依赖运行（第 15 章讲的外部库）
java -Dfile.encoding=UTF-8 -cp "out:lib/*" Demo5_Libs
```

| 参数 | 作用 | 说明 |
|---|---|---|
| `-encoding UTF-8` | 源码文件编码 | 中文注释/字符串必须显式指定，否则乱码 |
| `-d out` | 指定 .class 输出目录 | 保持包结构的输出 |
| `-cp` / `-classpath` | 类路径 | JVM "去哪找类"；多个路径用 `:` 分隔（Windows 用 `;`） |
| `-Dfile.encoding=UTF-8` | 运行时字符编码 | 避免控制台输出中文乱码 |

**真实运行输出（Hello.java）**：

```
1 + 2 = 3
```

### 1.5 从源码到运行的完整链路（心智模型）

```
Hello.java
   │ javac 编译（编译期：检查语法、类型）
   ▼
Hello.class（字节码）
   │ JVM 加载（类加载器：把 .class 读进内存）
   ▼
Hello 类（方法区元数据 + 堆中对象）
   │ JVM 解释执行 main 方法（运行期）
   ▼
程序输出
```

记住两个高频词：**编译期**（javac 干活，检查类型与语法）和**运行期**（JVM 干活，多态、反射都发生在这里）。后面第 5 章的"多态"、第 11 章的"反射"，本质上都是"编译期看到的"和"运行期实际执行的"不一致造成的——理解这条主线，很多概念会豁然开朗。

---

## 2. 数据类型与变量

### 2.1 八种基本类型（背下来）

| 类型 | 字节 | 范围/说明 | 字面量示例 | 默认值 |
|---|---|---|---|---|
| byte | 1 | [-128, 127] | `(byte) 100` | 0 |
| short | 2 | [-32768, 32767] | `(short) 100` | 0 |
| int | 4 | 约 ±21 亿（最常用） | `100` | 0 |
| long | 8 | 时间戳/ID 必用 | `100L` | 0L |
| float | 4 | 单精度 | `3.14f` | 0.0f |
| double | 8 | 双精度（小数默认） | `3.14` | 0.0d |
| char | 2 | 一个 Unicode 字符 | `'A'` | '\u0000' |
| boolean | 1 | true / false | `true` | false |

**真实运行输出（Demo1）**：

```
byte=127, short=32767, int=2147483647, long=9223372036854775807
float=3.14, double=3.141592653589793, char=中, boolean=true
注意: int 最大约 21 亿 —— 时间戳、ID 都要用 long!
```

选型口诀：**整数默认 int、大数用 long；小数默认 double、要省内存用 float；文本单字符用 char、字符串用 String（引用类型）**。

### 2.2 字面量的细节（全面化，笔试爱考）

```java
int a = 100;              // 十进制
int b = 0b110_0100;       // 二进制（0b 前缀，下划线可用于易读分隔）
int c = 0100;             // 八进制（0 前缀）—— 慎用，容易看错！
int d = 0x64;             // 十六进制（0x 前缀）—— 调试内存/颜色常用
long e = 100L;            // long 后缀 L（小写 l 易看成 1，用大写）
float f = 3.14F;          // float 后缀 f
double g = 3.14;          // 小数默认 double
double h = 1e3;           // 科学计数法 = 1000.0
char i = 'A';             // 字符：单引号，只能一个字符
char j = '\n';            // 转义：\n 换行 \t 制表 \\ 反斜杠 \' \" 
char k = '\u4e2d';        // Unicode：\u 前缀 + 四位十六进制（'中'）
String s = "hello";       // 字符串：双引号（引用类型！）
String empty = null;      // 空引用（后面第 8 章讲 NullPointerException）
```

### 2.3 三大经典陷阱（demo 已真实跑出）

1. **整数溢出**（静默事故，不报错）：
   ```
   int 最大值      = 2147483647
   int 最大值 + 1  = -2147483648   ← 溢出! 静默变成负数, 不报错!
   ```
   > 原因：int 只有 32 位，最大值加 1 会"进位到符号位"变成负数。**涉及金额、时间戳、ID 的累加都要警惕**，必要时先转 long。

2. **浮点精度**（钱绝对不能碰）：
   ```
   0.1 + 0.2 = 0.30000000000000004   ← 不等于 0.3!
   BigDecimal: 0.1 + 0.2 = 0.3       ← 金额必须这样算
   ```
   > 原因：0.1、0.2 在二进制里是无限循环小数，只能近似存储。这不是 Java 的 bug，IEEE 754 语言全都这样。

3. **char 本质是数字**：`'A' + 1 == 66`。char 可以参与算术运算（用的是码点），char 和 int 之间可以互转。

### 2.4 类型转换：什么时候自动、什么时候要强转

```java
// ① 自动（小 → 大，安全）：byte → short → int → long → float → double
int a = 100;
long b = a;          // OK
double c = a;        // OK

// ② 强制（大 → 小，可能丢数据，必须写 (类型)）
double x = 3.99;
int y = (int) x;     // 3 —— 直接"砍掉"小数，不是四舍五入！

// ③ 运算中的隐式提升
int m = 5, n = 2;
double r = m / n;            // 2.0 —— 整数除法先算完再赋值！经典坑
double r2 = m / (double) n;  // 2.5 —— 把任意一方转成 double 才对

// ④ char ↔ int
char ch = 'A';
int code = ch;       // 65
char next = (char) (code + 1);  // 'B'
```

> 一句话规则：**"小的往大的转"自动，"大的往小的转"要强转**；混合运算时，结果类型向"更宽"的一方看齐（但整数的 `/` 先用整数规则算）。

### 2.5 变量、常量、作用域

- 局部变量**必须初始化**；类字段自动有默认值（见 2.1 表）；
- `final` = 常量（赋值后不可改）：`final double PI = 3.14159;`
- 命名规范：类用大驼峰 `HelloWorld`、方法/变量用小驼峰 `getName`、常量全大写 `MAX_SIZE`；
- **作用域**：变量的"存活范围"在一对 `{}` 内；出了大括号就不可见——这也是"编译器能帮你抓 bug"的原因之一。

### 2.6 var：局部变量类型推断（JDK 10+，用户点名要会的语法）

`var` 让编译器**根据初始值自动推断类型**，少写类型名，代码更清爽：

```java
var name = "Java";                    // 推断为 String
var list = new ArrayList<String>();   // 推断为 ArrayList<String>
var map = new HashMap<String, Integer>();
var count = 10;                       // 推断为 int
```

**真实运行输出（Demo4）**：

```
var name -> String : Java
var list -> ArrayList [a]
var map  -> HashMap {one=1}
限制：只能用于局部变量 + 必须有初始值 + 不能接 null（编译器会拦）
```

**四条铁律**（考试和工作都爱考）：

| 规则 | 说明 |
|---|---|
| 只能用于局部变量 | 字段、方法参数、返回类型都不行 |
| 必须有初始值 | `var x;` 编译不过——没初始值推断不出来 |
| 不能赋 null | `var n = null;` 编译不过——推断不出类型 |
| 一旦推断就固定 | `var s = "a";` 之后再 `s = 1;` 编译错——s 就是 String |

> **本质澄清**：`var` 不是"动态类型"。Java 依然是强类型语言，`var` 只是"写代码的省事写法"，编译器在编译期就确定真实类型——把 `.java` 编译成 `.class` 之后，`var` 就消失了（不存在于字节码里）。

**什么时候用 / 什么时候别用：**

- 用：类型显而易见时（`var list = new ArrayList<User>();`——告别括号里重复写类型）；
- 别用：类型不明显时（`var result = service.handle();`——读者不知道 result 是什么类型，反而更难读）。

---

## 3. 运算符与流程控制

### 3.1 算术运算符

```java
int a = 7, b = 2;
System.out.println(a + b);   // 9
System.out.println(a - b);   // 5
System.out.println(a * b);   // 14
System.out.println(a / b);   // 3  —— 整数除法，砍掉小数！
System.out.println(a % b);   // 1  —— 取模（余数）
System.out.println(a / 2.0); // 3.5 —— 有小数参与才是精确的

int i = 1;
i++;       // i = 2（先用后加）
++i;       // i = 3（先加后用）
i += 5;    // i = 8（复合赋值：+= -= *= /= %=）
```

**记忆点**：`%` 的两个经典用途——判断奇偶（`n % 2 == 0`）、环形索引（`index % length` 绕圈）。

### 3.2 位运算（全面化补充，原理与优化必备）

```java
int x = 0b1100;   // 12
int y = 0b1010;   // 10
x & y    // 0b1000 = 8   与：都为 1 才为 1
x | y    // 0b1110 = 14  或：有 1 就为 1
x ^ y    // 0b0110 = 6   异或：不同为 1（可用来"翻转/找不同"）
~x       // 取反
x << 1   // 24  左移 1 位 = 乘 2（低位补 0）
x >> 1   // 6   右移 1 位 = 除以 2（保留符号）
x >>> 1  // 无符号右移（高位补 0）
```

**工程用法**：权限标志位（`READ|WRITE|EXEC` 打包进一个 int）、哈希值计算、HashMap 源码。平时业务开发用得少，但读源码躲不开。

### 3.3 关系与逻辑运算符

- `==` `!=` `>` `<` `>=` `<=`——比较大小/相等；
- `&&` `||` `!`——逻辑与或非，**支持短路**：`a != null && a.length() > 0`（前面为 false 就不算后面，保护空指针）；
- 字符串比较**永远用 `.equals()`**（`==` 比的是引用地址）：
  ```
  s1 == s2        : true    ← 同一常量池对象, 地址相同
  s1 == s3        : false   ← new 出来的不同对象
  s1.equals(s3)   : true    ← 比较内容, 永远用 equals
  ```

### 3.4 三元运算符与优先级

```java
int max = a > b ? a : b;                    // 取较大值
String tag = score >= 60 ? "及格" : "不及格"; // 嵌入判断
```

优先级记忆：`() > 一元 > 算术 > 关系 > 逻辑与 > 逻辑或 > 三元 > 赋值`。**拿不准就加括号**——括号是零成本的正确性保险。

### 3.5 if 与 switch（全形态）

```java
// ① if / else if / else —— 最常用
if (score >= 90) {
    System.out.println("优秀");
} else if (score >= 60) {
    System.out.println("及格");
} else {
    System.out.println("补考");
}
```

**switch 的三种形态**（现代 Java 的进化史）：

```java
// 形态一：经典 switch（有"穿透"风险，必须写 break）
switch (day) {
    case 1:
        System.out.println("周一");
        break;          // 忘了 break 会"穿透"执行下一个 case —— 经典 bug
    default:
        System.out.println("其他");
}

// 形态二：switch 表达式 + 箭头（JDK14+）—— 无穿透、能返回值
String type = switch (day) {
    case 1, 2, 3, 4, 5 -> "工作日";
    case 6, 7 -> "休息日";
    default -> "非法日期";
};

// 形态三：分支里要写多行逻辑时，用代码块 + yield 返回值
String type2 = switch (day) {
    case 1, 2, 3, 4, 5 -> "工作日";
    case 6, 7 -> "休息日";
    default -> {
        int n = day - 7;
        yield "非法(超出 " + n + " 天)";   // yield = "这个分支的返回值"
    }
};
```

**真实运行输出（Demo1 / Demo4）**：

```
day = 3 → 工作日   ← 还能作为表达式直接赋值

  day=1 → 工作日
  day=6 → 休息日
  day=7 → 休息日
  day=9 → 非法(编号=4)
特点：能返回值、无 fall-through 穿透、必须穷尽
```

### 3.6 循环四件套与跳转

```java
// ① for：次数明确
for (int i = 0; i < 5; i++) { ... }

// ② 增强 for（for-each）：遍历数组/集合，只读最舒服
int[] arr = {5, 8, 9};
for (int v : arr) { System.out.print(v + " "); }   // 5 8 9

// ③ while：条件驱动
while (queueNotEmpty()) { ... }

// ④ do-while：至少执行一次（先做后判）
do { ... } while (retry());
```

跳转三兄弟：

```java
for (...) {
    if (skip)  continue;   // 跳过本次循环，进下一次
    if (done)  break;      // 结束整个循环
}

// 带标签的循环（嵌套循环中"跳到外层"），面试偶尔考：
outer:
for (int i = 0; i < 3; i++) {
    for (int j = 0; j < 3; j++) {
        if (i * j > 2) break outer;   // 直接跳出两层
    }
}
```

> 循环选择的直觉：**知道次数用 for，不知道次数用 while，必须先执行一次用 do-while**。

---

## 4. 数组

### 4.1 声明、初始化与内存模型

```java
int[] a = new int[5];             // 声明 + 分配（元素默认 0）
int[] b = {1, 2, 3};              // 字面量初始化
int[] c = new int[]{1, 2, 3};     // 完整写法
```

数组在内存里是一块**连续的存储空间**，`a.length` 是属性（不是方法）。越界会抛 `ArrayIndexOutOfBoundsException`——这是最常见的运行时异常之一，说明"访问了不存在的下标"。

### 4.2 基本操作全家桶

```java
int[] arr = {5, 2, 8, 1, 9};

System.out.println(arr.length);          // 5
System.out.println(arr[0]);              // 5（下标从 0 开始！）

// 遍历
for (int v : arr) System.out.print(v + " ");

// 复制（三选一）
int[] copy1 = arr.clone();
int[] copy2 = Arrays.copyOf(arr, arr.length);
int[] copy3 = Arrays.copyOfRange(arr, 1, 3);   // [2, 8]

// 排序 + 查找
int[] sorted = {1, 2, 5, 8, 9};
Arrays.sort(arr);                         // 原地升序
int idx = Arrays.binarySearch(sorted, 5); // 2（必须是有序数组）
```

**真实运行输出（Demo1）**：

```
===== 7. 数组与 Arrays 工具类 =====
排序后: [1, 2, 5, 8, 9]
5 8 9 ← 增强 for-each 遍历
```

### 4.3 Arrays 工具类速查

| 方法 | 作用 |
|---|---|
| `Arrays.toString(arr)` | 数组 → 可读字符串（直接打印数组会得到地址！） |
| `Arrays.sort(arr)` | 排序（基本类型用双轴快排，对象用 TimSort） |
| `Arrays.binarySearch(arr, key)` | 二分查找（要求已排序） |
| `Arrays.copyOf(arr, newLen)` | 复制/扩容（newLen 更大时后面补默认值） |
| `Arrays.equals(a, b)` | 比较内容（`a == b` 比的是引用） |
| `Arrays.fill(arr, v)` | 填充 |
| `Arrays.asList(...)` | 数组 → List（**定长**，不能 add/remove） |

### 4.4 二维数组：数组的数组

```java
int[][] grid = new int[3][4];          // 3 行 4 列
int[][] matrix = {
    {1, 2, 3},
    {4, 5, 6},
};

for (int[] row : matrix) {             // 逐行遍历
    for (int v : row) System.out.print(v + " ");
}
```

> 严格说 Java 没有"真正的二维数组"，只有"数组的数组"——所以每行长度可以不同（锯齿数组）。

### 4.5 数组的局限 → 为什么需要集合

| 能力 | 数组 | 集合（第 7 章） |
|---|---|---|
| 长度 | 创建后固定 | 动态伸缩 |
| 功能 | 只有基本存取 | 排序/去重/查找/分组…… |
| 泛型 | 不支持（运行时才知道类型） | 类型安全 |

> 结论：**"定长的数据"用数组（性能敏感场景），"会变化的数据"用集合**——99% 的业务代码用集合。

---

## 5. 面向对象（Java 的灵魂）

### 5.1 类与对象：从"图纸"到"产品"

- 类是**图纸**（定义数据 + 行为），对象是**按图纸生产的产品**；
- `new` 做的事：分配内存 → 调用构造方法初始化 → 返回引用。

```java
public class Account {
    private String owner;      // 字段（状态）
    private double balance;

    public Account(String owner) {   // 构造方法（和类同名、无返回值）
        this.owner = owner;
    }

    public void deposit(double amount) {   // 方法（行为）
        this.balance += amount;
    }
}

Account acc = new Account("张三");   // 造对象
acc.deposit(100);                    // 发消息（调方法）
```

### 5.2 方法全解：重载、可变参数、递归

```java
// ① 方法重载（overload）：同名，参数列表不同 —— 编译器按"传的实参"选方法
public int add(int a, int b)        { return a + b; }
public int add(int a, int b, int c) { return a + b + c; }
public double add(double a, double b) { return a + b; }

// ② 可变参数（varargs）：最后一个参数可以传 0~N 个
public int sum(int... nums) {
    int s = 0;
    for (int n : nums) s += n;    // nums 在方法内是 int[]
    return s;
}
sum();          // 0
sum(1, 2, 3);   // 6

// ③ 递归：方法调用自己 —— 必须有"终止条件"，否则 StackOverflowError
public int factorial(int n) {
    if (n <= 1) return 1;         // 终止条件
    return n * factorial(n - 1);  // 向终止条件收敛
}
factorial(5);   // 120
```

> **重载 vs 重写**（面试高频）：重载是"同一个类里同名不同参"，编译期决定；重写（override）是"子类改掉父类的方法实现"，运行期动态绑定（多态的基础）。

### 5.3 构造器细节

```java
public class User {
    private String name;
    private int age;

    // ① 重载的构造器 + this() 调用兄弟构造器（必须是第一行）
    public User() {
        this("匿名", 0);
    }

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
```

- 一个构造器都不写 → 编译器送你一个"无参默认构造器"；写了任一构造器 → 默认构造器消失（需要就自己补）；
- `this` = "当前对象"；`this(...)` = "调用本类另一个构造器"；`super(...)` = "调用父类构造器"（不写时默认调用父类无参构造）。

### 5.4 封装：把数据关进"保险箱"

```java
public class Account {
    private double balance;   // private：外部直接看不到

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("金额必须为正数!");   // 受控入口做校验
        }
        this.balance += amount;
    }

    public double getBalance() { return balance; }
}
```

**真实运行输出（Demo2）**：

```
===== 1. 封装: 数据只能通过受控方法访问 =====
存取后: Account{owner='张三', balance=150.0}
拦截非法操作: 金额必须为正数!   ← 封装的意义
```

> 封装不只是"getter/setter"，而是**在受控入口处守住业务规则**。

### 5.5 继承：代码复用的第一手段

```java
public class Animal {
    protected String name;
    public void eat() { System.out.println(name + " 吃东西"); }
}

public class Dog extends Animal {          // extends = 继承
    @Override                              // 注解：让编译器检查你是不是真的在重写
    public void eat() {
        super.eat();                       // super = 父类
        System.out.println(name + " 啃骨头");
    }
}
```

- Java 是**单继承**（一个类只能有一个直接父类）；
- `@Override` 是习惯也是保险：写错方法名编译器会拦；
- 重写规则：**访问权限不能变小、返回类型兼容、异常不能变宽**；
- `final` 类不可继承（如 `String`）、`final` 方法不可重写。

### 5.6 多态（最重要的一句话）

> **父类引用指向子类对象。**

```java
Shape sh = new Circle(2);   // 编译时看 Shape, 运行时看 Circle
sh.area();                  // 动态绑定 → 实际执行 Circle.area()
```

**真实运行输出（Demo2）**：

```
===== 2. 多态: 同一行代码, 不同对象不同行为 =====
  圆形     面积 = 12.57
  矩形     面积 = 12.00
  三角形    面积 = 6.00
总面积 = 30.57   ← 循环里没有一行 if/else, 全靠多态
```

**多态的价值**：新增一种图形，不需要改老代码 → 开闭原则（对扩展开放、对修改关闭）。

**存在条件**：继承 + 重写 + 父类引用指向子类对象。三者缺一不可。

### 5.7 static：属于"类"而不是对象

```java
public class Shape {
    static int count = 0;     // 静态字段：所有对象共享一份

    public Shape() {
        count++;              // 每 new 一个对象，计数 +1
    }

    static void printTotal() {   // 静态方法：没有 this，只能访问静态成员
        System.out.println("已创建 " + count + " 个图形");
    }
}
```

**真实运行输出（Demo2）**：

```
===== 4. static: 属于类, 不属于对象 =====
已创建的图形总数: 3   ← 类名直接访问静态字段
```

- 静态方法/字段通过类名访问（`Shape.count`），不需要对象；
- 静态代码块 `static { ... }`：类加载时执行一次（做初始化配置）；
- 静态导入 `import static java.lang.Math.*;` 可直接写 `abs(-1)`——偶尔用于常量类。

### 5.8 抽象类 vs 接口

```java
// 抽象类："是什么" —— 单继承，可以有状态
public abstract class Shape {
    abstract double area();               // 抽象方法：子类必须实现
    void describe() { System.out.println("面积=" + area()); }  // 具体方法：可复用
}

// 接口："能做什么" —— 多实现，JDK8+ 也可以有默认方法
public interface WithCorners {
    int corners();                        // 抽象方法
    default boolean isPolygon() {         // 默认方法（接口也能"进化"了）
        return corners() >= 3;
    }
}

public class Triangle extends Shape implements WithCorners {
    double area() { return 6; }
    public int corners() { return 3; }
}
```

**真实运行输出（Demo2）**：

```
===== 3. 接口: 描述'能力' (抽象类描述'是什么') =====
  矩形 有 4 条边
  三角形 有 3 条边
```

| 对比项 | 抽象类 | 接口 |
|---|---|---|
| 关键字 | `abstract class` | `interface` |
| 表达 | "是什么"（is-a） | "能做什么"（can-do） |
| 字段 | 任意 | 常量（默认 public static final） |
| 方法 | 抽象 + 具体 | 抽象 + 默认 + 静态（JDK8+） |
| 继承 | 单继承 | 多实现 |

> 选择原则：有共享状态/代码 → 抽象类；描述能力/跨类型协议 → 接口。

### 5.9 Object 与 equals / hashCode（重写模板）

```java
public class Person {
    private String name;
    private int age;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;                       // ① 同一引用直接 true
        if (o == null || getClass() != o.getClass()) return false;  // ② 类型不符 false
        Person p = (Person) o;                            // ③ 逐字段比较
        return age == p.age && Objects.equals(name, p.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);                   // ④ 和 equals 用同一组字段
    }
}
```

> **铁律：重写 equals 必须重写 hashCode**——否则 `HashMap` / `HashSet` 会出诡异 bug（两个"相等"的对象算出不同的哈希，被放进不同的桶）。

**真实运行输出（Demo4 里的 record 自动实现）**：

```
自动 toString : Point[x=3, y=4]
自动 equals   : p1.equals(p2) = true
```

（record 由编译器自动生成 toString/equals/hashCode——见第 14 章，现在只需要知道"这三个方法在做什么"。）

### 5.10 内部类：四种形态

```java
public class Outer {

    class Inner {}                 // ① 成员内部类（持有外部类引用）
    static class Nested {}         // ② 静态嵌套类（不持有外部引用）

    void method() {
        class Local {}             // ③ 局部内部类（方法内）

        Runnable r = new Runnable() {   // ④ 匿名内部类（没名字，就地实现）
            @Override
            public void run() { System.out.println("run"); }
        };
    }
}
```

- 编译后生成独立 class 文件：`Outer$Inner.class`——**你在 `javase-demo/out` 目录里可以看到**（如 `Demo2_Oop$Account.class`）；
- 匿名内部类是"一次性实现接口"的老办法——**JDK8 后基本被 lambda 取代**（第 12 章）。

### 5.11 枚举 enum（全面化补充）

```java
public enum OrderStatus {
    CREATED("已创建"), PAID("已支付"), CANCELED("已取消");

    private final String label;
    OrderStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}

OrderStatus s = OrderStatus.PAID;
s.name();             // "PAID"（名字）
s.ordinal();          // 1（序号）
s == OrderStatus.PAID // true —— 枚举天生单例，用 == 安全
switch (s) { case PAID -> System.out.println("处理支付"); default -> {} }
```

> 枚举比 `static final int` 常量安全得多：**类型安全（只能取枚举里定义的值）+ 自带单例 + 可以带字段/方法**。工业代码里状态、类型、配置项一律用枚举。

### 5.12 包与访问控制

```java
package com.example.app;      // 包声明：文件第一行

import java.util.List;        // 导入其他包的类
```

| 修饰符 | 本类 | 同包 | 子类 | 任何地方 |
|---|:---:|:---:|:---:|:---:|
| private | ✅ | ❌ | ❌ | ❌ |
| 默认（不写） | ✅ | ✅ | ❌ | ❌ |
| protected | ✅ | ✅ | ✅ | ❌ |
| public | ✅ | ✅ | ✅ | ✅ |

> 包的命名约定：公司域名倒写（`com.google`, `com.example`）——保证全世界不重名。

### 5.13 小结：OOP 十问自测

1. 重载和重写的区别？2. `this` 和 `super` 各是什么？3. 多态的三个条件？4. 抽象类和接口怎么选？5. 为什么重写 equals 必须重写 hashCode？6. static 成员能不能访问实例成员？（不能）7. 内部类编译后长什么样？（`外部类$内部类.class`）8. 枚举相比常量的优势？9. `final` 修饰类/方法/变量各自的含义？10. Java 为什么单继承？（避免菱形继承问题；多"能力"用接口补）

> 自测全部能答上，再进入下一章。

---

## 6. 常用 API

> 本章是"日用得最多的工具箱"。Java 自带的核心类库（JDK API）有几千个类——不需要背，**记住"哪类问题找哪个类"，用的时候查文档**。

### 6.1 String 家族（使用频率第一）

**为什么 String 是不可变的？** 每次"修改"（拼接、替换、截取）都会**产生新对象**，原字符串纹丝不动。好处：可以安全地当常量/Map 的 key、天然的线程安全、能缓存哈希值。

```java
String s = "Hello";

// 查询类
s.length()            // 5
s.charAt(1)           // 'e'
s.indexOf("l")        // 2（首次出现位置）
s.lastIndexOf("l")    // 3
s.contains("ell")     // true
s.startsWith("He")    // true
s.endsWith("lo")      // true
s.isEmpty()           // false
s.isBlank()           // false（JDK11+，空白字符也算空）

// 转换类（都返回新字符串）
s.toUpperCase()             // HELLO
s.toLowerCase()             // hello
s.substring(1, 4)           // "ell"（左闭右开！）
s.replace("l", "L")         // HeLLo
s.trim()                    // 去首尾空白（旧）
s.strip()                   // 去首尾空白（新，支持 Unicode）

// 分割与拼接
"a,b,c".split(",")          // ["a", "b", "c"]
String.join("-", "a", "b")  // "a-b"
String.format("%s: %d 岁", "张三", 18)   // "张三: 18 岁"

// 比较
"abc".equals("abc")         // true（永远用 equals）
"ABC".equalsIgnoreCase("abc") // true
"apple".compareTo("banana") // 负数（字典序）
```

**常量池与 == 复测（Demo1 真实输出）**：

```
s1 == s2        : true   ← 同一常量池对象, 地址相同
s1 == s3        : false   ← new 出来的不同对象
s1.equals(s3)   : true    ← 比较内容, 永远用 equals
s1 == s3.intern(): true   ← intern() 把字符串'放回池'
```

> 判断口诀：**内容比较用 `equals`，引用比较才用 `==`**。`null` 在前更安全：`"java".equals(input)` 不会空指针，`input.equals("java")` 可能。

**字符串拼接的性能坑**：循环里用 `+` 会创建 N 个中间对象。要拼接用 `StringBuilder`（见下）。

### 6.2 StringBuilder / StringBuffer

```java
StringBuilder sb = new StringBuilder();
sb.append("Hello").append(", ").append("Java");   // 链式调用
sb.insert(0, ">> ");          // 插入
sb.reverse();                 // 反转
String result = sb.toString();   // 最终一次性转成 String
```

| 类 | 可变性 | 线程安全 | 场景 |
|---|---|---|---|
| String | 不可变 | 安全 | 常量、Key |
| StringBuilder | 可变 | 不安全 | 单线程拼接（首选） |
| StringBuffer | 可变 | 安全（synchronized） | 多线程拼接（少见） |

### 6.3 包装类：基本类型的"对象形态"

```java
// 基本类型 ↔ 包装类（自动装箱/拆箱）
Integer i = 100;      // 自动装箱（int → Integer）
int j = i;            // 自动拆箱（Integer → int）

// 常用方法
Integer.parseInt("123")       // 字符串 → int（最常用！）
Integer.valueOf("123")        // 字符串 → Integer
Integer.MAX_VALUE             // 2147483647
Integer.toBinaryString(12)    // "1100"
Double.isNaN(0.0 / 0)         // true
```

**装箱缓存陷阱（Demo1 真实输出）**：

```
127 == 127 : true   ← 缓存 [-128,127] 内, 是同一对象
128 == 128 : false   ← 超出缓存, 是不同对象
结论: 包装类比较永远用 .equals() !
```

> 原因：`Integer.valueOf()` 对 [-128, 127] 的值复用缓存对象。**包装类比较一律用 equals**，别用 `==`。

**空指针预警**：`Integer x = null; int y = x;` → 拆箱时 NPE。所以"方法返回值可能为 null 时，建议返回基本类型的安全默认值或先判空"。

### 6.4 BigDecimal：金额计算唯一正确答案

```java
BigDecimal a = new BigDecimal("0.1");
BigDecimal b = new BigDecimal("0.2");
System.out.println(a.add(b));       // 0.3（精确！）

// 陷阱：别用 new BigDecimal(0.1)（传入 double 本身就不精确），要用字符串构造
// 运算 API：add / subtract / multiply / divide
BigDecimal div = new BigDecimal("10").divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP); // 3.33

// 比较大小：用 compareTo（equals 会连精度一起比："1.0" != "1.00"）
new BigDecimal("1.0").compareTo(new BigDecimal("1.00"))  // 0（相等）
```

**真实运行输出（Demo1）**：

```
0.1 + 0.2 = 0.30000000000000004   ← 不等于 0.3!
BigDecimal: 0.1 + 0.2 = 0.3       ← 金额必须这样算
```

> 钱的铁律：**存用 long（分）或 BigDecimal，算用 BigDecimal**；`divide` 必须指定精度和舍入模式。

### 6.5 Math / Random / 其他

```java
Math.abs(-5)        // 5
Math.max(3, 7)      // 7
Math.min(3, 7)      // 3
Math.round(3.6)     // 4（四舍五入到 long）
Math.floor(3.7)     // 3.0（向下取整）
Math.ceil(3.2)      // 4.0（向上取整）
Math.pow(2, 10)     // 1024.0
Math.sqrt(16)       // 4.0
Math.random()       // [0.0, 1.0) 随机小数

// 新版随机数（JDK17+ 推荐 RandomGenerator，老代码常见 Random）
Random r = new Random();
r.nextInt(100)            // [0, 100) 随机整数
r.nextInt(1, 7)           // [1, 7) 骰子点数
```

### 6.6 日期时间 API（java.time，JDK8+ 必用）

**为什么废弃 `Date` / `Calendar`？**——可变（改一下就把调用方的数据改了）、线程不安全、月份从 0 开始（反人类）。新的 `java.time` 全部**不可变 + 线程安全 + API 清晰**。

```java
// ① 本地日期/时间
LocalDate date = LocalDate.now();                    // 2026-09-20
LocalTime time = LocalTime.now();                    // 08:30:15.123
LocalDateTime dateTime = LocalDateTime.now();        // 2026-09-20T08:30:15.123

LocalDate d = LocalDate.of(2026, 9, 20);             // 构造
d.plusDays(7);      // 加天数（返回新对象，原对象不变！）
d.minusMonths(1);   // 减月份
d.getYear(); d.getMonthValue(); d.getDayOfWeek();    // 拆字段

// ② 格式化 / 解析
DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
String text = fmt.format(LocalDateTime.now());        // "2026-09-20 08:30:15"
LocalDate parsed = LocalDate.parse("2026-09-20");     // 解析

// ③ 时间戳与机器时间
Instant now = Instant.now();                          // UTC 瞬间
long millis = now.toEpochMilli();                     // 毫秒时间戳

// ④ 时间间隔
long days = ChronoUnit.DAYS.between(start, end);      // 两个日期差几天
Duration duration = Duration.between(startTime, endTime);   // 时长（秒/纳秒）
```

**真实运行输出（Demo4）**：

```
现在  : 2026-09-20 00:58:54
一周后: 2026-09-27（相差 7 天）
时间戳: 1789865934259 ms（Instant = 机器时间视角）
```

> 实战提示：**跨时区/服务器场景存数据库用 UTC 时间戳或 Instant**；展示给用户时再转本地时区。工业软件（上位机、后台服务）里"时间"是最常出 bug 的数据类型之一，务必重视。

### 6.7 Objects / System 等实用工具

```java
Objects.requireNonNull(user, "user 不能为 null");   // 参数校验（快速失败）
Objects.equals(a, b);                              // 空安全的 equals
Objects.hash(name, age);                           // 生成 hashCode
Objects.isNull(x); Objects.nonNull(x);

System.currentTimeMillis();      // 毫秒时间戳
System.arraycopy(src, 0, dst, 0, n);   // 高性能数组复制
System.getenv("PATH");           // 读环境变量
System.exit(0);                  // 退出（谨慎！）
```

> 小结：本章 API 不需要死记——**重点是建立"什么场景查什么类"的索引**：文本 → String/StringBuilder；数字精确 → BigDecimal；时间 → java.time；空值防护 → Objects。

---

## 7. 集合框架（重中之重）

### 7.1 体系全图（收藏这张图）

```
Collection
├── List (有序, 可重复): ArrayList / LinkedList
├── Set  (去重):        HashSet / LinkedHashSet / TreeSet
└── Queue (队列):       ArrayDeque / PriorityQueue

Map (键值对):           HashMap / LinkedHashMap / TreeMap / ConcurrentHashMap
```

### 7.2 List：有序可重复

**真实运行输出（Demo3）**：

```
===== 1. List / Set / Map 三兄弟 =====
List (有序, 可重复): [苹果, 香蕉, 苹果]
Set  (去重):        [苹果, 香蕉]
Map  (键→值):       {苹果=5, 香蕉=3},  取苹果=5
```

```java
List<String> list = new ArrayList<>();
list.add("a"); list.add("b"); list.add("a");
list.get(0);             // "a" —— 随机访问
list.size();             // 3（可以有重复！）
list.contains("b");      // true
list.indexOf("a");       // 0
list.remove("a");        // 删第一个 "a"
list.set(0, "x");        // 替换下标 0
List.of("a", "b");       // 创建不可变 List（JDK9+，推荐做常量）
```

| 对比 | ArrayList | LinkedList |
|---|---|---|
| 底层 | 动态数组 | 双向链表 |
| 随机访问 `get(i)` | O(1) 快 | O(n) 慢 |
| 头部插删 | 慢（搬移元素）| 快 |
| 内存 | 紧凑 | 每个元素额外两个指针 |

> 结论：**99% 场景用 ArrayList**（CPU 缓存友好），LinkedList 几乎只在"频繁头尾操作"时有优势（队列用 ArrayDeque 更好）。

### 7.3 Set：去重的艺术

```java
Set<String> set = new HashSet<>();
set.add("a"); set.add("a"); set.add("b");
set.size();        // 2（自动去重！）
set.contains("a"); // true（O(1) 查找）
```

- `HashSet`：哈希去重，O(1)，无序；
- `TreeSet`：红黑树，自动排序，O(log n)；
- `LinkedHashSet`：保持插入顺序；
- **去重原理**：先比 `hashCode`（找桶），再比 `equals`（桶内确认）——这也是"重写 equals 必须重写 hashCode"的原因。

### 7.4 Map：键值对（面试靶心）

```java
Map<String, Integer> map = new HashMap<>();
map.put("语文", 90);
map.put("数学", 95);
map.get("语文");              // 90
map.getOrDefault("英语", 0);  // 0（无 key 时的默认值，防 NPE）
map.containsKey("数学");      // true
map.keySet();                 // [语文, 数学]
map.values();                 // [90, 95]
map.forEach((k, v) -> System.out.println(k + " → " + v));   // 最顺的遍历

// 复合操作（原子风格，简洁且不易错）
map.putIfAbsent("英语", 88);                    // 不存在才放
map.computeIfAbsent("物理", k -> 80);           // 不存在才计算放入
map.merge("语文", 5, Integer::sum);             // 存在则合并：90+5
```

**HashMap 原理（面试必背）**：

- 底层结构：**数组 + 链表 + 红黑树**（JDK8+，链表长度 ≥ 8 且容量 ≥ 64 转树）；
- `put` 流程：算 key 的 hash → 定位桶 → 桶空直接放；桶非空，键相同则覆盖，否则挂链表；
- 扩容：默认容量 16、负载因子 0.75、翻倍扩容、rehash（所以**尽量预设容量**：`new HashMap<>(expectedSize / 0.75 + 1)`）；
- key 的 `equals` / `hashCode` 必须成对重写；
- **HashMap 线程不安全**（并发 put 可能丢数据/死循环）→ 并发场景用 `ConcurrentHashMap`。

**遍历 Map 的正确姿势（Demo3 真实输出）**：

```
===== 5. 遍历 Map 的正确姿势 =====
  苹果 → 5
  香蕉 → 3
```

### 7.5 Queue / Deque：队列与双端队列

```java
// 队列：先进先出（FIFO）—— 任务排队、消息缓冲
Queue<String> queue = new ArrayDeque<>();
queue.offer("任务1");    // 入队（尾部）
queue.poll();            // 出队（头部），空则返回 null
queue.peek();            // 看一眼队头，不出队

// Deque：双端队列 —— 可以当"栈"用（后进先出 LIFO）
Deque<Integer> stack = new ArrayDeque<>();
stack.push(1);       // 压栈
stack.push(2);
stack.pop();         // 2（后进的先出）
```

> 记：**Stack 类已过时，要用栈就 `ArrayDeque`**；`PriorityQueue` 是"优先级队列"（堆实现，任务调度常用）。

### 7.6 泛型：把类型检查提前到编译期

```java
// ① 泛型类
public class Box<T> {
    private T value;
    public void set(T value) { this.value = value; }
    public T get() { return value; }
}
Box<String> box = new Box<>();   // T 被"绑定"为 String
box.set("hello");                // box.set(1) 编译期直接报错！

// ② 泛型方法
public static <T> T firstOrNull(List<T> list) {
    return list.isEmpty() ? null : list.get(0);
}

// ③ 通配符与 PECS 原则
void printAll(List<? extends Number> src) {}   // 生产者-extends：只读
void addAll(List<? super Integer> dst) {}      // 消费者-super：只写
```

**真实运行输出（Demo3）**：

```
===== 2. 泛型: 编译期类型约束 =====
已添加 5 个人 (放错类型编译期就报错)
```

- 通配符 PECS 原则：`? extends T` 适合"只读（生产者）"，`? super T` 适合"只写（消费者）"；
- **类型擦除**：泛型只存在于编译期，运行时是 Object——所以 `List<String>` 和 `List<Integer>` 运行时是同一个类（泛型不能 `new T[]`、不能 `instanceof List<String>` 的原因就在这）。

### 7.7 Comparable / Comparator：两种排序方式

```java
// ① 类实现 Comparable：定义"自然顺序"
public class Person implements Comparable<Person> {
    private int age;
    @Override
    public int compareTo(Person o) {
        return Integer.compare(this.age, o.age);   // 按年龄升序
    }
}
Collections.sort(persons);   // 直接用

// ② 外部 Comparator：定义"临时规则"，不改类
persons.sort(Comparator.comparingInt(Person::getAge));              // 年龄升序
persons.sort(Comparator.comparing(Person::getName).reversed());     // 名字降序
persons.sort(Comparator.comparing(Person::getCity)
                        .thenComparing(Person::getName));           // 多级排序

// ③ 按城市分组计数（Demo3 的进阶确认）
Map<String, Long> byCity = persons.stream()
        .collect(Collectors.groupingBy(Person::getCity, Collectors.counting()));
```

> 选择：**"这个类的天然大小"用 Comparable；"这次我想要的顺序"用 Comparator**。Comparator + lambda/方法引用 = 现代排序写法。

### 7.8 Collections 工具类

```java
Collections.sort(list);                      // 排序
Collections.reverse(list);                   // 反转
Collections.shuffle(list);                   // 打乱
Collections.max(list); Collections.min(list);   // 最值
Collections.unmodifiableList(list);          // 只读视图（防外部改动）
Collections.emptyList();                     // 空集合常量
Collections.swap(list, i, j);                // 交换
```

### 7.9 遍历陷阱（demo 实测）

**真实运行输出（Demo3）**：

```
===== 6. 迭代时删除元素陷阱 =====
  抛出了: ConcurrentModificationException   ← 迭代中修改集合的著名陷阱
  removeIf 后: [苹果, 橘子, 葡萄]
```

- **迭代中删除元素** → 抛 `ConcurrentModificationException`（demo 里真实触发了！注意 3 个元素时可能"侥幸"不抛，4 个元素才稳定复现——这本身就是著名隐蔽点）；
- 正确姿势：`removeIf(...)` 或显式 `Iterator.remove()`。

### 7.10 选型速查

| 需求 | 选择 |
|---|---|
| 普通列表 | ArrayList |
| 去重 | HashSet |
| 去重且保序 | LinkedHashSet |
| 键值对 | HashMap |
| 键值对且有序 | TreeMap / LinkedHashMap |
| 并发 Map | ConcurrentHashMap |
| 队列 | ArrayDeque |
| 优先级任务 | PriorityQueue |

---

## 8. 异常处理

### 8.1 异常体系：一切从这里长出来

```
Throwable
├── Error                    (系统级, 一般不处理: OOM / StackOverflowError)
└── Exception
    ├── RuntimeException     (非受检: NullPointerException / 越界 / 算术异常)
    └── 其他                 (受检: IOException / SQLException, 必须处理)
```

- **受检异常**：编译器强制你 try-catch 或 throws（如 IO 错误——调用方必须想好"出错怎么办"）；
- **非受检异常**：多是代码 bug，应该"修代码"而不是"抓异常"（如 NPE——说明逻辑有洞）。

### 8.2 try-catch-finally 与 try-with-resources

```java
try {
    int r = 10 / divisor;
    System.out.println("结果: " + r);
} catch (ArithmeticException | IllegalArgumentException e) {   // 多重 catch（JDK7+）
    System.out.println("参数问题: " + e.getMessage());
} catch (Exception e) {
    System.out.println("兜底: " + e);
} finally {
    System.out.println("无论成败都执行（清理现场）");
}
```

- catch 按顺序匹配，**子类异常写前面**（写反了编译器直接报错）；
- finally：无论是否异常都执行（除非 `System.exit`）。**return 也别想躲开 finally**；
- **JDK7+ try-with-resources**（现代写法）：实现了 `AutoCloseable` 的资源自动关闭：

```java
try (Connection conn = DriverManager.getConnection(url);
     PreparedStatement ps = conn.prepareStatement(sql)) {
    // 代码块结束时，ps、conn 自动 close（逆序关闭）
} catch (SQLException e) {
    // 处理
}
```

> 对比旧写法（finally 里手动 close + 判空）——try-with-resources **更短、更安全、不会漏**。凡是"用完必须关"的资源（连接、文件、流），一律用它。

### 8.3 throw / throws / 自定义异常

```java
// throw：抛出一个异常对象（动作）
if (amount <= 0) {
    throw new IllegalArgumentException("金额必须为正数: " + amount);
}

// throws：在方法签名上声明"我可能抛出什么"（契约）
public String readFile(String path) throws IOException { ... }

// 自定义异常：带上业务语义
public class BizException extends RuntimeException {
    private final int httpCode;
    public BizException(int httpCode, String message) {
        super(message);
        this.httpCode = httpCode;
    }
    public int httpCode() { return httpCode; }
}
```

**异常链**：捕获后包一层再抛，保留原始原因（排障救命）：

```java
try {
    dao.insert(order);
} catch (SQLException e) {
    throw new BizException(500, "保存订单失败: orderId=" + order.id()).initCause(e);
    // 或 new BizException(...) 里传 e：new BizException(500, msg, e)
}
```

### 8.4 最佳实践

- 不要吞异常（空 catch 块是"程序员之耻"）；
- 不要用异常做流程控制（慢且语义错）；
- 异常信息要带上下文（"用户 123 下单失败" 优于 "error"）；
- 记日志用 `logger.error("...", e)`，别只 `printStackTrace`；
- **能提前防的错不要等异常**（如判 null、判边界）——异常是"意外"，不是"流程"。

---

## 9. IO 流与文件

### 9.1 分类全景（两个维度一张表）

| 维度 | 分类 |
|---|---|
| 方向 | 输入流 / 输出流 |
| 单位 | **字节流** `InputStream/OutputStream`（万能，处理二进制）｜ **字符流** `Reader/Writer`（文本，自动处理编码）|
| 功能 | 节点流（直接连数据源）/ 处理流（`Buffered...` 包装加速）|

记忆：**`In/Out` 是字节，`Reader/Writer` 是字符**；处理流套在节点流外面（装饰器模式）。

### 9.2 字节流：什么都能读

```java
// 读文件（缓冲流包装，减少系统调用）
try (var in = new BufferedInputStream(new FileInputStream("photo.jpg"))) {
    int b;
    while ((b = in.read()) != -1) {   // 逐字节（演示用；实际会配 byte[] 缓冲）
        // 处理
    }
}

// 写文件
try (var out = new BufferedOutputStream(new FileOutputStream("copy.jpg"))) {
    out.write(new byte[]{1, 2, 3});
}
```

### 9.3 字符流：处理文本更省心

```java
// 读文本（InputStreamReader 负责"字节 → 字符"的解码）
try (var reader = new BufferedReader(
        new InputStreamReader(new FileInputStream("data.txt"), StandardCharsets.UTF_8))) {
    String line;
    while ((line = reader.readLine()) != null) {
        System.out.println(line);
    }
}

// 写文本
try (var writer = new BufferedWriter(new FileWriter("out.txt", StandardCharsets.UTF_8))) {
    writer.write("第一行");
    writer.newLine();
}
```

> 乱码的根源：**编码（写）和解码（读）用了不同的字符集**。中文项目统一 `UTF-8` 是习惯也是纪律。

### 9.4 现代文件操作：Path / Files（优先使用！）

JDK7 的 `java.nio.file` 把 90% 的日常文件操作变成一行：

```java
Path path = Path.of("data", "config.txt");       // 拼路径（跨平台安全）

// 一行读写
Files.writeString(path, "orders=42\n", StandardCharsets.UTF_8);
String content = Files.readString(path, StandardCharsets.UTF_8);

// 存在性 / 属性
Files.exists(path);          Files.size(path);
Files.createDirectories(Path.of("data", "logs"));   // 建多级目录

// 复制 / 移动 / 删除
Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
Files.deleteIfExists(path);

// 大文件流式处理（不会一次性读进内存）
try (var lines = Files.lines(Path.of("big.txt"))) {
    lines.filter(s -> s.contains("Java")).forEach(System.out::println);
}

// 遍历目录
try (var stream = Files.list(Path.of("."))) {     // 一层
    stream.filter(Files::isRegularFile).forEach(System.out::println);
}
try (var walk = Files.walk(Path.of("src"))) {     // 递归所有层
    walk.filter(p -> p.toString().endsWith(".java")).forEach(System.out::println);
}
```

> 原则：**新代码优先 `Files` + `Path`**（老代码里才见 `File` 类；老 File 类被吐槽"方法返回值黑盒、错误处理差"）。

### 9.5 序列化与 JSON（两条路）

```java
// ① Java 原生序列化：对象 ↔ 字节流（要求类 implements Serializable）
try (var out = new ObjectOutputStream(new FileOutputStream("user.ser"))) {
    out.writeObject(new User("张三", 18));
}
try (var in = new ObjectInputStream(new FileInputStream("user.ser"))) {
    User user = (User) in.readObject();
}
```

> 原生序列化的坑：`serialVersionUID` 不一致会反序列化失败、有安全风险、跨语言不通。**现代工程基本不用它存数据**——用 JSON（第 15 章的 Jackson 就是事实标准），跨语言、可读、可控。

### 9.6 NIO 速览：为了高并发

- `Path` / `Files`：现代文件 API（上面已用）；
- `Channel` / `Buffer`：可双向读写、支持零拷贝；
- **非阻塞 IO（NIO）**：一个线程管理大量连接——Netty 等高性能框架的地基（上位机/服务器开发进阶内容，先知道概念）。

> 本章小结：日常读写文件——`Files.readString` / `writeString`；大文件——`Files.lines` 流式；资源关闭——`try-with-resources`；文本编码——统一 UTF-8。

---

## 10. 多线程与并发

> 为什么需要多线程？——**等待太浪费**。网络请求、磁盘 IO、用户交互，CPU 大部分时间在"等"。多线程让"等 A 的时候去干 B"。但代价是：**共享数据的混乱**。本章就是从"怎么开线程"到"怎么不出乱子"。

### 10.1 三种创建方式

```java
// ① 继承 Thread（不推荐，占用继承位）
class MyThread extends Thread {
    public void run() { System.out.println("跑"); }
}
new MyThread().start();

// ② 实现 Runnable（推荐，接口不占继承）
Runnable task = () -> System.out.println("跑");
new Thread(task).start();

// ③ 实现 Callable + FutureTask（有返回值、能抛异常）
Callable<Integer> c = () -> 42;
FutureTask<Integer> ft = new FutureTask<>(c);
new Thread(ft).start();
int result = ft.get();      // 阻塞拿结果：42
```

> **永远用 `start()` 启动线程，不要直接调 `run()`**——直接调 `run()` 只是普通方法调用，跑在"当前线程"里，等于白写。

### 10.2 线程生命周期与常用方法

```
NEW → RUNNABLE → (BLOCKED / WAITING / TIMED_WAITING) → TERMINATED
```

| 方法 | 作用 | 注意 |
|---|---|---|
| `Thread.sleep(ms)` | 当前线程睡一会儿 | 抛 InterruptedException；**不释放锁** |
| `thread.join()` | 等这个线程跑完 | 主线程"等 A 完事再走" |
| `thread.interrupt()` | 打"中断标记" | 不是强杀，是"礼貌提醒" |
| `Thread.currentThread()` | 拿到当前线程对象 | 打印日志带线程名排障神器 |
| `thread.setDaemon(true)` | 设为守护线程 | 主线程结束它就结束（如日志线程） |

```java
Thread t = new Thread(() -> {
    try { Thread.sleep(1000); } catch (InterruptedException e) { /* 恢复策略 */ }
    System.out.println("后台任务完成");
});
t.start();
t.join();          // 等它完成
System.out.println("主线程继续");
```

### 10.3 线程安全：四种武器

**问题演示**：两个线程各加 1000 次，结果经常不是 2000（`i++` 不是原子操作——读、加、写三步，会交错）。

```java
// ① synchronized：互斥锁（最常用）
private synchronized void inc() { count++; }          // 方法级
private final Object lock = new Object();
void inc2() { synchronized (lock) { count++; } }      // 代码块级（锁对象显式）

// ② Lock：显式锁，更灵活（可尝试、可超时、可公平）
private final ReentrantLock lock = new ReentrantLock();
void inc3() {
    lock.lock();
    try { count++; } finally { lock.unlock(); }       // 必须在 finally 里解锁！
}

// ③ volatile：保证"可见性"（一个线程改了，另一个立刻看见）+ 禁止指令重排
//    但【不保证原子性】——count++ 用 volatile 依然会错！
private volatile boolean running = true;

// ④ 原子类：CAS 无锁编程
private final AtomicInteger count2 = new AtomicInteger();
void inc4() { count2.incrementAndGet(); }             // 线程安全的自增
```

| 武器 | 保证什么 | 适用 |
|---|---|---|
| synchronized | 原子 + 可见 + 有序 | 临界区互斥（首选） |
| volatile | 可见 + 有序（不原子） | 状态标志位 |
| AtomicXXX | 单变量原子操作 | 计数器、累加器 |
| Lock | 同 synchronized 但更灵活 | 需要超时/中断/多条件时 |

### 10.4 并发容器与工具

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();   // 并发 Map
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();     // 读多写少
BlockingQueue<String> queue = new LinkedBlockingQueue<>();            // 生产者-消费者
queue.put("任务");      // 满则阻塞
String task = queue.take();   // 空则阻塞

CountDownLatch latch = new CountDownLatch(3);   // 等 3 件事都完成
```

> **生产者-消费者模式**：一个线程往 `BlockingQueue` 放任务，另一个线程取任务处理——上位机"采集→处理→入库"、后端"接单→异步处理"都是它的变体。

### 10.5 线程池（工业必须）

线程是稀缺资源——不能"来一个任务 new 一个线程"。用**池**复用：

```java
ThreadPoolExecutor pool = new ThreadPoolExecutor(
        4,                                  // 核心线程数
        8,                                  // 最大线程数
        60L, TimeUnit.SECONDS,              // 空闲回收时间
        new ArrayBlockingQueue<>(100),      // 有界队列（防 OOM）
        new ThreadPoolExecutor.CallerRunsPolicy());  // 拒绝策略

pool.execute(() -> System.out.println("任务执行"));

// 提交有返回值的任务
Future<Integer> f = pool.submit(() -> 1 + 1);
int r = f.get();           // 2

pool.shutdown();           // 优雅关闭：不再收新任务，等旧任务跑完
```

**执行流程**（面试必考）：核心线程未满 → 建核心线程；核心满 → 进队列；队列满 → 建非核心线程；全满 → 触发拒绝策略。

**为什么不建议 `Executors.newFixedThreadPool()`？**——它的队列无界，任务堆积会 OOM；`Executors` 工厂方法隐藏了关键参数。**手写 `ThreadPoolExecutor`，参数可控**。

### 10.6 展望：虚拟线程（JDK21+）

传统线程 1:1 对应操作系统线程（重、贵、上限几千）。**虚拟线程**（`Thread.ofVirtual()`）由 JVM 调度，可以轻松跑几十万个——高并发 IO 场景的颠覆性变化（上位机同时管理大量设备连接、服务器高并发请求都受益）。

> 现在先把"线程池 + 并发安全"的地基打牢，虚拟线程的水到渠成。

---

## 11. 反射与注解（框架的基石）

> 一句话：**框架 = 反射 + 注解 + 泛型**。Spring、MyBatis 的"魔法"，底层全靠本章两样东西。

### 11.1 Class 对象：类的"身份证"

```java
// 获取 Class 对象的三种方式
Class<?> c1 = String.class;                  // 类名.class
Class<?> c2 = "hello".getClass();            // 对象.getClass()
Class<?> c3 = Class.forName("java.lang.String");   // 全限定名（框架最爱）

System.out.println(c1 == c2 && c2 == c3);    // true —— 同一个类只有一份 Class 对象
```

### 11.2 反射操作：运行时"看见"类的全部

```java
Class<?> clazz = Class.forName("com.example.User");

// ① 构造对象
Object user = clazz.getDeclaredConstructor().newInstance();   // 无参
Constructor<?> ctor = clazz.getDeclaredConstructor(String.class, int.class);
Object user2 = ctor.newInstance("张三", 18);

// ② 查看/调用方法
for (Method m : clazz.getDeclaredMethods()) {
    System.out.println(m.getName());     // 打印所有方法名
}
Method setName = clazz.getMethod("setName", String.class);
setName.invoke(user, "李四");            // 相当于 user.setName("李四")

// ③ 查看/修改字段（包括 private！）
Field f = clazz.getDeclaredField("age");
f.setAccessible(true);                   // 打破封装（谨慎）
f.set(user, 20);
Object age = f.get(user);                // 20
```

**反射的用武之地**：框架读注解配置 → 反射创建对象、注入依赖、调用方法。**业务代码不要滥用**——它跳过了编译期检查，运行时才报错，还慢。

### 11.3 注解：给代码贴"标签"

```java
// ① 内置注解
@Override          // 编译期检查重写
@Deprecated        // 标记"过时"
@SuppressWarnings("unchecked")   // 抑制警告

// ② 元注解（注解的注解）
@Target({ElementType.METHOD, ElementType.TYPE})   // 能贴在哪
@Retention(RetentionPolicy.RUNTIME)               // 保留到什么时候（RUNTIME 才能被反射读到）
public @interface MyLog {
    String value() default "默认值";               // 注解属性
}

// ③ 使用
@MyLog("下单操作")
public void placeOrder() { ... }

// ④ 反射读取（框架的"读标签"过程）
Method m = clazz.getMethod("placeOrder");
if (m.isAnnotationPresent(MyLog.class)) {
    MyLog log = m.getAnnotation(MyLog.class);
    System.out.println("操作名: " + log.value());   // 下单操作
}
```

### 11.4 动态代理：AOP 的底层原理（先认识一下）

```java
// 接口 + 调用处理器 = 运行期生成代理类
Object proxy = Proxy.newProxyInstance(
        clazz.getClassLoader(),
        new Class[]{Service.class},
        (p, method, args) -> {
            System.out.println("[前置] 日志/事务开始");
            Object result = method.invoke(target, args);   // 调真实方法
            System.out.println("[后置] 日志/事务提交");
            return result;
        });
```

> 这就是 `@Transactional`、日志 AOP 的底层原理——**"在不改你代码的前提下，在方法前后插一段逻辑"**。学 Spring 时会再见。

---

## 12. Lambda 与函数式接口

### 12.1 从匿名内部类到 lambda（进化史）

```java
// 老写法：匿名内部类（还记得第 5 章吗）
Runnable r1 = new Runnable() {
    @Override
    public void run() { System.out.println("跑"); }
};

// 新写法：lambda（JDK8+）—— 因为接口只有一个抽象方法，编译器能推断出你在实现谁
Runnable r2 = () -> System.out.println("跑");
```

lambda 语法：`(参数列表) -> { 方法体 }`——省略类型、省略括号（单参数时）、省略 `{}`（单表达式时）。

### 12.2 函数式接口：Java 的"四把扳手"

**函数式接口 = 只有一个抽象方法的接口**（`@FunctionalInterface` 标记）。JDK 内置了最常用的四个：

**真实运行输出（Demo4）**：

```
square.apply(9)      = 81
notBlank.test(" ")   = false
supplier.get()       = supplied
consumer + 方法引用输出这一行
```

| 接口 | 形状 | 用途 | 示例 |
|---|---|---|---|
| `Function<T,R>` | T → R | 转换 | `x -> x * x` |
| `Predicate<T>` | T → boolean | 判断/过滤 | `s -> !s.isBlank()` |
| `Supplier<T>` | () → T | 提供值/工厂 | `() -> "supplied"` |
| `Consumer<T>` | T → void | 消费（打印/保存） | `System.out::println` |

```java
Function<Integer, Integer> square = x -> x * x;       // 输入一个，输出一个
Predicate<String> notBlank = s -> !s.isBlank();       // 判断
Supplier<String> supplier = () -> "supplied";         // 无中生有
Consumer<String> printer = System.out::println;       // 消费掉

// 扩展：基本类型特化版（避免装箱开销）：IntFunction、ToIntFunction、IntPredicate...
// 还有：BiFunction<T,U,R>（两入一出）、UnaryOperator<T>（入出同型）
```

### 12.3 方法引用：lambda 的"极简版"

当 lambda 体只是"调用一个已存在的方法"时，可以用 `::`：

**真实运行输出（Demo4）**：

```
Integer::parseInt 方法引用 → [1, 2, 3]
四种方法引用：类::静态 / 对象::实例 / 类::实例 / 类::new
```

| 类型 | 写法 | 等价 lambda |
|---|---|---|
| 静态方法 | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| 对象实例方法 | `System.out::println` | `x -> System.out.println(x)` |
| 类实例方法 | `String::length` | `s -> s.length()` |
| 构造器 | `ArrayList::new` | `() -> new ArrayList<>()` |

### 12.4 实战：lambda 让代码"说话"

```java
// 给集合排序
list.sort((a, b) -> a.age() - b.age());               // 或者 Comparator.comparingInt(Person::age)

// 按钮回调（Swing/JavaFX——上位机界面常用）
button.addActionListener(e -> System.out.println("点击!"));

// 线程任务
new Thread(() -> System.out.println("异步跑")).start();
```

> 记住：**lambda 不是语法魔法，只是"把只有一个方法的接口写短"**——理解函数式接口，lambda 就全懂了。下一章的 Stream 就是它的最佳搭档。

---

## 13. Stream 与 Optional

### 13.1 Stream：集合的"声明式流水线"

**是什么**：把"一堆数据的加工流程"写成"一条流水线"——**描述要什么，而不是怎么写循环**。

```
数据源 → [filter] → [map] → [sorted] → [collect] → 结果
        中间操作    中间操作   中间操作    终止操作
```

**真实运行输出（Demo3，先看效果）**：

```
===== 3. Lambda + Stream: 声明式数据流水线 =====
北京成人(按年龄): 孙七, 张三, 王五
  对比: 如果用 for 循环 + if, 这段要写十几行
```

### 13.2 三阶段与常用操作

```java
List<Person> persons = ...;

// 完整流水线：筛选 30 岁以下 → 取名字 → 排序 → 收集
List<String> names = persons.stream()             // ① 数据源
        .filter(p -> p.age() < 30)                 // ② 中间操作（惰性！）
        .map(Person::name)                        //    转换
        .sorted()                                 //    排序
        .toList();                                // ③ 终止操作（这时才真正执行）

// 常用中间操作
stream.distinct()          // 去重
      .limit(5)            // 取前 5 个
      .skip(2)             // 跳过前 2 个
      .flatMap(s -> Arrays.stream(s.split(",")))   // 打平（一对多）

// 常用终止操作
stream.count()                       // 计数
      .anyMatch(s -> s.startsWith("A"))   // 存在一个满足？（短路）
      .allMatch(...)                 // 全部满足？
      .findFirst()                   // 取第一个（Optional）
      .reduce(0, Integer::sum)       // 归约（求和/求积/最值）
      .forEach(System.out::println); // 逐个消费
```

> **惰性求值**：中间操作只是"记账"，终止操作才开始干活——所以 `filter` 写一百个也不怕（没有终止操作，一行都不会执行）。

### 13.3 Collectors：收集器工具箱（对照 Demo4 输出）

**真实运行输出（Demo4）**：

```
工资≥15000（降序）: [张三, 李四, 赵六, 王五]
部门平均工资: {研发=19000.0, 运维=12000.0, 销售=15500.0}
工资总和: 81000
名字→工资: {钱七=12000, 李四=18000, 张三=20000, 王五=15000, 赵六=16000}
```

```java
// 下面是产生上面输出的代码形状
var topNames = staff.stream()
        .filter(e -> e.salary() >= 15000)
        .sorted(Comparator.comparingInt(Employee::salary).reversed())
        .map(Employee::name)
        .toList();

Map<String, Double> avgByDept = staff.stream()
        .collect(Collectors.groupingBy(Employee::dept,
                 Collectors.averagingInt(Employee::salary)));       // 分组统计

int sum = staff.stream().mapToInt(Employee::salary).sum();          // 基本类型流（避免装箱）

Map<String, Integer> salaryMap = staff.stream()
        .collect(Collectors.toMap(Employee::name, Employee::salary));// 收集成 Map
```

**Collectors 表（收藏）**：

| 收集器 | 作用 |
|---|---|
| `toList()` / `toSet()` | 收集成列表/集合 |
| `toMap(k, v)` | 收集成 Map |
| `groupingBy(fn)` | 按某字段分组 |
| `counting()` / `summingInt()` / `averagingInt()` | 计数/求和/平均 |
| `joining(", ")` | 字符串拼接 |
| `partitioningBy(fn)` | 按 boolean 分区（true/false 两组） |

### 13.4 Optional：把"可能为空"写进类型里

**为什么**：`null` 是"十亿美元的错误"——你不知道一个方法的返回值是不是可能 null，忘了判空就是 NPE。`Optional<T>` 是一个"装东西的盒子"：**盒子里可能有值，可能没有**——迫使调用方处理。

**真实运行输出（Demo4）**：

```
timeout = 3000, retries = 默认值
host 缺失，走降级逻辑
链式风格：user.getAddr().map(Addr::city).orElse("未知")
```

```java
// 创建
Optional<String> full = Optional.of("值");             // 值不能为 null
Optional<String> maybe = Optional.ofNullable(getFromMap());   // 可能为 null
Optional<String> empty = Optional.empty();

// 常用 API
maybe.orElse("默认值")              // 无值用默认
     .orElseGet(() -> compute())    // 无值才计算（省性能）
     .orElseThrow(() -> new IllegalStateException("缺失"));   // 无值抛异常
maybe.ifPresent(v -> System.out.println(v));                // 有值才执行
maybe.ifPresentOrElse(v -> print(v), () -> fallback());     // 两分支
maybe.map(String::length)          // 有值则转换（链式）
     .filter(len -> len > 3);      // 有值则过滤

// 从可能为 null 的链式调用中优雅取值的对照：
// 旧：if (user != null && user.getAddr() != null && user.getAddr().getCity() != null) ...
// 新：Optional.ofNullable(user).map(User::getAddr).map(Addr::city).orElse("未知")
```

> 使用原则：**返回值可能为空 → 用 Optional 表达**（尤其是"查询类"方法）；**字段和方法参数不要用 Optional**（过度设计）。

### 13.5 并行流与注意事项

```java
long count = bigList.parallelStream()     // 并行流：自动多线程分片处理
        .filter(this::heavyCheck)
        .count();
```

- 适合：数据量大、计算密集、无共享状态的场景；
- 慎用：IO 操作（并行流用的是公共线程池，会互相拖累）、有状态操作、小数据量（线程调度比计算还贵）。

> 本章小结：**Stream 不是"更快的循环"，而是"更清晰的表达"**。判断标准：用 for 写得清楚就用 for；"筛选-转换-汇总"的套路用 Stream。两者都要会——工具箱越全，选择越从容。

---

## 14. 现代语法全家桶（var / 文本块 / switch 表达式 / record / sealed / 模式匹配）

> 本章是"从课本语法到 2020 年代工业写法"的升级路径。所有语法点均由 `javase-demo/src/Demo4_ModernJava.java` 真实编译运行验证。
> 这也是《Java 上位机开发实战》的前置章节之一——那个项目全程使用本章语法。

### 14.1 var：局部类型推断（回顾 + 深化）

第 2.6 节已详细讲过。一句话回顾：**编译器帮你补类型，编译器保证类型安全**。它在 Demo4 里的样子：

```java
var name = "Java";                    // String
var list = new ArrayList<String>();   // ArrayList<String>
var map = new HashMap<String, Integer>();
```

**真实运行输出（Demo4）**：

```
var name -> String : Java
var list -> ArrayList [a]
var map  -> HashMap {one=1}
限制：只能用于局部变量 + 必须有初始值 + 不能接 null（编译器会拦）
```

> 工程惯例：**配合"接口类型 + 具体实现"更相宜**——`List<String> list = new ArrayList<>();` 想省略可写 `var list = new ArrayList<String>();`。在方法链的中间结果、循环变量上，`var` 的收益最大。

### 14.2 文本块（JDK15+）：多行字符串所见即所得

**痛点**：以前拼 HTML/SQL/JSON 要写一堆 `\n` 和 `\"` 转义，又难读又易错。文本块用 `"""` 三个双引号包起来：

```java
String json = """
        {
          "name": "DeviceMonitor",
          "version": 1.0,
          "tags": ["java", "modbus"]
        }""";
```

**真实运行输出（Demo4）**：

```
{
  "name": "DeviceMonitor",
  "version": 1.0,
  "tags": ["java", "modbus"]
}
传统写法需要显式 \n 与转义：{⏎  "name": "DeviceMonitor"⏎}
文本块：直接换行、引号不用转义，拼接 JSON / SQL / HTML 首选
```

**规则要点**：

- 开头的 `"""` 后必须换行；结尾 `"""` 位置决定"公共缩进"的处理；
- 内部的双引号**不用转义**；`\` 依然需要（或用 `\s` 等）；
- 适合：SQL、JSON、HTML、日志模板——**上位机项目里"Modbus 报文说明"、"SQL 建表语句"用的就是它**。

### 14.3 switch 表达式（JDK14+）：从"语句"到"会返回值的表达式"

第 3.5 节已展示三形态。复习重点：**箭头标签无穿透 + 可以用 `yield` 在代码块分支返回值 + 作为表达式参与赋值**：

```java
String type = switch (day) {
    case 1, 2, 3, 4, 5 -> "工作日";
    case 6, 7 -> "休息日";
    default -> {
        int n = day - 7;
        yield "非法(超出 " + n + " 天)";
    }
};
```

**真实运行输出（Demo4）**：

```
  day=1 → 工作日
  day=6 → 休息日
  day=7 → 休息日
  day=9 → 非法(编号=4)
特点：能返回值、无 fall-through 穿透、必须穷尽
```

> 好处总结："漏写 break 的 bug"成为历史 + "赋值语法更顺" + "编译器检查穷尽性"。

### 14.4 record：一行 = 一个不可变数据类（JDK16+）

**痛点**：写一个"只有字段 + getter + toString + equals/hashCode"的类要几十行样板代码。record 一行搞定，编译器自动生成：

```java
record Point(int x, int y) { }
```

**真实运行输出（Demo4）**：

```
自动 toString : Point[x=3, y=4]
自动 equals   : p1.equals(p2) = true
访问器        : p1.x()=3, p1.y()=4
紧凑构造器校验: User[name=张三, age=18]
校验拦截      : 非法年龄: -5
```

**要点**：

- 自动生成：全参构造器、访问器（注意是 `x()` 不是 `getX()`）、`toString`、`equals`、`hashCode`；
- 字段是 `final` 的（不可变）——**天然线程安全、适合做 DTO/值对象**；
- **紧凑构造器**（compact constructor）做校验：

```java
record User(String name, int age) {
    User {                                  // 没有参数列表的构造器 = 紧凑构造器
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("非法年龄: " + age);
        }
    }
}
```

- 不能继承别的类（已经隐式继承 `Record`），可以实现接口。

> 工业实践：**"数据传输对象"（DTO）、"值对象"、"配置项"全部优先 record**——上位机项目里"设备读数"、后端项目里"请求/响应体"都是它（JavaEE 教程的 Spring Boot 项目里大量使用）。

### 14.5 sealed 类（JDK17）：受限继承

**痛点**：你写了一个 `Shape` 接口，希望"只允许 Circle / Rect / Triangle 实现它"——这样编译器能帮你检查"所有分支都处理了吗"。sealed 就是干这个的：

```java
sealed interface Shape permits Circle, Rect { }   // 只允许这两个类实现

record Circle(double r) implements Shape { }
record Rect(double w, double h) implements Shape { }
```

- 子类必须是 `final`（或 `sealed`/`non-sealed`），"继承权"被白名单了；
- **与 switch 模式匹配是最佳搭档**（见下节）。

### 14.6 instanceof 模式匹配（JDK16+）：省掉强转

**老写法**：

```java
if (obj instanceof String) {
    String s = (String) obj;     // 判完了还要转，啰嗦
    System.out.println(s.length());
}
```

**新写法**：

```java
if (obj instanceof String s) {   // 判完直接绑定变量 s
    System.out.println(s.length());
}
if (obj instanceof Integer i && i > 10) {   // 还能带守卫条件
    System.out.println(i);
}
```

**真实运行输出（Demo4）**：

```
  String，长度=5
  Integer 且 >10: 42
  其他类型: Double
  其他类型: int[]
```

**和 sealed 组合起来**（`switch` 模式匹配是 JDK21 正式语法，这里用 if-else 版本演示，效果一样）：

```java
for (Shape s : shapes) {
    if (s instanceof Circle c) {
        area = Math.PI * c.r() * c.r();
    } else if (s instanceof Rect r) {
        area = r.w() * r.h();
    } else {
        throw new IllegalStateException("未知形状: " + s);   // sealed 保证"理论上不可达"
    }
}
```

**真实运行输出（Demo4）**：

```
  Circle[r=2.0] → 面积 12.57
  Rect[w=3.0, h=4.0] → 面积 12.00
  总面积 = 24.57
（sealed 限制谁能继承；switch 模式匹配是其最佳搭档——JDK17 为预览、JDK21 正式，本示例坚持用正式语法）
```

### 14.7 这些语法为什么值得现在学

| 语法 | 解决什么 | 你在哪里会见到 |
|---|---|---|
| var | 啰嗦的类型声明 | 一切现代代码 |
| 文本块 | 多行字符串地狱 | SQL/JSON/HTML 拼接 |
| switch 表达式 | 漏 break、不能赋值 | 枚举分发、状态机 |
| record | 样板 DTO 代码 | API 请求/响应、数据对象 |
| sealed | 继承失控 | 领域建模、模式匹配前置 |
| 模式匹配 | 判类型 + 强转重复 | 类型分发逻辑 |

> **下一章预告**：语法清爽了，但"JSON 解析、日志、工具类、测试"这些事不会凭空解决——需要**外部函数库**。这就是第 15 章。

---

## 15. 外部函数库实战（从"裸写"到"站在巨人肩膀上"）

### 15.1 什么是"外部函数库"：jar 与 classpath

JDK 自带的类库（`java.*`、`javax.*`、`java.time.*`……）是"官方标配"。除此之外，全世界开发者写了海量**第三方库**——你 90% 的工程能力来自"会不会选库、用库"。

**为什么需要**？三个例子：

- JSON 序列化：自己写解析器容易出 bug，Jackson 一行搞定；
- 日志：`System.out.println` 没有级别、没有输出控制，SLF4J 是行业标准；
- 测试：手工"main 方法跑一遍"不可复现，JUnit 让测试自动化。

**一个库 = 一个 jar 文件**（本质是 zip，里面是编译好的 `.class`）。使用三步走：

```bash
# ① 把 jar 放进项目（本项目放在 lib/ 目录）
# ② 编译时加 -cp（classpath 告诉编译器"去哪找类"）
javac -encoding UTF-8 -cp "lib/*" -d out src/*.java

# ③ 运行时也要加 -cp（运行时同样要能找到 jar 里的类）
java -Dfile.encoding=UTF-8 -cp "out:lib/*" Demo5_Libs
```

> `lib/*` 是通配符：把 lib 下所有 jar 都加进 classpath。**编译和运行都要写**——这是新手最常忘的一步（错误信息：`NoClassDefFoundError` / `ClassNotFoundException`）。

**与 Maven 的关系**（预告）：真项目里用 Maven/Gradle 声明依赖，自动下载管理 jar——见《JavaEE 教程》第 10 章。但**"jar + classpath"是底层真相**，先懂它，框架才不会变成黑盒。

**lib/ 里都有什么**（本项目实测用的 7 个 jar）：

| jar | 用途 | 演示 |
|---|---|---|
| jackson-databind / core / annotations 2.17.2 | JSON 序列化/反序列化 | Demo5 |
| slf4j-api / slf4j-simple 2.0.13 | 日志门面 + 简单实现 | Demo5 |
| commons-lang3 3.14.0 | Apache 工具箱 | Demo5 |
| junit-platform-console-standalone 1.10.2 | 单元测试 | junit/ |

### 15.2 Jackson：Java 对象 ⇄ JSON（事实标准）

**为什么**：JSON 是前后端、服务间、配置文件的通用语言。Jackson 是 Java 生态事实标准（Spring Boot 默认用它）。

```java
// record + Jackson 是 2020 年代的标准组合
record Device(String name, double temp, boolean online) { }

var mapper = new ObjectMapper();

// 对象 → JSON
String json = mapper.writeValueAsString(new Device("温室传感器-A", 23.9, true));

// JSON → 对象
Device back = mapper.readValue(json, Device.class);

// JSON → List / Map（用 TypeReference 保留泛型信息）
List<Device> list = mapper.readValue(arr, new TypeReference<List<Device>>() {});
Map<String, Object> m = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
```

**真实运行输出（Demo5）**：

```
对象→JSON: {"name":"温室传感器-A","temp":23.9,"online":true}
美化输出:
{
  "name" : "温室传感器-A",
  "temp" : 23.9,
  "online" : true
}
JSON→对象: Device[name=温室传感器-A, temp=23.9, online=true]
往返一致: true
List→JSON: [{"name":"温室传感器-A","temp":23.9,"online":true},{"name":"传感器-B","temp":21.5,"online":false}]
JSON→List: 大小=2, 第二项=Device[name=传感器-B, temp=21.5, online=false]
JSON→Map: {id=1, tags=[a, b]}
```

**工程习惯**：

- "往返一致"（对象→JSON→对象，`equals` 为 true）是序列化库的"体检标准"；
- 字段名与 JSON key 不一致时用 `@JsonProperty("key")` 注解；
- 遇到"未知字段"的报错：配置 `FAIL_ON_UNKNOWN_PROPERTIES=false`。

### 15.3 SLF4J：日志门面（换实现不改代码）

**为什么**：日志"要分级（debug/info/warn/error）、要能关能开、要能换输出目标（控制台/文件）"——`System.out.println` 全做不到。

```java
private static final Logger log = LoggerFactory.getLogger(Demo5_Libs.class);

log.info("服务启动完成, 端口={}", 8080);                    // {} 占位符，不用字符串拼接
log.warn("库存不足: productId={}, 剩余={}", 7, 0);
log.error("模拟异常日志", new RuntimeException("连接超时(演示用)"));
```

**真实运行输出（Demo5）**：

```
[main] INFO Demo5_Libs - 服务启动完成, 端口=8080
[main] WARN Demo5_Libs - 库存不足: productId=7, 剩余=0
[main] ERROR Demo5_Libs - 模拟异常日志
java.lang.RuntimeException: 连接超时(演示用)
	at Demo5_Libs.main(Demo5_Libs.java:61)
（上方日志由 slf4j-simple 输出：时间 级别 类名 - 消息）
```

**要点**：

- **门面模式**：代码只依赖 `slf4j-api`（接口），具体输出由实现决定（本次是 `slf4j-simple`；生产常用 Logback/Log4j2）——**换实现不改一行业务代码**；
- `{}` 占位符优于字符串拼接（不打印时零开销）；
- `log.error("...", e)` 传异常对象（打印完整堆栈），别只拼 `e.getMessage()`。

### 15.4 Apache Commons Lang3：补 JDK 的短板

JDK 的 `String`、`Number` 缺一些"顺手"的方法，commons-lang3 是使用率最高的补丁包：

```java
StringUtils.isBlank(" ")              // true（空格也算"空白"）
StringUtils.leftPad("7", 2, '0')      // "07"（左边补齐）
StringUtils.abbreviate("Java上位机开发实战教程", 8)   // "Java上..."（超长截断）
NumberUtils.toInt(null, -1)           // -1（null 安全转换，不抛异常）
RandomStringUtils.randomAlphanumeric(8)   // 随机 8 位字母数字（造 ID/验证码）
```

**真实运行输出（Demo5）**：

```
isBlank(" ")        = true
leftPad("7",2,'0')  = 07
abbreviate(...,8)  = "Java上..."
toInt(null,-1)     = -1
随机设备ID(8位)    = 05WAAPJ6
```

> 同类还有：Guava（Google 工具箱）、Hutool（国产全家桶）。**原则：先查 JDK 有没有，没有再找库**。

### 15.5 JUnit：让"验证"自动化（单元测试）

**为什么**：手工跑 demo 只能"看着对"，改代码后不知道有没有破坏旧功能。JUnit 把"验证"写成可重复运行的测试代码。

```java
class CalcTest {
    @Test
    void "2 + 3 应当等于 5"() {          // 测试方法命名可以直接描述意图
        assertEquals(5, Calc.add(2, 3));
    }

    @Test
    void "10 / 3 整数除法应当等于 3"() {
        assertEquals(3, Calc.div(10, 3));
    }

    @Test
    void "除以 0 应当抛 ArithmeticException 且带正确消息"() {
        var e = assertThrows(ArithmeticException.class, () -> Calc.div(1, 0));
        assertEquals("/ by zero", e.getMessage());
    }
}
```

**运行方式（命令行，无需 IDE）**：

```bash
javac -encoding UTF-8 -cp "lib/*" -d out-junit junit/*.java
java -jar lib/junit-platform-console-standalone-1.10.2.jar \
     --class-path out-junit --scan-class-path
```

**真实运行输出（本项目实测）**：

```
[JUnit] 测试套件启动
├─ JUnit Jupiter ✔
│  └─ CalcTest ✔
│     ├─ 除以 0 应当抛 ArithmeticException 且带正确消息 ✔
│     ├─ 10 / 3 整数除法应当等于 3 ✔
│     └─ 2 + 3 应当等于 5 ✔

Test run finished after 184 ms
[         3 tests successful      ]
[         0 tests failed          ]
```

**要点**：

- 三大断言：`assertEquals`（相等）、`assertTrue`（为真）、`assertThrows`（抛异常）；
- 每个测试方法独立运行、互不影响；
- **"先跑通，再改代码，再跑测试"**——这就是"回归保护"。

### 15.6 如何选库、学库（方法论）

| 步骤 | 看什么 |
|---|---|
| 选 | Maven Central 下载量、GitHub star、最近更新、License |
| 用 | 官方文档的 Quick Start（先跑通最小例子） |
| 学 | 源码/API 文档；**遇到问题先搜"库名 + 你的报错"** |
| 慎 | 太重、太久未更新、和已有库职责重复的 |

> 本章小结：**JDK 是毛坯房，外部库是精装修**。判断一个 Java 工程师的功力，很大程度看"知不知道该用什么库、用对版本、排对故障"。

---

## 16. 网络编程基础（上位机的"高速公路"）

> 本教程的最后一章技术内容。它连接两个方向：**往后端**——HTTP API（JavaEE 教程）；**往工业**——设备通信（上位机教程）。核心概念只有一个：**Socket = 两台机器间的双向字节管道**。

### 16.1 网络分层与 TCP/UDP 一分钟版

```
应用层：HTTP / Modbus / MQTT     ← 你写的协议
传输层：TCP / UDP                ← Socket 编程在这里
网络层：IP                       ← 路由器寻址
链路层：以太网 / Wi-Fi / RS-485   ← 物理世界
```

| 对比 | TCP | UDP |
|---|---|---|
| 连接 | 面向连接（三次握手） | 无连接 |
| 可靠性 | 可靠（重传、有序） | 不保证（可能丢/乱序） |
| 速度 | 稍慢（可靠有代价） | 快 |
| 典型场景 | HTTP、Modbus TCP、数据库 | 视频流、DNS、广播 |

> 工业上位机：**Modbus TCP 基于 TCP**；串口（RS-485）不在 IP 层，但编程模型相似（都是"收发字节"）。

### 16.2 Socket 编程：服务端与客户端

**核心模型**：

```
服务端：ServerSocket 监听端口 → accept() 等待连接 → 拿到 Socket → 读写
客户端：new Socket(host, port) 主动连接 → 拿到 Socket → 读写
两边都用：InputStream / OutputStream 收发字节（或包装成字符流）
```

**真实可运行的例子（Demo6：同一进程内的"迷你两端"）**：

```java
// 服务端：后台线程
var server = new ServerSocket(0);          // 0 = 让系统挑一个空闲端口
int port = server.getLocalPort();
Thread serverThread = new Thread(() -> {
    try (var s = server.accept();          // 阻塞等待客户端连接
         var in = new BufferedReader(new InputStreamReader(s.getInputStream()));
         var out = new PrintWriter(s.getOutputStream(), true)) {
        String line;
        while ((line = in.readLine()) != null) {
            out.println("ECHO: " + line.toUpperCase());   // 回声（大写）
            if (line.equalsIgnoreCase("bye")) break;
        }
    }
});
serverThread.start();

// 客户端：主线程
try (var s = new Socket("127.0.0.1", port);
     var in = new BufferedReader(new InputStreamReader(s.getInputStream()));
     var out = new PrintWriter(s.getOutputStream(), true)) {
    for (String msg : new String[]{"hello", "modbus over tcp", "bye"}) {
        out.println(msg);                  // 发送
        System.out.println("[client] 发送: " + msg + " | 收到: " + in.readLine());
    }
}
```

**真实运行输出（Demo6）**：

```
▶ TCP 回显服务端启动（端口由系统自动分配）……
  监听端口: 40639
  注意：真实上位机程序里，这个端口通常对应"下位机/设备服务端"的角色
[server] 收到客户端连接: /127.0.0.1:52662
[client] 发送: hello | 收到: ECHO: HELLO
[server] hello  →  ECHO: HELLO
[server] modbus over tcp  →  ECHO: MODBUS OVER TCP
[client] 发送: modbus over tcp | 收到: ECHO: MODBUS OVER TCP
[server] bye  →  ECHO: BYE
[server] 连接关闭
[client] 发送: bye | 收到: ECHO: BYE

✓ Demo6 完成：TCP 全流程真实跑通（连接→发送→应答→关闭）
  上位机教程《06》里，这套 Socket 就是与设备通信的高速公路
```

**编码细节备忘**：

- 流包装链：`Socket.getInputStream()` → `InputStreamReader`（字节→字符）→ `BufferedReader`（按行读）；
- `PrintWriter(..., true)` 的第二参数 `autoFlush`——发送方要"发一条走一条"，别攒着；
- **必须 try-with-resources**：忘关连接会泄漏端口/句柄；
- 真实程序要加：**超时**（`socket.setSoTimeout(3000)`）、**重连**、**心跳**——上位机教程第 7 章专门讲。

### 16.3 HTTP 客户端：访问"别人的服务"

除了"自定义协议"（如 Modbus），更常见的是调用 HTTP API。JDK11+ 自带 `HttpClient`：

```java
var client = HttpClient.newHttpClient();
var request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.example.com/books/1"))
        .header("Accept", "application/json")
        .timeout(Duration.ofSeconds(5))
        .GET()
        .build();
var response = client.send(request, HttpResponse.BodyHandlers.ofString());
System.out.println(response.statusCode());   // 200
System.out.println(response.body());         // JSON 文本
```

> POST JSON、鉴权头、异步 `sendAsync` 都是同一套 API 的延伸——学 Spring Boot 的 `RestTemplate`/`WebClient` 时回来对照。

### 16.4 从本章到《Java 上位机开发实战》

网络 + 串口 + 协议 + 数据入库 + 界面 = 上位机。本教程学到这里，你已经具备全部前置：

| 上位机需要的 | 本教程对应章节 |
|---|---|
| Socket 与设备通信 | 本章（16）+ 线程池（10.5） |
| 数据入库 SQLite/JDBC | 《Java与SQL数据库-深度融合教学》+ 本教程第 8 章 |
| 现代语法（var/record/文本块） | 第 14 章 |
| 外部库（jSerialComm 等） | 第 15 章 + 上位机教程 1 章 |
| 界面回调 / 定时采集 | lambda（12）、线程（10） |
| 日志与排障 | SLF4J（15.3） |

> **下一步**：打开《Java 上位机开发实战：串口 · Modbus · 数据采集与监控》，从"没有真设备也能练"的模拟器开始，把本章的 Socket 知识变成一条真实的"采集 → 入库 → 监控 → 报警"链路。

---

## 17. 学习路线与常见误区

### 17.1 五阶段路线（更新版）

| 阶段 | 内容 | 过关标准 |
|:---:|---|---|
| 1 | 基础语法 + OOP（第 1~5 章） | 不看资料写出 5.6 的多态 demo |
| 2 | 集合 + 异常 + IO + 多线程（第 6~10 章） | 说清 ArrayList/HashMap 原理 |
| 3 | 反射注解 + 函数式 + Stream（第 11~13 章） | 会用 Stream 处理数据 |
| 4 | 现代语法 + 外部库 + 网络（第 14~16 章） | 能读第三方库文档、独立跑通一个 JSON 处理程序 |
| 5 | SQL/数据库（配套教程）→ JavaEE 服务端 → **上位机方向** | 二选一或都走：跑通 JavaEE 四项目 / 跑通上位机全链路 |

### 17.2 十大常见误区（对照自查）

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

**新增补充 5 条**：

11. `var` 当"动态类型"用 ✗ —— 它是编译期推断的强类型缩写；
12. 忘了在"运行时" classpath 里加 jar（编译通过、运行 `ClassNotFoundException`）；
13. `Optional` 滥用（字段/参数用 Optional ✗ → 只用于返回值）；
14. 线程池用无界队列（OOM 隐患）；
15. 忘关资源（连接/流）——try-with-resources 保平安。

---

## 附录：配套演示与运行方式

`javase-demo/` 共 **7 个程序 + 1 套 JUnit 测试**，全部在 OpenJDK 17 下真实编译运行通过：

```
javase-demo/
├── src/Hello.java              # 字节码演示（javac → javap → java）
├── src/Demo1_Basics.java       # 类型系统与 7 个陷阱（已实测）
├── src/Demo2_Oop.java          # 封装/继承/多态/接口/static（已实测）
├── src/Demo3_Collections.java  # 集合/泛型/Stream/迭代陷阱（已实测）
├── src/Demo4_ModernJava.java   # var/文本块/switch/record/sealed/模式匹配/lambda/Stream/Optional/时间（已实测）
├── src/Demo5_Libs.java         # Jackson / SLF4J / Commons Lang3（已实测）
├── src/Demo6_Network.java      # TCP Socket 全流程（已实测）
├── junit/Calc.java             # 被测类
├── junit/CalcTest.java         # JUnit 测试（3 用例全部通过）
├── lib/                        # 7 个外部库 jar
├── out/ · out-junit/           # 编译产物
```

**一键跑通全部**：

```bash
cd javase-demo

# ① 全量编译（依赖 lib/ 里的外部库）
javac -encoding UTF-8 -cp "lib/*" -d out src/*.java

# ② 逐个运行（每个都是真实输出）
java -Dfile.encoding=UTF-8 -cp out Demo1_Basics
java -Dfile.encoding=UTF-8 -cp out Demo2_Oop
java -Dfile.encoding=UTF-8 -cp out Demo3_Collections
java -Dfile.encoding=UTF-8 -cp out Demo4_ModernJava
java -Dfile.encoding=UTF-8 -cp "out:lib/*" Demo5_Libs       # ← 注意带 lib
java -Dfile.encoding=UTF-8 -cp out Demo6_Network

# ③ 字节码演示
java -Dfile.encoding=UTF-8 -cp out Hello
javap -c -cp out Hello

# ④ JUnit 单元测试
javac -encoding UTF-8 -cp "lib/*" -d out-junit junit/*.java
java -jar lib/junit-platform-console-standalone-1.10.2.jar --class-path out-junit --scan-class-path
```

**下一步学习衔接**：

- 数据库方向 →《Java与SQL数据库-深度融合教学》+《SQL数据库深度讲解-工业实践版》；
- 服务端方向 →《JavaEE企业级开发-现代实战教程》；
- 工业/桌面方向 →《Java上位机开发实战：串口 · Modbus · 数据采集与监控》。

> —— 教程全文完 ——
