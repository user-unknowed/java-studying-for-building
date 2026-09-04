package com.salary;

import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper;
import com.salary.ui.LoginFrame;
import javax.swing.*;

/**
 * 员工工资管理系统 - 主程序入口
 * 
 * 功能特性：
 * - JDBC连接MySQL数据库
 * - Swing图形用户界面
 * - 事件处理（监听器、适配器、匿名内部类）
 * - 异常处理机制
 * - JXL库操作Excel
 * - BeautyEye界面美化
 * - 成绩分析创新功能
 * 
 * @author System Developer
 * @version 1.0
 */
public class Main {
    
    /**
     * 主程序入口
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 设置Swing界面美化（BeautyEye）
        try {
            // 设置BeautyEye外观
            BeautyEyeLNFHelper.launchBeautyEyeLNF();
            
            // 可选：设置关闭图标
            UIManager.put("RootPane.setupButtonVisible", false);
            
            System.out.println("BeautyEye界面美化启动成功！");
        } catch (Exception e) {
            System.err.println("BeautyEye启动失败，使用默认外观：" + e.getMessage());
        }
        
        // 设置UI管理器属性
        try {
            // 设置默认字体
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 使用SwingUtilities在EDT线程中启动GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // 创建并显示登录窗口
                    LoginFrame loginFrame = new LoginFrame();
                    loginFrame.setVisible(true);
                    
                    System.out.println("员工工资管理系统启动成功！");
                    System.out.println("请使用账号登录系统...");
                    System.out.println("默认管理员账号：admin / admin123");
                    System.out.println("普通用户账号：user / user123");
                    
                } catch (Exception e) {
                    // 捕获启动异常
                    JOptionPane.showMessageDialog(null, 
                        "系统启动失败：" + e.getMessage(), 
                        "错误", 
                        JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        });
    }
}
