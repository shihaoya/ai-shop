package com.sh.aishop.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sh.aishop.common.entity.User;
import com.sh.aishop.user.mapper.UserMapper;
import com.sh.aishop.util.SecurityUtil;
import com.sh.aishop.util.SnowflakeIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 系统启动时自动初始化管理员账号
 * 检测数据库中是否不存在管理员，不存在则创建默认管理员
 */
@Component
@Order(1)
public class AdminInitializer implements ApplicationRunner {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void run(ApplicationArguments args) {
        long adminCount = userMapper.selectCount(
            new LambdaQueryWrapper<User>()
                .eq(User::getRole, 1)
                .eq(User::getDeleted, 0)
        );

        if (adminCount == 0) {
            User admin = new User();
            admin.setId(SnowflakeIdUtil.nextId());
            admin.setUsername("admin");
            admin.setNickname("管理员");
            admin.setPassword(SecurityUtil.encryptPassword("admin123"));
            admin.setRole(1);
            admin.setParentId(0L);
            admin.setStatus(2);
            userMapper.insert(admin);
            System.out.println("========================================");
            System.out.println("系统检测到无管理员账号，已自动创建默认管理员");
            System.out.println("用户名: admin");
            System.out.println("密码: admin123");
            System.out.println("请首次登录后立即修改密码！");
            System.out.println("========================================");
        }
    }
}