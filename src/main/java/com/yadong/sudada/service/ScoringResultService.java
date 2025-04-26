package com.yadong.sudada.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yadong.sudada.model.dto.scoringresult.ScoringResultQueryRequest;
import com.yadong.sudada.model.entity.ScoringResult;
import com.yadong.sudada.model.vo.ScoringResultVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 评分结果服务
 *
 *  
 *    
 */
public interface ScoringResultService extends IService<ScoringResult> {

    /**
     * 校验数据
     *
     * @param ScoringResult
     * @param add 对创建的数据进行校验
     */
    void validScoringResult(ScoringResult ScoringResult, boolean add);

    /**
     * 获取查询条件
     *
     * @param ScoringResultQueryRequest
     * @return
     */
    QueryWrapper<ScoringResult> getQueryWrapper(ScoringResultQueryRequest ScoringResultQueryRequest);
    
    /**
     * 获取评分结果封装
     *
     * @param ScoringResult
     * @param request
     * @return
     */
    ScoringResultVO getScoringResultVO(ScoringResult ScoringResult, HttpServletRequest request);

    /**
     * 分页获取评分结果封装
     *
     * @param ScoringResultPage
     * @param request
     * @return
     */
    Page<ScoringResultVO> getScoringResultVOPage(Page<ScoringResult> ScoringResultPage, HttpServletRequest request);
}
