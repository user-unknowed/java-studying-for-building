package com.example.oddeven;

/**
 * @class ModuloChecker
 * @brief 模运算奇偶数判断器
 * 
 * 使用取模运算(number % 2 == 0)判断奇偶数，这是最直观的算法。
 * 实现EvenOddChecker接口，体现Java接口实现特性。
 * 
 * @see EvenOddChecker, BitwiseChecker
 */
public class ModuloChecker implements EvenOddChecker {
    
    /**
     * @brief 判断整数是否为偶数（模运算算法）
     * 
     * 使用number % 2 == 0判断，若余数为0则为偶数。
     * 
     * @param number 待判断的整数
     * @return 如果是偶数返回true，否则返回false
     */
    @Override
    public boolean isEven(int number) {
        return number % 2 == 0;
    }
    
    /**
     * @brief 判断整数是否为奇数（模运算算法）
     * 
     * 使用number % 2 != 0判断，若余数不为0则为奇数。
     * 
     * @param number 待判断的整数
     * @return 如果是奇数返回true，否则返回false
     */
    @Override
    public boolean isOdd(int number) {
        return number % 2 != 0;
    }
    
    /**
     * @brief 获取判断结果的描述信息
     * 
     * @param number 待判断的整数
     * @return 判断结果的字符串描述
     */
    @Override
    public String getResultDescription(int number) {
        // 使用三元运算符简化条件判断
        String result = isEven(number) ? "偶数" : "奇数";
        return String.format("数字 %d 是 %s（使用模运算算法）", number, result);
    }
    
    /**
     * @brief 获取算法名称
     * 
     * @return 算法名称字符串
     */
    @Override
    public String getAlgorithmName() {
        return "模运算算法 (Modulo Operation)";
    }
}