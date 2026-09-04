package com.example.oddeven;

/**
 * @class BitwiseChecker
 * @brief 位运算奇偶数判断器
 * 
 * 使用位运算(number & 1 == 0)判断奇偶数，这是更高效的算法。
 * 原理：二进制数的最低位为0表示偶数，为1表示奇数。
 * 
 * @see EvenOddChecker, ModuloChecker
 */
public class BitwiseChecker implements EvenOddChecker {
    
    /**
     * @brief 判断整数是否为偶数（位运算算法）
     * 
     * 使用位与运算number & 1 == 0判断。
     * 位运算比模运算更高效，因为计算机直接操作二进制。
     * 
     * @param number 待判断的整数
     * @return 如果是偶数返回true，否则返回false
     */
    @Override
    public boolean isEven(int number) {
        return (number & 1) == 0;
    }
    
    /**
     * @brief 判断整数是否为奇数（位运算算法）
     * 
     * 使用位与运算number & 1 == 1判断。
     * 
     * @param number 待判断的整数
     * @return 如果是奇数返回true，否则返回false
     */
    @Override
    public boolean isOdd(int number) {
        return (number & 1) == 1;
    }
    
    /**
     * @brief 获取判断结果的描述信息
     * 
     * @param number 待判断的整数
     * @return 判断结果的字符串描述
     */
    @Override
    public String getResultDescription(int number) {
        String result = isEven(number) ? "偶数" : "奇数";
        return String.format("数字 %d 是 %s（使用位运算算法）", number, result);
    }
    
    /**
     * @brief 获取算法名称
     * 
     * @return 算法名称字符串
     */
    @Override
    public String getAlgorithmName() {
        return "位运算算法 (Bitwise Operation)";
    }
}