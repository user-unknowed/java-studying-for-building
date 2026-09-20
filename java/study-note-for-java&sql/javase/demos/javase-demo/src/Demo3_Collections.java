import java.util.*;
import java.util.stream.*;

/**
 * JavaSE 基础演示 3: 集合框架 + 泛型 + Lambda/Stream
 *
 * 覆盖知识点:
 *   1. List / Set / Map 三兄弟的区别
 *   2. 泛型: 编译期的类型安全
 *   3. Lambda + Stream 声明式数据处理 (现代 Java 标志)
 *   4. 分组统计
 *   5. 迭代时删除元素的经典陷阱 (ConcurrentModificationException)
 */
public class Demo3_Collections {

    /** record: JDK16+ 不可变数据类 (替代一堆 getter/setter/toString) */
    record Person(String name, int age, String city) {}

    public static void main(String[] args) {
        System.out.println("===== 1. List / Set / Map 三兄弟 =====");
        List<String> list = new ArrayList<>(List.of("苹果", "香蕉", "苹果"));
        System.out.println("List (有序, 可重复): " + list);

        Set<String> set = new HashSet<>(List.of("苹果", "香蕉", "苹果"));
        System.out.println("Set  (去重):        " + set);

        Map<String, Integer> map = new HashMap<>();
        map.put("苹果", 5);
        map.put("香蕉", 3);
        System.out.println("Map  (键→值):       " + map + ",  取苹果=" + map.get("苹果"));

        System.out.println("\n===== 2. 泛型: 编译期类型约束 =====");
        List<Person> people = new ArrayList<>();
        people.add(new Person("张三", 25, "北京"));
        people.add(new Person("李四", 17, "上海"));
        people.add(new Person("王五", 32, "北京"));
        people.add(new Person("赵六", 28, "广州"));
        people.add(new Person("孙七", 19, "北京"));
        // people.add("字符串");  // ← 编译错误! 泛型保证集合里只能是 Person
        System.out.println("已添加 " + people.size() + " 个人 (放错类型编译期就报错)");

        System.out.println("\n===== 3. Lambda + Stream: 声明式数据流水线 =====");
        // 需求: 找出北京的成年人, 按年龄升序, 取名字, 用逗号连接
        String result = people.stream()                                  // 变成流水线
                .filter(p -> p.age() >= 18)                              // 过滤: 只要成年人
                .filter(p -> p.city().equals("北京"))                    // 过滤: 只要北京的
                .sorted(Comparator.comparingInt(Person::age))            // 排序: 按年龄
                .map(Person::name)                                       // 映射: 对象→名字
                .collect(Collectors.joining(", "));                      // 收集: 拼成字符串
        System.out.println("北京成人(按年龄): " + result);
        System.out.println("  对比: 如果用 for 循环 + if, 这段要写十几行");

        System.out.println("\n===== 4. 分组统计 =====");
        Map<String, Long> byCity = people.stream()
                .collect(Collectors.groupingBy(Person::city, Collectors.counting()));
        System.out.println("按城市分组计数: " + byCity);

        double avgAge = people.stream().mapToInt(Person::age).average().orElse(0);
        System.out.println("平均年龄: " + avgAge);

        System.out.println("\n===== 5. 遍历 Map 的正确姿势 =====");
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            System.out.println("  " + e.getKey() + " → " + e.getValue());
        }

        System.out.println("\n===== 6. 迭代时删除元素陷阱 =====");
        List<String> cart = new ArrayList<>(List.of("苹果", "香蕉", "橘子", "葡萄"));
        try {
            for (String s : cart) {
                if (s.equals("香蕉")) cart.remove(s);   // 错误示范!
            }
        } catch (ConcurrentModificationException ex) {
            System.out.println("  抛出了: " + ex.getClass().getSimpleName() + "   ← 迭代中修改集合的著名陷阱");
        }
        cart.removeIf(s -> s.equals("香蕉"));            // 正确姿势
        System.out.println("  removeIf 后: " + cart);

        System.out.println("\nDemo3 完成 ✓");
    }
}