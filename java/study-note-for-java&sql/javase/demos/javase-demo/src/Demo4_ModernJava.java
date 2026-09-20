import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Demo4：现代 Java 语法全家桶（JDK 8 ~ 17）—— 全部真实编译运行。
 *
 * 覆盖：var / 文本块 / switch 表达式 / record / sealed + 模式匹配 /
 *      instanceof 模式 / lambda / 方法引用 / Stream / Optional / 日期时间
 *
 * 运行：java -Dfile.encoding=UTF-8 -cp out Demo4_ModernJava
 */
public class Demo4_ModernJava {

    public static void main(String[] args) {
        section("[1] var 局部类型推断（JDK10+）");
        var name = "Java";
        var list = new ArrayList<String>();
        var map = new HashMap<String, Integer>();
        list.add("a");
        map.put("one", 1);
        System.out.println("var name -> " + name.getClass().getSimpleName() + " : " + name);
        System.out.println("var list -> " + list.getClass().getSimpleName() + " " + list);
        System.out.println("var map  -> " + map.getClass().getSimpleName() + " " + map);
        System.out.println("限制：只能用于局部变量 + 必须有初始值 + 不能接 null（编译器会拦）");

        section("[2] 文本块（JDK15+）：所见即所得的多行字符串");
        String json = """
                {
                  "name": "DeviceMonitor",
                  "version": 1.0,
                  "tags": ["java", "modbus"]
                }""";
        System.out.println(json);
        String legacy = "{\n  \"name\": \"DeviceMonitor\"\n}";
        System.out.println("传统写法需要显式 \\n 与转义：" + legacy.replace("\n", "⏎"));
        System.out.println("文本块：直接换行、引号不用转义，拼接 JSON / SQL / HTML 首选");

        section("[3] 增强 switch：表达式 + 箭头标签 + yield（JDK14+）");
        for (int day : new int[]{1, 6, 7, 9}) {
            String type = switch (day) {
                case 1, 2, 3, 4, 5 -> "工作日";
                case 6, 7 -> "休息日";
                default -> {
                    int n = day - 5;              // 多语句分支：用代码块 + yield 返回值
                    yield "非法(编号=" + n + ")";
                }
            };
            System.out.printf("  day=%d → %s%n", day, type);
        }
        System.out.println("特点：能返回值、无 fall-through 穿透、必须穷尽");

        section("[4] record：一行 = 一个不可变数据类（JDK16+）");
        var p1 = new Point(3, 4);
        var p2 = new Point(3, 4);
        System.out.println("自动 toString : " + p1);
        System.out.println("自动 equals   : p1.equals(p2) = " + p1.equals(p2));
        System.out.println("访问器        : p1.x()=" + p1.x() + ", p1.y()=" + p1.y());
        System.out.println("紧凑构造器校验: " + new User("张三", 18));
        try {
            new User("李四", -5);
        } catch (IllegalArgumentException e) {
            System.out.println("校验拦截      : " + e.getMessage());
        }

        section("[5] sealed 类 + 模式匹配（JDK17）");
        List<Shape> shapes = List.of(new Circle(2), new Rect(3, 4));
        double total = 0;
        for (Shape s : shapes) {
            double area;
            if (s instanceof Circle c) {
                area = Math.PI * c.r() * c.r();
            } else if (s instanceof Rect r) {
                area = r.w() * r.h();
            } else {
                throw new IllegalStateException("未知形状: " + s); // sealed 保证"理论上不可达"
            }
            total += area;
            System.out.printf("  %s → 面积 %.2f%n", s, area);
        }
        System.out.println("  总面积 = " + String.format("%.2f", total));
        System.out.println("（sealed 限制谁能继承；switch 模式匹配是其最佳搭档——JDK17 为预览、JDK21 正式，本示例坚持用正式语法）");

        section("[6] instanceof 模式匹配（JDK16+）——省掉强转");
        Object[] objs = {"hello", 42, 3.14, new int[]{1, 2}};
        for (Object o : objs) {
            if (o instanceof String str) {
                System.out.println("  String，长度=" + str.length());
            } else if (o instanceof Integer i && i > 10) {   // 可带条件（守卫）
                System.out.println("  Integer 且 >10: " + i);
            } else {
                System.out.println("  其他类型: " + o.getClass().getSimpleName());
            }
        }

        section("[7] lambda、函数式接口与方法引用（JDK8+）");
        Function<Integer, Integer> square = x -> x * x;
        Predicate<String> notBlank = s -> !s.isBlank();
        Supplier<String> supplier = () -> "supplied";
        Consumer<String> printer = System.out::println;
        System.out.println("square.apply(9)      = " + square.apply(9));
        System.out.println("notBlank.test(\" \")   = " + notBlank.test(" "));
        System.out.println("supplier.get()       = " + supplier.get());
        printer.accept("consumer + 方法引用输出这一行");
        var nums = Stream.of("1", "2", "3").map(Integer::parseInt).toList();
        System.out.println("Integer::parseInt 方法引用 → " + nums);
        System.out.println("四种方法引用：类::静态 / 对象::实例 / 类::实例 / 类::new");

        section("[8] Stream 流水线实战（JDK8+）");
        record Employee(String name, String dept, int salary) {
        }   // 局部 record：方法内也能定义
        var staff = List.of(
                new Employee("张三", "研发", 20000),
                new Employee("李四", "研发", 18000),
                new Employee("王五", "销售", 15000),
                new Employee("赵六", "销售", 16000),
                new Employee("钱七", "运维", 12000));
        var topNames = staff.stream()
                .filter(e -> e.salary() >= 15000)
                .sorted(Comparator.comparingInt(Employee::salary).reversed())
                .map(Employee::name)
                .toList();
        System.out.println("工资≥15000（降序）: " + topNames);
        Map<String, Double> avgByDept = staff.stream()
                .collect(Collectors.groupingBy(Employee::dept,
                        Collectors.averagingInt(Employee::salary)));
        System.out.println("部门平均工资: " + avgByDept);
        int sum = staff.stream().mapToInt(Employee::salary).sum();
        System.out.println("工资总和: " + sum);
        Map<String, Integer> salaryMap = staff.stream()
                .collect(Collectors.toMap(Employee::name, Employee::salary));
        System.out.println("名字→工资: " + salaryMap);

        section("[9] Optional：把\"可能为空\"写进类型里（JDK8+）");
        Map<String, String> config = new HashMap<>();
        config.put("timeout", "3000");
        String t = Optional.ofNullable(config.get("timeout")).orElse("默认值");
        String r = Optional.ofNullable(config.get("retries")).orElse("默认值");
        System.out.println("timeout = " + t + ", retries = " + r);
        Optional.ofNullable(config.get("host")).ifPresentOrElse(
                v -> System.out.println("host = " + v),
                () -> System.out.println("host 缺失，走降级逻辑"));
        System.out.println("链式风格：user.getAddr().map(Addr::city).orElse(\"未知\")");

        section("[10] 日期时间 API：不可变、线程安全（JDK8+）");
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        var nextWeek = today.plusWeeks(1);
        long days = ChronoUnit.DAYS.between(today, nextWeek);
        var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("现在  : " + fmt.format(now));
        System.out.println("一周后: " + nextWeek + "（相差 " + days + " 天）");
        Instant instant = Instant.now();
        System.out.println("时间戳: " + instant.toEpochMilli() + " ms（Instant = 机器时间视角）");

        System.out.println();
        System.out.println("✓ Demo4 完成：现代语法 10 组知识点全部实测通过");
    }

    // ---------------- 支撑类型 ----------------

    /** 坐标点：紧凑构造器演示参数校验。 */
    record Point(int x, int y) {
        Point {
            if (x < 0 || y < 0) {
                throw new IllegalArgumentException("坐标不能为负: (" + x + "," + y + ")");
            }
        }
    }

    record User(String name, int age) {
        User {
            if (age < 0 || age > 150) {
                throw new IllegalArgumentException("非法年龄: " + age);
            }
        }
    }

    sealed interface Shape permits Circle, Rect {
    }

    record Circle(double r) implements Shape {
    }

    record Rect(double w, double h) implements Shape {
    }

    static void section(String title) {
        System.out.println();
        System.out.println("──────────────────────────────────────────────");
        System.out.println("▶ " + title);
        System.out.println("──────────────────────────────────────────────");
    }
}