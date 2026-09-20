/**
 * 最小 Java 程序 —— 用于演示 Java 的"编译 → 字节码 → JVM 执行"全过程
 *
 * 之后会执行:
 *   javac Hello.java    → 生成 Hello.class (字节码)
 *   javap -c Hello      → 反汇编, 亲眼看字节码指令长什么样
 *   java Hello          → JVM 执行字节码
 */
public class Hello {
    public static void main(String[] args) {
        int a = 1;
        int b = 2;
        int c = a + b;
        System.out.println("1 + 2 = " + c);
    }
}