package com.yadong.sudada.model.vo;

import com.yadong.sudada.model.entity.UserAnswer;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户答题记录视图
 */
@Data
public class UserAnswerVO implements Serializable {
    /**
     *
     */
    private Long id;

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 应用类型（0-得分类，1-角色测评类）
     */
    private Integer appType;

    /**
     * 评分策略（0-自定义，1-AI）
     */
    private Integer scoringStrategy;

    /**
     * 用户答案（JSON 数组）
     */
    private String choices;

    /**
     * 评分结果 id
     */
    private Long resultId;

    /**
     * 结果名称，如物流师
     */
    private String resultName;

    /**
     * 结果描述
     */
    private String resultDesc;

    /**
     * 结果图标
     */
    private String resultPicture;

    /**
     * 得分
     */
    private Integer resultScore;

    /**
     * 用户 id
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
     * @param UserAnswerVO
     * @return
     */
    public static UserAnswer voToObj(UserAnswerVO UserAnswerVO) {
        if (UserAnswerVO == null) {
            return null;
        }
        UserAnswer UserAnswer = new UserAnswer();
        BeanUtils.copyProperties(UserAnswerVO, UserAnswer);
        return UserAnswer;
    }

    /**
     * 对象转封装类
     *
     * @param UserAnswer
     * @return
     */
    public static UserAnswerVO objToVo(UserAnswer UserAnswer) {
        if (UserAnswer == null) {
            return null;
        }
        UserAnswerVO UserAnswerVO = new UserAnswerVO();
        BeanUtils.copyProperties(UserAnswer, UserAnswerVO);
        return UserAnswerVO;
    }
}
