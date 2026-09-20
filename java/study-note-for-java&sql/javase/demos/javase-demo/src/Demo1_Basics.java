import java.math.BigDecimal;

/**
 * JavaSE 基础演示 1: 类型系统 与 常见陷阱
 *
 * 覆盖知识点:
 *   1. 八种基本类型
 *   2. 整数溢出
 *   3. 浮点精度陷阱 (钱不能用 double!)
 *   4. String 常量池 & == vs equals
 *   5. 包装类自动装箱缓存陷阱
 *   6. switch 表达式 (JDK14+)
 *   7. 数组与工具类
 */
public class Demo1_Basics {

    public static void main(String[] args) {
        System.out.println("Java 版本: " + System.getProperty("java.version"));

        System.out.println("\n===== 1. 八种基本类型 =====");
        byte b = 127;                     // 1字节, [-128, 127]
        short s = 32767;                  // 2字节
        int i = 2147483647;               // 4字节 (最常用)
        long l = 9223372036854775807L;    // 8字节 (字面量要加 L)
        float f = 3.14f;                  // 4字节 (字面量要加 f)
        double d = 3.141592653589793;     // 8字节 (小数默认类型)
        char ch = '中';                    // 2字节, Unicode
        boolean bool = true;
        System.out.printf("byte=%d, short=%d, int=%d, long=%d%n", b, s, i, l);
        System.out.printf("float=%s, double=%s, char=%c, boolean=%s%n", f, d, ch, bool);
        System.out.println("注意: int 最大约 21 亿 —— 时间戳、ID 都要用 long!");

        System.out.println("\n===== 2. 整数溢出 (静默事故) =====");
        int max = Integer.MAX_VALUE;
        System.out.println("int 最大值      = " + max);
        System.out.println("int 最大值 + 1  = " + (max + 1) + "   ← 溢出! 静默变成负数, 不报错!");

        System.out.println("\n===== 3. 浮点精度: 钱绝对不能用 double =====");
        System.out.println("0.1 + 0.2 = " + (0.1 + 0.2) + "   ← 不等于 0.3!");
        BigDecimal x = new BigDecimal("0.1");
        BigDecimal y = new BigDecimal("0.2");
        System.out.println("BigDecimal: 0.1 + 0.2 = " + x.add(y) + "   ← 金额必须这样算");

        System.out.println("\n===== 4. String 常量池 & == vs equals =====");
        String s1 = "hello";
        String s2 = "hello";               // 同一个常量池对象
        String s3 = new String("hello");   // 堆上新建的对象
        System.out.println("s1 == s2        : " + (s1 == s2) + "   ← 同一常量池对象, 地址相同");
        System.out.println("s1 == s3        : " + (s1 == s3) + "   ← new 出来的不同对象");
        System.out.println("s1.equals(s3)   : " + s1.equals(s3) + "    ← 比较内容, 永远用 equals");
        System.out.println("s1 == s3.intern(): " + (s1 == s3.intern()) + "   ← intern() 把字符串'放回池'");

        System.out.println("\n===== 5. 包装类自动装箱缓存陷阱 =====");
        Integer a1 = 127, a2 = 127;
        Integer b1 = 128, b2 = 128;
        System.out.println("127 == 127 : " + (a1 == a2) + "   ← 缓存 [-128,127] 内, 是同一对象");
        System.out.println("128 == 128 : " + (b1 == b2) + "   ← 超出缓存, 是不同对象");
        System.out.println("结论: 包装类比较永远用 .equals() !");

        System.out.println("\n===== 6. switch 表达式 (JDK14+) =====");
        int day = 3;
        String type = switch (day) {
            case 1, 2, 3, 4, 5 -> "工作日";
            case 6, 7 -> "休息日";
            default -> "非法日期";
        };
        System.out.println("day = " + day + " → " + type + "   ← 还能作为表达式直接赋值");

        System.out.println("\n===== 7. 数组与 Arrays 工具类 =====");
        int[] nums = {5, 2, 8, 1, 9};
        java.util.Arrays.sort(nums);
        System.out.println("排序后: " + java.util.Arrays.toString(nums));
        for (int n : nums) {
            if (n > 4) System.out.print(n + " ");
        }
        System.out.println("← 增强 for-each 遍历");
    }
}