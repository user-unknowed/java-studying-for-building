package com.example.oddeven;

/**
 * @abstract AbstractChecker
 * @brief 奇偶数判断抽象基类
 * 
 * 提供奇偶数判断的通用功能，包含模板方法模式。
 * 抽象类不能被实例化，只能被继承。
 * 
 * @see EvenOddChecker
 */
public abstract class AbstractChecker implements EvenOddChecker {
    
    /**
     * @brief 统计判断次数（私有成员变量，体现封装性）
     */
    private int checkCount;
    
    /**
     * @brief 判断器名称（私有成员变量，体现封装性）
     */
    private String name;
    
    /**
     * @brief 无参构造函数
     */
    protected AbstractChecker() {
        this.checkCount = 0;
        this.name = "AbstractChecker";
    }
    
    /**
     * @brief 带参数构造函数
     * 
     * @param name 判断器名称
     */
    protected AbstractChecker(String name) {
        this.checkCount = 0;
        this.name = name;
    }
    
    /**
     * @brief 获取判断次数
     * 
     * @return 判断次数
     */
    public int getCheckCount() {
        return checkCount;
    }
    
    /**
     * @brief 获取判断器名称
     * 
     * @return 判断器名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * @brief 模板方法：完整的判断流程
     * 
     * 定义算法骨架，具体步骤由子类实现。
     * 
     * @param number 待判断的整数
     * @return 判断结果的详细描述
     */
    public String checkAndReport(int number) {
        // 记录判断次数
        checkCount++;
        
        // 调用子类实现的方法
        boolean even = isEven(number);
        
        // 生成报告
        return generateReport(number, even);
    }
    
    /**
     * @brief 生成判断报告（抽象方法，由子类实现）
     * 
     * @param number 待判断的整数
     * @param isEven 是否为偶数
     * @return 报告字符串
     */
    protected abstract String generateReport(int number, boolean isEven);
    
    /**
     * @brief 判断整数是否为奇数（实现自接口）
     * 
     * 默认实现：调用isEven方法的取反
     * 
     * @param number 待判断的整数
     * @return 如果是奇数返回true，否则返回false
     */
    @Override
    public boolean isOdd(int number) {
        return !isEven(number);
    }
}