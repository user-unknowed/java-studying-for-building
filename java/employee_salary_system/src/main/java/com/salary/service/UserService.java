package com.salary.service;

import com.salary.model.User;
import com.salary.exception.SalaryException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 用户业务服务类
 * 提供用户登录、注册、密码修改等功能
 */
public class UserService {

    // 模拟用户数据存储，实际应用中应使用数据库
    private static Map<String, User> userStorage = new HashMap<>();

    // 用户名格式：字母数字下划线，4-20位
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{4,20}$");
    
    // 密码格式：至少6位
    private static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * 用户登录验证
     * 验证用户名和密码是否匹配
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户对象（密码置空）
     * @throws SalaryException 如果登录失败
     */
    public User login(String username, String password) {
        // 参数验证
        if (username == null || username.trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "用户名不能为空");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "密码不能为空");
        }

        // 查找用户
        User user = findUserByUsername(username);
        if (user == null) {
            throw new SalaryException(SalaryException.ERR_DATA_NOT_FOUND, "用户名不存在");
        }

        // 验证密码
        if (!password.equals(user.getPassword())) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "密码错误");
        }

        // 返回用户信息（隐藏密码）
        User result = new User();
        result.setId(user.getId());
        result.setUsername(user.getUsername());
        result.setRealName(user.getRealName());
        result.setRole(user.getRole());
        result.setCreateTime(user.getCreateTime());
        result.setPassword(null); // 安全起见，不返回密码

        return result;
    }

    /**
     * 用户注册
     * 创建新用户账号
     * 
     * @param user 用户对象（需包含用户名、密码、真实姓名）
     * @return 注册成功的用户ID
     * @throws SalaryException 如果注册失败
     */
    public Long register(User user) {
        // 参数验证
        validateUserForRegister(user);

        // 检查用户名是否已存在
        if (findUserByUsername(user.getUsername()) != null) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "用户名已存在，请选择其他用户名");
        }

        // 设置创建时间
        if (user.getCreateTime() == null) {
            user.setCreateTime(LocalDateTime.now());
        }

        // 设置默认角色
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("普通用户");
        }

        // 生成用户ID（模拟）
        Long userId = System.currentTimeMillis();
        user.setId(userId);

        // 存储用户
        userStorage.put(user.getUsername(), user);

        return userId;
    }

    /**
     * 修改密码
     * 用户修改自己的密码，需要验证旧密码
     * 
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @throws SalaryException 如果修改失败
     */
    public void updatePassword(int userId, String oldPassword, String newPassword) {
        // 参数验证
        if (userId <= 0) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "用户ID无效");
        }

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "旧密码不能为空");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "新密码不能为空");
        }

        // 验证新密码长度
        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, 
                    "新密码长度不能少于" + MIN_PASSWORD_LENGTH + "位");
        }

        // 查找用户
        User user = findUserById(userId);
        if (user == null) {
            throw new SalaryException(SalaryException.ERR_DATA_NOT_FOUND, "用户不存在");
        }

        // 验证旧密码
        if (!oldPassword.equals(user.getPassword())) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "旧密码错误");
        }

        // 检查新密码是否与旧密码相同
        if (oldPassword.equals(newPassword)) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "新密码不能与旧密码相同");
        }

        // 更新密码
        user.setPassword(newPassword);
    }

    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户对象，如果不存在返回null
     */
    private User findUserByUsername(String username) {
        return userStorage.get(username);
    }

    /**
     * 根据用户ID查找用户
     * 
     * @param userId 用户ID
     * @return 用户对象，如果不存在返回null
     */
    private User findUserById(Long userId) {
        for (User user : userStorage.values()) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    /**
     * 验证用户注册信息的合法性
     * 
     * @param user 用户对象
     * @throws SalaryException 如果信息不合法
     */
    private void validateUserForRegister(User user) {
        if (user == null) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "用户信息不能为空");
        }

        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "用户名不能为空");
        }

        if (!USERNAME_PATTERN.matcher(user.getUsername()).matches()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, 
                    "用户名格式不正确，只能包含字母、数字和下划线，长度4-20位");
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "密码不能为空");
        }

        if (user.getPassword().length() < MIN_PASSWORD_LENGTH) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, 
                    "密码长度不能少于" + MIN_PASSWORD_LENGTH + "位");
        }

        if (user.getRealName() == null || user.getRealName().trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "真实姓名不能为空");
        }
    }

    /**
     * 初始化测试用户（用于测试目的）
     */
    public static void initTestUsers() {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("admin123");
        admin.setRealName("系统管理员");
        admin.setRole("管理员");
        admin.setCreateTime(LocalDateTime.now());
        userStorage.put("admin", admin);

        User testUser = new User();
        testUser.setId(2L);
        testUser.setUsername("test_user");
        testUser.setPassword("test123");
        testUser.setRealName("测试用户");
        testUser.setRole("普通用户");
        testUser.setCreateTime(LocalDateTime.now());
        userStorage.put("test_user", testUser);
    }
}
