import java.util.ArrayList;
import java.util.List;

/**
 * JavaSE 基础演示 2: 面向对象三大特性 —— 封装 / 继承 / 多态
 *
 * 覆盖知识点:
 *   1. 封装: private 字段 + 方法访问控制
 *   2. 多态: 父类引用指向子类对象 (Java 最有价值的一句话)
 *   3. 抽象类 vs 接口
 *   4. static: 属于"类"而不是"对象"
 *   5. final / instanceof 模式匹配 (JDK16+)
 */
public class Demo2_Oop {

    public static void main(String[] args) {
        System.out.println("===== 1. 封装: 数据只能通过受控方法访问 =====");
        Account acc = new Account("张三", 100);
        acc.deposit(50);
        // acc.balance = -999;   // ← 编译错误! private 字段外部碰不到
        System.out.println("存取后: " + acc);
        try {
            acc.deposit(-10);    // 校验逻辑在类内部兜住非法操作
        } catch (IllegalArgumentException e) {
            System.out.println("拦截非法操作: " + e.getMessage() + "   ← 封装的意义");
        }

        System.out.println("\n===== 2. 多态: 同一行代码, 不同对象不同行为 =====");
        List<Shape> shapes = new ArrayList<>();
        shapes.add(new Circle(2));
        shapes.add(new Rectangle(3, 4));
        shapes.add(new Triangle(3, 4, 5));

        double total = 0;
        for (Shape sh : shapes) {
            double area = sh.area();   // 编译时看 Shape, 运行时看实际对象 → 动态绑定
            total += area;
            System.out.printf("  %-6s 面积 = %.2f%n", sh.name(), area);
        }
        System.out.printf("总面积 = %.2f   ← 循环里没有一行 if/else, 全靠多态%n", total);

        System.out.println("\n===== 3. 接口: 描述'能力' (抽象类描述'是什么') =====");
        for (Shape sh : shapes) {
            if (sh instanceof WithCorners wc) {   // JDK16+ instanceof 模式匹配
                System.out.println("  " + sh.name() + " 有 " + wc.corners() + " 条边");
            }
        }

        System.out.println("\n===== 4. static: 属于类, 不属于对象 =====");
        System.out.println("已创建的图形总数: " + Shape.count + "   ← 类名直接访问静态字段");
    }

    // ==================== 封装示例 ====================
    static class Account {
        private final String owner;   // private: 外部不可见
        private double balance;       // 只能通过方法读写

        Account(String owner, double balance) {
            this.owner = owner;
            this.balance = balance;
        }

        void deposit(double amount) { // 对外的受控入口
            if (amount <= 0) throw new IllegalArgumentException("金额必须为正数!");
            this.balance += amount;
        }

        @Override
        public String toString() {
            return "Account{owner='" + owner + "', balance=" + balance + "}";
        }
    }

    // ==================== 抽象类: 是什么 ====================
    static abstract class Shape {
        static int count = 0;         // 静态字段: 所有实例共享
        final String name;            // final: 赋值后不可改

        Shape(String name) {
            this.name = name;
            count++;                  // 每 new 一个就计数 +1 (静待观察↑)
        }

        String name() { return name; }       // 具体方法: 子类直接继承

        abstract double area();              // 抽象方法: 子类必须实现
    }

    // ==================== 接口: 能什么 ====================
    interface WithCorners {
        int corners();                       // 只有承诺, 没有实现
    }

    static class Circle extends Shape {
        final double r;
        Circle(double r) { super("圆形"); this.r = r; }

        @Override double area() { return Math.PI * r * r; }
    }

    static class Rectangle extends Shape implements WithCorners {
        final double w, h;
        Rectangle(double w, double h) { super("矩形"); this.w = w; this.h = h; }

        @Override double area() { return w * h; }
        @Override public int corners() { return 4; }
    }

    static class Triangle extends Shape implements WithCorners {
        final double a, b, c;
        Triangle(double a, double b, double c) { super("三角形"); this.a = a; this.b = b; this.c = c; }

        @Override double area() {            // 海伦公式
            double p = (a + b + c) / 2;
            return Math.sqrt(p * (p - a) * (p - b) * (p - c));
        }
        @Override public int corners() { return 3; }
    }
}