import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JUnit 5 单元测试演示。
 *
 * 编译：javac -encoding UTF-8 -cp "lib/junit-platform-console-standalone-1.10.2.jar" -d out-junit junit/*.java
 * 运行：java -jar lib/junit-platform-console-standalone-1.10.2.jar --class-path out-junit --select-class CalcTest
 */
class CalcTest {

    @BeforeAll
    static void beforeAll() {
        System.out.println("[JUnit] 测试套件启动");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("[JUnit] 测试套件结束");
    }

    @Test
    @DisplayName("2 + 3 应当等于 5")
    void addWorks() {
        assertEquals(5, Calc.add(2, 3), "2+3 应为 5");
    }

    @Test
    @DisplayName("10 / 3 整数除法应当等于 3")
    void divideWorks() {
        assertEquals(3, Calc.divide(10, 3));
    }

    @Test
    @DisplayName("除以 0 应当抛 ArithmeticException 且带正确消息")
    void divideByZeroThrows() {
        var ex = assertThrows(ArithmeticException.class, () -> Calc.divide(1, 0));
        assertEquals("除数不能为0", ex.getMessage());
    }
}