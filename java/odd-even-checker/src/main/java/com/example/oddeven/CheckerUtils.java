package com.example.oddeven;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @class CheckerUtils
 * @brief 奇偶数判断工具类
 * 
 * 提供各种静态工具方法，展示Java的泛型、集合框架、Lambda表达式等特性。
 * 工具类通常将构造函数设为私有，所有方法为静态。
 */
public final class CheckerUtils {
    
    /**
     * @brief 私有构造函数，防止实例化工具类
     */
    private CheckerUtils() {
        throw new AssertionError("工具类不允许实例化");
    }
    
    /**
     * @brief 判断整数是否为偶数（静态方法）
     * 
     * @param number 待判断的整数
     * @return 如果是偶数返回true，否则返回false
     */
    public static boolean isEven(int number) {
        return number % 2 == 0;
    }
    
    /**
     * @brief 判断整数是否为奇数（静态方法）
     * 
     * @param number 待判断的整数
     * @return 如果是奇数返回true，否则返回false
     */
    public static boolean isOdd(int number) {
        return !isEven(number);
    }
    
    /**
     * @brief 判断包装类Integer是否为偶数（方法重载）
     * 
     * 展示Java方法重载特性和包装类的使用。
     * 
     * @param number 待判断的Integer对象
     * @return 如果是偶数返回true，否则返回false
     * @throws NullPointerException 如果number为null
     */
    public static boolean isEven(Integer number) {
        if (number == null) {
            throw new NullPointerException("输入参数不能为null");
        }
        return isEven(number.intValue());
    }
    
    /**
     * @brief 判断包装类Integer是否为奇数（方法重载）
     * 
     * @param number 待判断的Integer对象
     * @return 如果是奇数返回true，否则返回false
     * @throws NullPointerException 如果number为null
     */
    public static boolean isOdd(Integer number) {
        return !isEven(number);
    }
    
    /**
     * @brief 使用泛型过滤偶数列表
     * 
     * 展示Java泛型特性和集合框架的应用。
     * 
     * @param numbers 输入的整数列表
     * @return 过滤后的偶数列表
     */
    public static <T extends Number> List<T> filterEvens(List<T> numbers) {
        return numbers.stream()
                      .filter(n -> isEven(n.intValue()))
                      .collect(Collectors.toList());
    }
    
    /**
     * @brief 使用泛型过滤奇数列表
     * 
     * @param numbers 输入的整数列表
     * @return 过滤后的奇数列表
     */
    public static <T extends Number> List<T> filterOdds(List<T> numbers) {
        return numbers.stream()
                      .filter(n -> isOdd(n.intValue()))
                      .collect(Collectors.toList());
    }
    
    /**
     * @brief 获取奇偶数判断结果的枚举类型
     * 
     * @param number 待判断的整数
     * @return 判断结果枚举
     */
    public static EvenOddResult getResult(int number) {
        return isEven(number) ? EvenOddResult.EVEN : EvenOddResult.ODD;
    }
    
    /**
     * @brief 批量判断整数列表
     * 
     * 使用Lambda表达式和Stream API。
     * 
     * @param numbers 整数列表
     * @return 判断结果列表
     */
    public static List<String> batchCheck(List<Integer> numbers) {
        return numbers.stream()
                      .map(n -> {
                          boolean even = isEven(n);
                          return String.format("%d -> %s", n, even ? "偶数" : "奇数");
                      })
                      .collect(Collectors.toList());
    }
    
    /**
     * @brief 统计列表中奇数和偶数的数量
     * 
     * @param numbers 整数列表
     * @return 统计结果数组，[偶数数量, 奇数数量]
     */
    public static int[] countEvensAndOdds(List<Integer> numbers) {
        int evenCount = 0;
        int oddCount = 0;
        
        for (int num : numbers) {
            if (isEven(num)) {
                evenCount++;
            } else {
                oddCount++;
            }
        }
        
        return new int[]{evenCount, oddCount};
    }
    
    /**
     * @enum EvenOddResult
     * @brief 奇偶数判断结果枚举
     */
    public enum EvenOddResult {
        EVEN("偶数"),
        ODD("奇数");
        
        private final String description;
        
        EvenOddResult(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * @brief 生成测试数据列表
     * 
     * @param start 起始值
     * @param end 结束值
     * @return 测试数据列表
     */
    public static List<Integer> generateTestData(int start, int end) {
        List<Integer> data = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            data.add(i);
        }
        return data;
    }
}