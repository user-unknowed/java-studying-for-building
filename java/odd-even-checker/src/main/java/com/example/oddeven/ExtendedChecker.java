package com.example.oddeven;

/**
 * @class ExtendedChecker
 * @brief 扩展奇偶数判断器
 * 
 * 继承自AbstractChecker抽象类，演示Java继承特性。
 * 实现模板方法模式中的抽象方法。
 * 
 * @see AbstractChecker, EvenOddChecker
 */
public class ExtendedChecker extends AbstractChecker {
    
    /**
     * @brief 偶数计数器（私有成员变量）
     */
    private int evenCount;
    
    /**
     * @brief 奇数计数器（私有成员变量）
     */
    private int oddCount;
    
    /**
     * @brief 构造函数
     */
    public ExtendedChecker() {
        super("ExtendedChecker");
        this.evenCount = 0;
        this.oddCount = 0;
    }
    
    /**
     * @brief 判断整数是否为偶数（重写父类方法）
     * 
     * 使用位运算算法实现。
     * 
     * @param number 待判断的整数
     * @return 如果是偶数返回true，否则返回false
     */
    @Override
    public boolean isEven(int number) {
        return (number & 1) == 0;
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
        return String.format("数字 %d 是 %s", number, result);
    }
    
    /**
     * @brief 获取算法名称
     * 
     * @return 算法名称字符串
     */
    @Override
    public String getAlgorithmName() {
        return "扩展位运算算法";
    }
    
    /**
     * @brief 生成判断报告（实现抽象方法）
     * 
     * @param number 待判断的整数
     * @param isEven 是否为偶数
     * @return 报告字符串
     */
    @Override
    protected String generateReport(int number, boolean isEven) {
        // 更新计数器
        if (isEven) {
            evenCount++;
        } else {
            oddCount++;
        }
        
        // 生成详细报告
        StringBuilder report = new StringBuilder();
        report.append("========== 判断报告 ==========\n");
        report.append(String.format("判断数字: %d\n", number));
        report.append(String.format("判断结果: %s\n", isEven ? "偶数" : "奇数"));
        report.append(String.format("算法名称: %s\n", getAlgorithmName()));
        report.append(String.format("累计判断: %d 次\n", getCheckCount()));
        report.append(String.format("偶数统计: %d 个\n", evenCount));
        report.append(String.format("奇数统计: %d 个\n", oddCount));
        report.append("==============================");
        
        return report.toString();
    }
    
    /**
     * @brief 获取偶数统计数
     * 
     * @return 偶数统计数
     */
    public int getEvenCount() {
        return evenCount;
    }
    
    /**
     * @brief 获取奇数统计数
     * 
     * @return 奇数统计数
     */
    public int getOddCount() {
        return oddCount;
    }
}