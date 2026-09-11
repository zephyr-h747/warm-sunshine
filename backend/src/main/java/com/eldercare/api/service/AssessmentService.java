package com.eldercare.api.service;

import com.eldercare.core.common.PageResult;
import com.eldercare.core.dto.AssessmentSubmitRequest;
import com.eldercare.core.entity.AssessmentResult;
import com.eldercare.core.entity.Question;
import com.eldercare.core.entity.Questionnaire;
import com.eldercare.core.exception.AccessDeniedException;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.AssessmentResultMapper;
import com.eldercare.core.mapper.QuestionMapper;
import com.eldercare.core.mapper.QuestionnaireMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.vo.AssessmentResultVO;
import com.eldercare.core.vo.QuestionVO;
import com.eldercare.core.vo.QuestionnaireVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 【用户端】健康评测服务：
 * - 问卷列表（已发布）、问卷详情（含题目）
 * - 提交评测：答案快照 + AI 评分与建议（失败降级）+ 完成奖励积分 +20
 * - 评测历史、详情（校验归属）
 */
@Service
public class AssessmentService {

    private static final Logger log = LoggerFactory.getLogger(AssessmentService.class);

    /** 完成一次评测奖励积分 */
    private static final int ASSESSMENT_BONUS_POINTS = 20;

    /** AI 未配置或解析失败时的兜底评分与建议 */
    private static final int FALLBACK_SCORE = 80;
    private static final String FALLBACK_SUGGESTION = "已完成健康评测。建议保持规律作息、均衡饮食、适量运动，并定期复查。";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final QuestionnaireMapper questionnaireMapper;
    private final QuestionMapper questionMapper;
    private final AssessmentResultMapper assessmentResultMapper;
    private final UserMapper userMapper;
    private final AiService aiService;
    private final PointService pointService;

    public AssessmentService(QuestionnaireMapper questionnaireMapper, QuestionMapper questionMapper,
                             AssessmentResultMapper assessmentResultMapper, UserMapper userMapper,
                             AiService aiService, PointService pointService) {
        this.questionnaireMapper = questionnaireMapper;
        this.questionMapper = questionMapper;
        this.assessmentResultMapper = assessmentResultMapper;
        this.userMapper = userMapper;
        this.aiService = aiService;
        this.pointService = pointService;
    }

    /** 已发布问卷列表（分页） */
    public PageResult<Questionnaire> listQuestionnaires(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Questionnaire> list = questionnaireMapper.selectPublishedList();
        return PageResult.of(new PageInfo<>(list));
    }

