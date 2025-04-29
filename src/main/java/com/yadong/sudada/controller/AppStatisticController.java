package com.yadong.sudada.controller;

import com.yadong.sudada.common.BaseResponse;
import com.yadong.sudada.common.ResultUtils;
import com.yadong.sudada.mapper.UserAnswerMapper;
import com.yadong.sudada.model.dto.app.AppAnswerCountDTO;
import com.yadong.sudada.model.dto.app.AppAnswerResultCountDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/app/statistic")
@Slf4j
public class AppStatisticController {

    @Resource
    private UserAnswerMapper userAnswerMapper;

    /**
     * 查询前num个最火的应用
     */
    @GetMapping("/answer_count")
    public BaseResponse<List<AppAnswerCountDTO>> getTopApp(Integer num) {
        return ResultUtils.success(userAnswerMapper.getTopApp(num));
    }

    /**
     * 查询应用id为appId的应用的回答结果占比
     */
    @GetMapping("/answer_result_count")
    public BaseResponse<List<AppAnswerResultCountDTO>> getAppResultCount(Long appId) {
        return ResultUtils.success(userAnswerMapper.getAppResultCount(appId));
    }

}
