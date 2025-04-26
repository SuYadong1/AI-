package com.yadong.sudada.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yadong.sudada.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.yadong.sudada.model.entity.User;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class HutoolTest {
    @Resource
    private UserMapper userMapper;

    @Test
    public void testJsonUtil() {
        QueryWrapper<User> qw = new QueryWrapper<>();
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            System.out.println(user);
        }
    }
}
