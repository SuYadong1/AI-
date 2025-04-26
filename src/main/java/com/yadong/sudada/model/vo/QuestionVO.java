package com.yadong.sudada.model.vo;

import cn.hutool.json.JSONUtil;
import com.yadong.sudada.model.entity.Question;
import com.yadong.sudada.model.entity.QuestionContent;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 问题视图
 */
@Data
public class QuestionVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 题目内容（数组，存在数据库中的是json格式）
     */
    private List<QuestionContent> questionContents;

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 封装类转对象
     *
     * @param QuestionVO
     * @return
     */
    public static Question voToObj(QuestionVO QuestionVO) {
        if (QuestionVO == null) {
            return null;
        }
        Question Question = new Question();
        BeanUtils.copyProperties(QuestionVO, Question);
        // 将集合转为json字符串
        List<QuestionContent> qc = QuestionVO.getQuestionContents();
        Question.setQuestionContent(JSONUtil.toJsonStr(qc));
        return Question;
    }

    /**
     * 对象转封装类
     *
     * @param Question
     * @return
     */
    public static QuestionVO objToVo(Question Question) {
        if (Question == null) {
            return null;
        }
        QuestionVO QuestionVO = new QuestionVO();
        BeanUtils.copyProperties(Question, QuestionVO);
        // 将集合转为json字符串
        QuestionVO.setQuestionContents(JSONUtil.toList(Question.getQuestionContent(), QuestionContent.class));
        return QuestionVO;
    }
}
