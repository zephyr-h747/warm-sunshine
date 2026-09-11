package com.eldercare.admin.controller;

import com.eldercare.admin.service.AdminAssessmentService;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.common.Result;
import com.eldercare.core.dto.QuestionRequest;
import com.eldercare.core.dto.QuestionnaireRequest;
import com.eldercare.core.entity.Question;
import com.eldercare.core.entity.Questionnaire;
import com.eldercare.core.vo.QuestionnaireVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【管理端】评测管理接口（/api/admin/assessment）
 */
@RestController
@RequestMapping("/api/admin/assessment")
public class AdminAssessmentController {

    private final AdminAssessmentService adminAssessmentService;

    public AdminAssessmentController(AdminAssessmentService adminAssessmentService) {
        this.adminAssessmentService = adminAssessmentService;
    }

    /** 问卷列表（分页） */
    @GetMapping("/questionnaires")
    public Result<PageResult<Questionnaire>> listQuestionnaires(@RequestParam(defaultValue = "1") int pageNum,
                                                                @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(adminAssessmentService.listQuestionnaires(pageNum, pageSize));
    }

    /** 问卷详情 */
    @GetMapping("/questionnaires/{id}")
    public Result<QuestionnaireVO> getQuestionnaire(@PathVariable Long id) {
        return Result.success(adminAssessmentService.getQuestionnaire(id));
    }

    /** 创建问卷 */
    @PostMapping("/questionnaires")
    public Result<Questionnaire> createQuestionnaire(@Valid @RequestBody QuestionnaireRequest req) {
        return Result.success(adminAssessmentService.createQuestionnaire(req));
    }

    /** 编辑问卷 */
    @PutMapping("/questionnaires/{id}")
    public Result<Questionnaire> updateQuestionnaire(@PathVariable Long id, @Valid @RequestBody QuestionnaireRequest req) {
        return Result.success(adminAssessmentService.updateQuestionnaire(id, req));
    }

    /** 删除问卷 */
    @DeleteMapping("/questionnaires/{id}")
    public Result<Void> deleteQuestionnaire(@PathVariable Long id) {
        adminAssessmentService.deleteQuestionnaire(id);
        return Result.success("问卷已删除", null);
    }

    /** 发布问卷 */
    @PutMapping("/questionnaires/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        adminAssessmentService.publish(id);
        return Result.success("问卷已发布", null);
    }

    /** 下架问卷 */
    @PutMapping("/questionnaires/{id}/unpublish")
    public Result<Void> unpublish(@PathVariable Long id) {
        adminAssessmentService.unpublish(id);
        return Result.success("问卷已下架", null);
    }

    /** 添加题目 */
    @PostMapping("/questions")
    public Result<Question> createQuestion(@Valid @RequestBody QuestionRequest req) {
        return Result.success(adminAssessmentService.createQuestion(req));
    }

    /** 编辑题目 */
    @PutMapping("/questions/{id}")
    public Result<Question> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionRequest req) {
        return Result.success(adminAssessmentService.updateQuestion(id, req));
    }

    /** 删除题目 */
    @DeleteMapping("/questions/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        adminAssessmentService.deleteQuestion(id);
        return Result.success("题目已删除", null);
    }
}