    /** 问卷详情（含题目，仅已发布可见） */
    public QuestionnaireVO getQuestionnaire(Long id) {
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new ResourceNotFoundException("问卷不存在");
        }
        if (!"PUBLISHED".equals(questionnaire.getStatus())) {
            throw new BusinessException(400, "问卷未发布");
        }
        List<Question> questions = questionMapper.selectByQuestionnaireId(id);
        return toQuestionnaireVO(questionnaire, questions);
    }

    /** 提交评测：AI 评分 + 奖励积分 */
    @Transactional
    public AssessmentResultVO submit(Long userId, AssessmentSubmitRequest req) {
        Questionnaire questionnaire = questionnaireMapper.selectById(req.getQuestionnaireId());
        if (questionnaire == null) {
            throw new ResourceNotFoundException("问卷不存在");
        }
        if (!"PUBLISHED".equals(questionnaire.getStatus())) {
            throw new BusinessException(400, "问卷未发布");
        }
        List<Question> questions = questionMapper.selectByQuestionnaireId(req.getQuestionnaireId());

        ScoreResult score = aiScore(questionnaire, questions, req.getAnswers());

        AssessmentResult result = new AssessmentResult();
        result.setUserId(userId);
        result.setQuestionnaireId(req.getQuestionnaireId());
        result.setAnswers(toJson(req.getAnswers()));
        result.setAiScore(score.score());
        result.setAiSuggestion(score.suggestion());
        assessmentResultMapper.insert(result);

        // 完成评测奖励积分
        pointService.grant(userId, ASSESSMENT_BONUS_POINTS, "ASSESSMENT", String.valueOf(result.getId()), "完成健康评估奖励");
        log.info("评测提交成功: userId={}, questionnaireId={}, score={}", userId, req.getQuestionnaireId(), score.score());

        return toResultVO(result, questionnaire.getTitle());
    }

    /** 评测历史（分页） */
    public PageResult<AssessmentResultVO> history(Long userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<AssessmentResult> list = assessmentResultMapper.selectByUserId(userId);
        PageInfo<AssessmentResult> pageInfo = new PageInfo<>(list);
        List<AssessmentResultVO> vos = list.stream().map(r -> toResultVO(r, null)).toList();
        return new PageResult<>(vos, pageInfo.getTotal(), pageInfo.getPageNum(),
                pageInfo.getPageSize(), pageInfo.getPages());
    }

    /** 评测详情（校验归属，查看他人返回 403） */
    public AssessmentResultVO detail(Long userId, Long id) {
        AssessmentResult result = assessmentResultMapper.selectById(id);
        if (result == null) {
            throw new ResourceNotFoundException("评测记录不存在");
        }
        if (!result.getUserId().equals(userId)) {
            throw new AccessDeniedException("无权查看他人的评测记录");
        }
        Questionnaire questionnaire = questionnaireMapper.selectById(result.getQuestionnaireId());
        return toResultVO(result, questionnaire != null ? questionnaire.getTitle() : null);
    }

    // ==================== 私有方法 ====================

    /** AI 评分：失败时降级到兜底分数与建议 */
    private ScoreResult aiScore(Questionnaire questionnaire, List<Question> questions, List<AssessmentSubmitRequest.AnswerItem> answers) {
        int ruleScore = calculateRuleScore(questionnaire, questions, answers);
        String systemPrompt = "你是一位专业的健康评估专家。请根据用户的健康问卷答案，给出 0-100 的整数评分（分数越高越健康），"
                + "并给出简洁、个性化的健康建议。必须只返回 JSON 格式：{\"score\":85,\"suggestion\":\"建议内容\"}";
        String userPrompt = buildAssessmentPrompt(questionnaire, questions, answers);
        String raw = aiService.chat(systemPrompt, userPrompt);
        if (raw == null) {
            return new ScoreResult(ruleScore, FALLBACK_SUGGESTION);
        }
        return parseScore(raw);
    }

    /** 从 AI 返回文本解析评分与建议，解析失败回退兜底值 */
    private ScoreResult parseScore(String raw) {
        Integer score = null;
        String suggestion = null;

        Matcher scoreMatcher = Pattern.compile("\"?score\"?\\s*[:：]\\s*(\\d{1,3})").matcher(raw);
        if (scoreMatcher.find()) {
            score = Math.max(0, Math.min(100, Integer.parseInt(scoreMatcher.group(1))));
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(raw);
            if (node.has("suggestion")) {
                suggestion = node.get("suggestion").asText();
            }
        } catch (Exception ignored) {
            Matcher sugMatcher = Pattern.compile("\"?suggestion\"?\\s*[:：]\\s*[\"']([^\"']+)[\"']").matcher(raw);
            if (sugMatcher.find()) {
                suggestion = sugMatcher.group(1);
            }
        }
        if (score == null) {
            score = FALLBACK_SCORE;
        }
        if (!StringUtils.hasText(suggestion)) {
            suggestion = FALLBACK_SUGGESTION;
        }
        return new ScoreResult(score, suggestion.trim());
    }

    private String buildAssessmentPrompt(Questionnaire questionnaire, List<Question> questions,
                                         List<AssessmentSubmitRequest.AnswerItem> answers) {
        Map<Long, Object> answerMap = answers.stream()
                .collect(Collectors.toMap(AssessmentSubmitRequest.AnswerItem::getQid,
                        AssessmentSubmitRequest.AnswerItem::getValue, (a, b) -> b));
        StringBuilder sb = new StringBuilder();
        sb.append("问卷《").append(questionnaire.getTitle()).append("》用户答案如下：\n");
        sb.append("规则分（百分制）：").append(calculateRuleScore(questionnaire, questions, answers)).append("。请结合非计分题和文本题进行综合判断。\n");
        for (Question question : questions) {
            sb.append(question.getSortOrder() == null ? "" : question.getSortOrder()).append(". ")
                    .append(question.getContent()).append(" 答案：");
            Object value = answerMap.get(question.getId());
            sb.append(value == null ? "（未作答）" : value).append("\n");
        }
        return sb.toString();
    }

    /** 按计分题选项 score 汇总为百分制；选项兼容 {text,score,meaning} 与历史 "文本|分值" 格式。 */
    private int calculateRuleScore(Questionnaire questionnaire, List<Question> questions, List<AssessmentSubmitRequest.AnswerItem> answers) {
        Map<Long, Object> answersByQuestion = answers.stream().collect(Collectors.toMap(AssessmentSubmitRequest.AnswerItem::getQid, AssessmentSubmitRequest.AnswerItem::getValue, (a, b) -> b));
        BigDecimal earned = BigDecimal.ZERO, full = BigDecimal.ZERO;
        for (Question question : questions) {
            if ("NON_SCORE".equalsIgnoreCase(question.getScoreMode())) continue;
            int max = question.getMaxScore() == null ? 0 : question.getMaxScore();
            if (max <= 0) continue;
            full = full.add(BigDecimal.valueOf(max));
            Object answer = answersByQuestion.get(question.getId());
            for (String choice : answerValues(answer)) earned = earned.add(optionScore(question.getOptions(), choice));
        }
        if (full.signum() == 0) return FALLBACK_SCORE;
        return earned.multiply(BigDecimal.valueOf(100)).divide(full, 0, java.math.RoundingMode.HALF_UP).intValueExact();
    }

    private List<String> answerValues(Object value) {
        if (value instanceof List<?> list) return list.stream().map(String::valueOf).toList();
        return value == null ? List.of() : List.of(String.valueOf(value));
    }

    private BigDecimal optionScore(String optionsJson, String answer) {
        if (!StringUtils.hasText(optionsJson)) return BigDecimal.ZERO;
        try {
            JsonNode options = OBJECT_MAPPER.readTree(optionsJson);
            for (JsonNode option : options) {
                if (option.isObject() && answer.equals(option.path("text").asText())) return option.path("score").decimalValue();
                if (option.isTextual()) {
                    String raw = option.asText(); String[] parts = raw.split("\\|", 3);
                    if (answer.equals(parts[0]) && parts.length > 1) return new BigDecimal(parts[1]);
                }
            }
        } catch (Exception ignored) { }
        return BigDecimal.ZERO;
    }

    private String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new BusinessException(500, "答案序列化失败");
        }
    }

    private QuestionnaireVO toQuestionnaireVO(Questionnaire q, List<Question> questions) {
        QuestionnaireVO vo = new QuestionnaireVO();
        vo.setId(q.getId());
        vo.setTitle(q.getTitle());
        vo.setDescription(q.getDescription());
        vo.setStatus(q.getStatus());
        vo.setTotalScore(q.getTotalScore()); vo.setPassScore(q.getPassScore()); vo.setGradeRules(q.getGradeRules());
        List<QuestionVO> questionVOs = new ArrayList<>();
        for (Question question : questions) {
            QuestionVO item = new QuestionVO();
            item.setId(question.getId());
            item.setContent(question.getContent());
            item.setType(question.getType());
            item.setSortOrder(question.getSortOrder());
            item.setOptions(parseOptions(question.getOptions()));
            item.setScoreMode(question.getScoreMode()); item.setMaxScore(question.getMaxScore());
            questionVOs.add(item);
        }
        vo.setQuestions(questionVOs);
        return vo;
    }

    /** 解析题目选项 JSON，失败返回空列表 */
    private List<String> parseOptions(String optionsJson) {
        if (!StringUtils.hasText(optionsJson)) {
            return List.of();
        }
        try {
            return OBJECT_MAPPER.readValue(optionsJson, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private AssessmentResultVO toResultVO(AssessmentResult r, String questionnaireTitle) {
        AssessmentResultVO vo = new AssessmentResultVO();
        vo.setId(r.getId());
        vo.setQuestionnaireId(r.getQuestionnaireId());
        vo.setQuestionnaireTitle(questionnaireTitle);
        vo.setAiScore(r.getAiScore());
        vo.setAiSuggestion(r.getAiSuggestion());
        vo.setCreateTime(r.getCreateTime());
        return vo;
    }

    private record ScoreResult(Integer score, String suggestion) {
    }
}
