package com.yadong.sudada.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yadong.sudada.common.ErrorCode;
import com.yadong.sudada.constant.CommonConstant;
import com.yadong.sudada.exception.ThrowUtils;
import com.yadong.sudada.mapper.QuestionMapper;
import com.yadong.sudada.model.dto.question.QuestionQueryRequest;
import com.yadong.sudada.model.entity.App;
import com.yadong.sudada.model.entity.Question;
import com.yadong.sudada.model.entity.User;
import com.yadong.sudada.model.vo.QuestionVO;
import com.yadong.sudada.model.vo.UserVO;
import com.yadong.sudada.service.AppService;
import com.yadong.sudada.service.QuestionService;
import com.yadong.sudada.service.UserService;
import com.yadong.sudada.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 问题服务实现
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    /**
     * 校验数据
     *
     * @param Question
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestion(Question Question, boolean add) {
        ThrowUtils.throwIf(Question == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String questionContent = Question.getQuestionContent();
        Long appId = Question.getAppId();
        // 创建数据时，参数不能为空
        if (add) {
            // 每个问题必须对应一个app，问题内容必填
            ThrowUtils.throwIf(StringUtils.isBlank(questionContent), ErrorCode.PARAMS_ERROR, "问题不能为空");
            ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "appId非法");
        }

        // 通用的校验规则
        if (appId == null || appId <= 0) {
            App app = appService.getById(appId);
            ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "app不存在");
        }
    }

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = questionQueryRequest.getId();
        Long notId = questionQueryRequest.getNotId();
        String questionContent = questionQueryRequest.getQuestionContent();
        Long userId = questionQueryRequest.getUserId();
        Long appId = questionQueryRequest.getAppId();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();

        // 补充需要的查询条件
        // 模糊搜索
        queryWrapper.like(StringUtils.isNotBlank(questionContent), "questionContent",questionContent);

        // 精确查询
        queryWrapper.ne(notId != null && notId > 0, "id", notId);
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(userId != null && userId > 0, "userId", userId);
        queryWrapper.eq(appId != null && appId > 0, "appId", appId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取问题封装
     *
     * @param question
     * @param request
     * @return
     */
    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        // 对象转封装类
        QuestionVO questionVO = QuestionVO.objToVo(question);

        // 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionVO.setUser(userVO);

        return questionVO;
    }

    /**
     * 分页获取问题封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> QuestionList = questionPage.getRecords();
        Page<QuestionVO> QuestionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollUtil.isEmpty(QuestionList)) {
            return QuestionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionVO> QuestionVOList = QuestionList.stream()
                .map(QuestionVO::objToVo)
                .collect(Collectors.toList());

        // 关联查询用户信息
        Set<Long> userIdSet = QuestionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充信息
        QuestionVOList.forEach(QuestionVO -> {
            Long userId = QuestionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            QuestionVO.setUser(userService.getUserVO(user));
        });

        QuestionVOPage.setRecords(QuestionVOList);
        return QuestionVOPage;
    }

}
