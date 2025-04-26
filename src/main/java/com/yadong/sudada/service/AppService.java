package com.yadong.sudada.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yadong.sudada.model.dto.app.AppQueryRequest;
import com.yadong.sudada.model.dto.app.ReviewRequest;
import com.yadong.sudada.model.entity.App;
import com.yadong.sudada.model.vo.AppVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 应用服务
 */
public interface AppService extends IService<App> {

    /**
     * 校验数据
     * @param add 是否对创建的数据进行校验
     */
    void validApp(App app, boolean add);

    /**
     * 获取查询条件
     */
    QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest);
    
    /**
     * 获取应用封装
     */
    AppVO getAppVO(App app, HttpServletRequest request);

    /**
     * 分页获取应用封装
     */
    Page<AppVO> getAppVOPage(Page<App> appPage, HttpServletRequest request);

    /**
     * 审核应用请求
     */
    boolean submitReview(ReviewRequest reviewRequest, HttpServletRequest request);
}
