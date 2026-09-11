package com.eldercare.api.controller;

import com.eldercare.api.service.AssessmentService;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.common.Result;
import com.eldercare.core.dto.AssessmentSubmitRequest;
import com.eldercare.core.entity.Questionnaire;
import com.eldercare.core.util.SecurityUtils;
import com.eldercare.core.vo.AssessmentResultVO;
import com.eldercare.core.vo.QuestionnaireVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【用户端】健康评测接口（/api/member/assessment）
 */
@RestController
@RequestMapping("/api/member/assessment")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    /** 已发布问卷列表 */
    @GetMapping("/list")
    public Result<PageResult<Questionnaire>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(assessmentService.listQuestionnaires(pageNum, pageSize));
    }

    /** 问卷详情（含题目） */
    @GetMapping("/{id}")
    public Result<QuestionnaireVO> get(@PathVariable Long id) {
        return Result.success(assessmentService.getQuestionnaire(id));
    }

    /** 提交评测（AI 评分 + 积分奖励） */
    @PostMapping("/submit")
    public Result<AssessmentResultVO> submit(@Valid @RequestBody AssessmentSubmitRequest req) {
        return Result.success(assessmentService.submit(SecurityUtils.getCurrentUserId(), req));
    }

    /** 评测历史 */
    @GetMapping("/history")
    public Result<PageResult<AssessmentResultVO>> history(@RequestParam(defaultValue = "1") int pageNum,
                                                         @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(assessmentService.history(SecurityUtils.getCurrentUserId(), pageNum, pageSize));
    }

    /** 评测详情 */
    @GetMapping("/{id}/detail")
    public Result<AssessmentResultVO> detail(@PathVariable Long id) {
        return Result.success(assessmentService.detail(SecurityUtils.getCurrentUserId(), id));
    }
}
