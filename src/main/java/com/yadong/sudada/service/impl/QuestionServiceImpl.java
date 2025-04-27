package com.yadong.sudada.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yadong.sudada.common.ErrorCode;
import com.yadong.sudada.constant.CommonConstant;
import com.yadong.sudada.exception.ThrowUtils;
import com.yadong.sudada.manager.AIManager;
import com.yadong.sudada.mapper.QuestionMapper;
import com.yadong.sudada.model.dto.question.QuestionGenerateRequest;
import com.yadong.sudada.model.dto.question.QuestionQueryRequest;
import com.yadong.sudada.model.entity.App;
import com.yadong.sudada.model.entity.Question;
import com.yadong.sudada.model.entity.QuestionContent;
import com.yadong.sudada.model.entity.User;
import com.yadong.sudada.model.enums.AppTypeEnum;
import com.yadong.sudada.model.vo.QuestionVO;
import com.yadong.sudada.model.vo.UserVO;
import com.yadong.sudada.service.AppService;
import com.yadong.sudada.service.QuestionService;
import com.yadong.sudada.service.UserService;
import com.yadong.sudada.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
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

    @Resource
    private AIManager aiManager;

    /**
     * 校验数据
     * @param add  对创建的数据进行校验
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

    /**
     * 通过AI生成题目
     * @param questionGenerateRequest 生成题目请求
     */
    @Override
    public List<QuestionContent> generateQuestionsByAI(QuestionGenerateRequest questionGenerateRequest, HttpServletRequest request) {
        Long appId = questionGenerateRequest.getAppId();
        Integer questionNumber = questionGenerateRequest.getQuestionNumber();
        Integer optionNumber = questionGenerateRequest.getOptionNumber();
        App app = appService.getById(appId);

        // 校验appId
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        // 校验questionNumber
        ThrowUtils.throwIf(questionNumber == null || questionNumber <= 0, ErrorCode.PARAMS_ERROR, "生成的题目个数不能为0");
        // optionNumber
        ThrowUtils.throwIf(optionNumber == null || optionNumber <= 1, ErrorCode.PARAMS_ERROR, "每个题目至少需要两个选项");
        // 检查app是否存在
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);

        // 用户消息
        String userMessage = generateUserMessage(app, questionNumber, optionNumber);

        // 发起生成题目请求
        String result = aiManager.doStableSyncRequest(SYSTEM_MESSAGE, userMessage);

        // 组装结果并转为list数组返回
        int start = result.indexOf("[");
        int end = result.lastIndexOf("]");
        String questions = result.substring(start, end + 1);
        return JSONUtil.toList(questions, QuestionContent.class);
    }

    private static final String SYSTEM_MESSAGE = "你是一位严谨的出题专家，我会给你如下信息：\n" +
            "```\n" +
            "应用名称，\n" +
            "【【【应用描述】】】，\n" +
            "应用类别，\n" +
            "要生成的题目数，\n" +
            "每个题目的选项数\n" +
            "```\n" +
            "\n" +
            "请你根据上述信息，按照以下步骤来出题：\n" +
            "1. 要求：题目和选项尽可能地短，题目不要包含序号，每题的选项数以我提供的为主，题目不能重复\n" +
            "2. 严格按照下面的 json 格式输出题目和选项\n" +
            "```\n" +
            "[{\"options\":[{\"value\":\"选项内容\",\"key\":\"A\"},{\"value\":\"\",\"key\":\"B\"}],\"title\":\"题目标题\"}]\n" +
            "```\n" +
            "title 是题目，options 是选项，每个选项的 key 按照英文字母序（比如 A、B、C、D）以此类推，value 是选项内容\n" +
            "3. 检查题目是否包含序号，若包含序号则去除序号\n" +
            "4. 返回的题目列表格式必须为 JSON 数组\n";
    private static String generateUserMessage(App app, Integer questionNumber, Integer optionNumber) {
        // 获取应用的属性
        String appName = app.getAppName();
        String appDesc = app.getAppDesc();
        Integer appType = app.getAppType();
        String strAppType = Objects.requireNonNull(AppTypeEnum.getEnumByValue(appType)).getText();

        StringBuilder userMessage = new StringBuilder();
        userMessage.append(appName).append("\n");
        userMessage.append("【【【").append(appDesc).append("】】】").append("\n");
        userMessage.append(strAppType).append("\n");
        userMessage.append(questionNumber).append("\n");
        userMessage.append(optionNumber).append("\n");
        return userMessage.toString();
    }

}
