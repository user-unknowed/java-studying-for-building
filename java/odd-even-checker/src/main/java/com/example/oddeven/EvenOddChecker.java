package com.example.oddeven;

/**
 * @interface EvenOddChecker
 * @brief 奇偶数判断接口
 * 
 * 定义奇偶数判断的统一接口规范，体现Java接口特性。
 * 所有具体判断算法都需实现此接口，实现接口与实现的分离。
 * 
 * @see ModuloChecker, BitwiseChecker
 */
public interface EvenOddChecker {
    
    /**
     * @brief 判断整数是否为偶数
     * 
     * @param number 待判断的整数
     * @return 如果是偶数返回true，否则返回false
     */
    boolean isEven(int number);
    
    /**
     * @brief 判断整数是否为奇数
     * 
     * @param number 待判断的整数
     * @return 如果是奇数返回true，否则返回false
     */
    boolean isOdd(int number);
    
    /**
     * @brief 获取判断结果的描述信息
     * 
     * @param number 待判断的整数
     * @return 判断结果的字符串描述
     */
    String getResultDescription(int number);
    
    /**
     * @brief 获取算法名称
     * 
     * @return 算法名称字符串
     */
    String getAlgorithmName();
}