package com.eldercare.admin.service;

import com.eldercare.core.common.PageResult;
import com.eldercare.core.dto.QuestionRequest;
import com.eldercare.core.dto.QuestionnaireRequest;
import com.eldercare.core.entity.Question;
import com.eldercare.core.entity.Questionnaire;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.QuestionMapper;
import com.eldercare.core.mapper.QuestionnaireMapper;
import com.eldercare.core.vo.QuestionnaireVO;
import com.eldercare.core.vo.QuestionVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 【管理端】评测管理服务：问卷 CRUD + 发布/下架，题目 CRUD + 排序
 */
@Service
public class AdminAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(AdminAssessmentService.class);

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PUBLISHED = "PUBLISHED";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final QuestionnaireMapper questionnaireMapper;
    private final QuestionMapper questionMapper;

    public AdminAssessmentService(QuestionnaireMapper questionnaireMapper, QuestionMapper questionMapper) {
        this.questionnaireMapper = questionnaireMapper;
        this.questionMapper = questionMapper;
    }

    // ==================== 问卷管理 ====================

    /** 问卷列表（分页） */
    public PageResult<Questionnaire> listQuestionnaires(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Questionnaire> list = questionnaireMapper.selectList();
        return PageResult.of(new PageInfo<>(list));
    }

    /** 问卷详情（含题目列表，供管理端题目管理页使用） */
    public QuestionnaireVO getQuestionnaire(Long id) {
        Questionnaire questionnaire = requireQuestionnaire(id);
        List<Question> questions = questionMapper.selectByQuestionnaireId(id);
        QuestionnaireVO vo = new QuestionnaireVO();
        vo.setId(questionnaire.getId());
        vo.setTitle(questionnaire.getTitle());
        vo.setDescription(questionnaire.getDescription());
        vo.setStatus(questionnaire.getStatus());
        List<QuestionVO> questionVOs = new ArrayList<>();
        for (Question question : questions) {
            QuestionVO item = new QuestionVO();
            item.setId(question.getId());
            item.setContent(question.getContent());
            item.setType(question.getType());
            item.setSortOrder(question.getSortOrder());
            item.setOptions(parseOptions(question.getOptions()));
            questionVOs.add(item);
        }
        vo.setQuestions(questionVOs);
        return vo;
    }

    /** 创建问卷：初始状态 DRAFT（草稿） */
    public Questionnaire createQuestionnaire(QuestionnaireRequest req) {
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setTitle(req.getTitle());
        questionnaire.setDescription(req.getDescription());
        questionnaire.setTotalScore(req.getTotalScore()); questionnaire.setPassScore(req.getPassScore()); questionnaire.setGradeRules(req.getGradeRules());
        questionnaire.setStatus(STATUS_DRAFT);
        questionnaireMapper.insert(questionnaire);
        log.info("管理端创建问卷: id={}, title={}", questionnaire.getId(), questionnaire.getTitle());
        return questionnaireMapper.selectById(questionnaire.getId());
    }

    /** 更新问卷标题/描述 */
    public Questionnaire updateQuestionnaire(Long id, QuestionnaireRequest req) {
        Questionnaire questionnaire = requireQuestionnaire(id);
        questionnaire.setTitle(req.getTitle());
        questionnaire.setDescription(req.getDescription());
        questionnaire.setTotalScore(req.getTotalScore()); questionnaire.setPassScore(req.getPassScore()); questionnaire.setGradeRules(req.getGradeRules());
        questionnaireMapper.update(questionnaire);
        return questionnaireMapper.selectById(id);
    }

    /** 删除问卷（逻辑删除，含其下题目） */
    @Transactional
    public void deleteQuestionnaire(Long id) {
        requireQuestionnaire(id);
        questionnaireMapper.delete(id);
        questionMapper.deleteByQuestionnaireId(id);
        log.info("管理端删除问卷: id={}", id);
    }

    /** 发布问卷 */
    public void publish(Long id) {
        requireQuestionnaire(id);
        questionnaireMapper.updateStatus(id, STATUS_PUBLISHED);
        log.info("管理端发布问卷: id={}", id);
    }

    /** 下架问卷（恢复草稿状态） */
    public void unpublish(Long id) {
        requireQuestionnaire(id);
        questionnaireMapper.updateStatus(id, STATUS_DRAFT);
        log.info("管理端下架问卷: id={}", id);
    }

    // ==================== 题目管理 ====================

    /** 新增题目：所属问卷必须存在 */
    public Question createQuestion(QuestionRequest req) {
        requireQuestionnaire(req.getQuestionnaireId());
        Question question = new Question();
        question.setQuestionnaireId(req.getQuestionnaireId());
        question.setContent(req.getContent());
        question.setType(req.getType());
        question.setOptions(toOptionsJson(req.getOptions()));
        question.setScoreMode(req.getScoreMode() == null ? "SCORE" : req.getScoreMode()); question.setMaxScore(req.getMaxScore() == null ? 0 : req.getMaxScore());
        question.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        questionMapper.insert(question);
        return questionMapper.selectById(question.getId());
    }

    /** 更新题目：仅覆盖传入字段 */
    public Question updateQuestion(Long id, QuestionRequest req) {
        Question question = requireQuestion(id);
        if (req.getContent() != null) {
            question.setContent(req.getContent());
        }
        if (req.getType() != null) {
            question.setType(req.getType());
        }
        if (req.getOptions() != null) {
            question.setOptions(toOptionsJson(req.getOptions()));
        }
        if (req.getSortOrder() != null) {
            question.setSortOrder(req.getSortOrder());
        }
        if (req.getScoreMode() != null) question.setScoreMode(req.getScoreMode());
        if (req.getMaxScore() != null) question.setMaxScore(req.getMaxScore());
        questionMapper.update(question);
        return questionMapper.selectById(id);
    }

    /** 删除题目 */
    public void deleteQuestion(Long id) {
        requireQuestion(id);
        questionMapper.delete(id);
        log.info("管理端删除题目: id={}", id);
    }

    /** 题目排序 */
    public void updateSortOrder(Long id, Integer sortOrder) {
        requireQuestion(id);
        questionMapper.updateSortOrder(id, sortOrder);
    }

    // ==================== 私有方法 ====================

    private Questionnaire requireQuestionnaire(Long id) {
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new ResourceNotFoundException("问卷不存在");
        }
        return questionnaire;
    }

    private Question requireQuestion(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new ResourceNotFoundException("题目不存在");
        }
        return question;
    }

    /** 解析题目选项 JSON，失败返回空列表 */
    private List<String> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isBlank()) {
            return List.of();
        }
        try {
            return OBJECT_MAPPER.readValue(optionsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("题目选项解析失败: {}", optionsJson);
            return List.of();
        }
    }

    private String toOptionsJson(List<String> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(options);
        } catch (Exception e) {
            throw new BusinessException(400, "选项格式错误");
        }
    }
}
