/** 被 JUnit 测试的计算类（演示单元测试的"被测代码"长什么样）。 */
public class Calc {

    public static int add(int a, int b) {
        return a + b;
    }

    /** 除法：除数为 0 时抛异常（故意留一条异常路径给测试断言）。 */
    public static int divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("除数不能为0");
        }
        return a / b;
    }
}