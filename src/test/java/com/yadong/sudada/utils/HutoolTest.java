package com.yadong.sudada.utils;

import cn.hutool.Hutool;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yadong.sudada.mapper.UserMapper;
import org.apache.commons.codec.cli.Digest;
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

    @Test
    public void testMD5() {
        Long id = 1L;
        String result = "result";

        String key = DigestUtil.md5Hex(result);
        String caffeineKey = id + ":" + key;
        System.out.println(caffeineKey);
    }

    @Test
    public void testGenerateId() {
        long id = IdUtil.getSnowflakeNextId();
        System.out.println("id = " + id);
    }
}
