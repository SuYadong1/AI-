package com.yadong.sudada.service;
import java.util.Date;

import javax.annotation.Resource;

import com.yadong.sudada.mapper.UserMapper;
import com.yadong.sudada.model.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 用户服务测试
 *
 *  
 *   
 */
@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;


    @Resource
    private UserMapper userMapper;

    @Test
    void userRegister() {
        String userAccount = "yadong";
        String userPassword = "";
        String checkPassword = "123456";
        try {
            long result = userService.userRegister(userAccount, userPassword, checkPassword);
            Assertions.assertEquals(-1, result);
            userAccount = "yu";
            result = userService.userRegister(userAccount, userPassword, checkPassword);
            Assertions.assertEquals(-1, result);
        } catch (Exception e) {

        }
    }

    @Test
    void testInsert() {
        User user = new User();
        user.setUserAccount("test");
        user.setUserPassword("test");
        user.setUnionId("test");
        user.setMpOpenId("test");
        user.setUserName("test");
        user.setUserAvatar("test");
        user.setUserProfile("test");
        user.setUserRole("user");

        int result = userMapper.insert(user);

        Assertions.assertEquals(1, result);
    }

    /**
     * 测试updateById会不会对将实体类中的属性为null或者空字符串的字段也更新
     */
    @Test
    public void testUpdateById() {
        User user = new User();
        user.setId(1916010620101079042L);
        user.setUserAccount(null);
        user.setUserPassword("");
        user.setMpOpenId("");
        user.setUserName("");
        user.setUserAvatar("");
        user.setUserProfile("");
        user.setUserRole("");
        user.setUnionId(null);

        int result = userMapper.updateById(user);
        Assertions.assertEquals(1, result);
    }
}
