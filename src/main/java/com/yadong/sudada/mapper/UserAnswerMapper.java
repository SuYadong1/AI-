package com.yadong.sudada.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yadong.sudada.model.dto.app.AppAnswerCountDTO;
import com.yadong.sudada.model.dto.app.AppAnswerResultCountDTO;
import com.yadong.sudada.model.entity.UserAnswer;

import java.util.List;


/**
* @author suydong
* @description 针对表【user_answer(用户答题记录)】的数据库操作Mapper
* @createDate 2025-04-23 21:04:37
* @Entity generator.domain.UserAnswer
*/
public interface UserAnswerMapper extends BaseMapper<UserAnswer> {

    /**
     * 查询最火的应用
     */
    List<AppAnswerCountDTO> getTopApp(Integer num);

    /**
     * 统计应用测评结果占比
     */
    List<AppAnswerResultCountDTO> getAppResultCount(Long appId);
}




