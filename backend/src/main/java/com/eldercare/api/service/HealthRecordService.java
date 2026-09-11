package com.eldercare.api.service;

import com.eldercare.core.common.PageResult;
import com.eldercare.core.dto.HealthRecordRequest;
import com.eldercare.core.entity.HealthGuidance;
import com.eldercare.core.entity.HealthRecord;
import com.eldercare.core.entity.Message;
import com.eldercare.core.entity.User;
import com.eldercare.core.exception.AccessDeniedException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.HealthGuidanceMapper;
import com.eldercare.core.mapper.HealthRecordMapper;
import com.eldercare.core.mapper.MessageMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.vo.HealthTrendVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 【用户端】健康记录服务：
 * - 录入时根据身高自动计算 BMI
 * - 指标超标时生成健康指导（DATA_SUMMARY）+ 站内消息（HEALTH_REMINDER），同指标当日去重
 * - 历史分页、近 6 个月趋势、详情（校验归属）
 */
@Service
public class HealthRecordService {

    private static final Logger log = LoggerFactory.getLogger(HealthRecordService.class);

    /** 健康阈值（设计文档 5.2 节） */
    private static final int SYSTOLIC_LOW = 90;
    private static final int SYSTOLIC_HIGH = 140;
    private static final int DIASTOLIC_LOW = 60;
    private static final int DIASTOLIC_HIGH = 90;
    private static final BigDecimal BLOOD_SUGAR_LOW = new BigDecimal("3.9");
    private static final BigDecimal BLOOD_SUGAR_HIGH = new BigDecimal("6.1");
    private static final int HEART_RATE_LOW = 60;
    private static final int HEART_RATE_HIGH = 100;
    private static final BigDecimal BMI_LOW = new BigDecimal("18.5");
    private static final BigDecimal BMI_HIGH = new BigDecimal("24");

    private static final String GUIDANCE_TYPE = "DATA_SUMMARY";
    private static final String MESSAGE_TYPE = "HEALTH_REMINDER";

    /** 趋势指标顺序 */
    private static final String[] INDICATORS = {
            "SYSTOLIC", "DIASTOLIC", "BLOOD_SUGAR", "HEART_RATE", "WEIGHT", "BMI"
    };

    private final HealthRecordMapper healthRecordMapper;
    private final HealthGuidanceMapper healthGuidanceMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final AiService aiService;

    public HealthRecordService(HealthRecordMapper healthRecordMapper, HealthGuidanceMapper healthGuidanceMapper,
                               MessageMapper messageMapper, UserMapper userMapper, AiService aiService) {
        this.healthRecordMapper = healthRecordMapper;
        this.healthGuidanceMapper = healthGuidanceMapper;
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
        this.aiService = aiService;
    }

    /** 录入健康记录，自动计算 BMI 并推送超标提醒 */
    public HealthRecord create(Long userId, HealthRecordRequest req) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }

        HealthRecord record = new HealthRecord();
        record.setUserId(userId);
        record.setSystolic(req.getSystolic());
        record.setDiastolic(req.getDiastolic());
        record.setBloodSugar(req.getBloodSugar());
        record.setHeartRate(req.getHeartRate());
        record.setWeight(req.getWeight());
        record.setMemo(req.getMemo());
        record.setRecordedAt(LocalDateTime.now());

        // BMI = weight / (height/100)^2，保留 1 位小数
        if (req.getWeight() != null && user.getHeight() != null) {
            BigDecimal heightM = user.getHeight().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal bmi = req.getWeight().divide(heightM.multiply(heightM), 1, RoundingMode.HALF_UP);
            record.setBmi(bmi);
        }
        healthRecordMapper.insert(record);

        pushHealthAlerts(userId, record);
        return record;
    }

    /** 历史记录（分页） */
    public PageResult<HealthRecord> list(Long userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<HealthRecord> records = healthRecordMapper.selectByUserId(userId);
        return PageResult.of(new PageInfo<>(records));
    }

    /** 近 6 个月趋势：按指标分组计算 avg/max/min 与逐日数据 */
    public List<HealthTrendVO> trend(Long userId) {
        List<HealthRecord> records = healthRecordMapper.selectRecent6Months(userId);
        Map<String, List<BigDecimal>> values = new LinkedHashMap<>();
        Map<String, List<HealthTrendVO.TrendRecord>> points = new LinkedHashMap<>();

        for (HealthRecord r : records) {
            LocalDateTime d = r.getRecordedAt();
            accumulate(values, points, "SYSTOLIC", d, r.getSystolic() == null ? null : BigDecimal.valueOf(r.getSystolic()));
            accumulate(values, points, "DIASTOLIC", d, r.getDiastolic() == null ? null : BigDecimal.valueOf(r.getDiastolic()));
            accumulate(values, points, "BLOOD_SUGAR", d, r.getBloodSugar());
            accumulate(values, points, "HEART_RATE", d, r.getHeartRate() == null ? null : BigDecimal.valueOf(r.getHeartRate()));
            accumulate(values, points, "WEIGHT", d, r.getWeight());
            accumulate(values, points, "BMI", d, r.getBmi());
        }

        List<HealthTrendVO> result = new ArrayList<>();
        for (String indicator : INDICATORS) {
            HealthTrendVO vo = new HealthTrendVO();
            vo.setIndicator(indicator);
            vo.setIndicatorName(indicatorName(indicator));
            List<BigDecimal> vals = values.getOrDefault(indicator, List.of());
            if (vals.isEmpty()) {
                vo.setRecords(List.of());
            } else {
                BigDecimal sum = vals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                vo.setAvgValue(sum.divide(BigDecimal.valueOf(vals.size()), 1, RoundingMode.HALF_UP));
                vo.setMaxValue(vals.stream().max(BigDecimal::compareTo).orElse(null));
                vo.setMinValue(vals.stream().min(BigDecimal::compareTo).orElse(null));
                vo.setRecords(points.getOrDefault(indicator, List.of()));
            }
            result.add(vo);
        }
        return result;
    }

    /** 详情（校验归属） */
    public HealthRecord detail(Long userId, Long recordId) {
        HealthRecord record = healthRecordMapper.selectById(recordId);
        if (record == null) {
            throw new ResourceNotFoundException("健康记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new AccessDeniedException("无权查看他人的健康记录");
        }
        return record;
    }

    // ==================== 私有方法 ====================

    private void accumulate(Map<String, List<BigDecimal>> values, Map<String, List<HealthTrendVO.TrendRecord>> points,
                            String indicator, LocalDateTime date, BigDecimal value) {
        if (value == null || date == null) {
            return;
        }
        values.computeIfAbsent(indicator, k -> new ArrayList<>()).add(value);
        points.computeIfAbsent(indicator, k -> new ArrayList<>())
                .add(new HealthTrendVO.TrendRecord(date.toLocalDate().toString(), value));
    }

    /** 指标超标健康提醒：生成指导 + 消息，同指标当日去重 */
    private void pushHealthAlerts(Long userId, HealthRecord record) {
        // 按指标 label 聚合（血压 = 收缩压/舒张压合并为一条）
        Map<String, AlertInfo> alertMap = new LinkedHashMap<>();

        Integer systolic = record.getSystolic();
        Integer diastolic = record.getDiastolic();
        List<String> bpDetails = new ArrayList<>();
        if (systolic != null && (systolic < SYSTOLIC_LOW || systolic >= SYSTOLIC_HIGH)) {
            bpDetails.add("收缩压 " + systolic + " mmHg " + (systolic < SYSTOLIC_LOW ? "偏低" : "偏高"));
        }
        if (diastolic != null && (diastolic < DIASTOLIC_LOW || diastolic >= DIASTOLIC_HIGH)) {
            bpDetails.add("舒张压 " + diastolic + " mmHg " + (diastolic < DIASTOLIC_LOW ? "偏低" : "偏高"));
        }
        if (!bpDetails.isEmpty()) {
            alertMap.put("血压", new AlertInfo("血压", "血压异常", String.join("，", bpDetails)));
        }

        if (record.getBloodSugar() != null) {
            BigDecimal bs = record.getBloodSugar();
            if (bs.compareTo(BLOOD_SUGAR_LOW) < 0 || bs.compareTo(BLOOD_SUGAR_HIGH) > 0) {
                alertMap.put("血糖", new AlertInfo("血糖", "血糖异常",
                        "血糖 " + bs + " mmol/L " + (bs.compareTo(BLOOD_SUGAR_LOW) < 0 ? "偏低" : "偏高")));
            }
        }
        Integer heartRate = record.getHeartRate();
        if (heartRate != null && (heartRate < HEART_RATE_LOW || heartRate > HEART_RATE_HIGH)) {
            alertMap.put("心率", new AlertInfo("心率", "心率异常",
                    "心率 " + heartRate + " 次/分 " + (heartRate < HEART_RATE_LOW ? "偏低" : "偏高")));
        }
        if (record.getBmi() != null) {
            BigDecimal bmi = record.getBmi();
            if (bmi.compareTo(BMI_LOW) < 0 || bmi.compareTo(BMI_HIGH) >= 0) {
                alertMap.put("体重", new AlertInfo("体重", "体重异常",
                        "BMI " + bmi + (bmi.compareTo(BMI_LOW) < 0 ? "偏低" : "偏高")));
            }
        }
        List<AlertInfo> alerts = new ArrayList<>(alertMap.values());
        if (alerts.isEmpty()) {
            return;
        }

        // 当日已推送的指标（去重）
        List<HealthGuidance> todays = healthGuidanceMapper.selectTodayByUserAndType(userId, GUIDANCE_TYPE);
        Set<String> pushedLabels = todays.stream()
                .map(g -> extractLabel(g.getContent()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // AI 整体建议（未配置 Key 或失败时返回 null）
        boolean hasNew = alerts.stream().anyMatch(a -> !pushedLabels.contains(a.label()));
        String aiAdvice = hasNew ? aiService.chat(
                "你是一位社区健康顾问，请用简洁、通俗、可执行的语言给老年人健康建议。",
                buildAlertPrompt(alerts)) : null;

        for (AlertInfo alert : alerts) {
            if (pushedLabels.contains(alert.label())) {
                log.debug("今日已推送过【{}】提醒，跳过", alert.label());
                continue;
            }
            String baseContent = "【" + alert.label() + "】" + alert.detail() + "。建议：" + ruleAdvice(alert.label());

            HealthGuidance guidance = new HealthGuidance();
            guidance.setUserId(userId);
            guidance.setType(GUIDANCE_TYPE);
            guidance.setContent(baseContent);
            healthGuidanceMapper.insert(guidance);

            Message message = new Message();
            message.setUserId(userId);
            message.setTitle("健康提醒：" + alert.title());
            message.setContent(aiAdvice != null ? baseContent + "\n\nAI 建议：" + aiAdvice : baseContent);
            message.setType(MESSAGE_TYPE);
            messageMapper.insert(message);

            log.info("健康提醒已推送: userId={}, label={}", userId, alert.label());
        }
    }

    private String buildAlertPrompt(List<AlertInfo> alerts) {
        StringBuilder sb = new StringBuilder("以下是用户本次录入的异常健康指标：\n");
        for (AlertInfo a : alerts) {
            sb.append("- ").append(a.detail()).append("\n");
        }
        sb.append("请给出简洁、通俗、可执行的整体健康建议（不超过 200 字）。");
        return sb.toString();
    }

    private String ruleAdvice(String label) {
        return switch (label) {
            case "血压" -> "注意低盐饮食、规律作息，每日定时监测血压，如持续异常或伴头晕头痛请及时就医。";
            case "血糖" -> "控制主食与高糖食物摄入、规律进餐，如有糖尿病史请按医嘱监测与用药。";
            case "心率" -> "规律作息、避免过度劳累与情绪激动，如伴胸闷心悸请及时就医。";
            case "体重" -> "均衡饮食、适度运动，将 BMI 控制在 18.5-24 的健康范围。";
            default -> "请保持健康生活方式，定期复查。";
        };
    }

    /** 从指导内容中解析指标标签，如 【血压】xxx → 血压 */
    private String extractLabel(String content) {
        if (content == null) {
            return null;
        }
        int start = content.indexOf('【');
        int end = content.indexOf('】');
        if (start >= 0 && end > start) {
            return content.substring(start + 1, end);
        }
        return null;
    }

    private String indicatorName(String indicator) {
        return switch (indicator) {
            case "SYSTOLIC" -> "收缩压";
            case "DIASTOLIC" -> "舒张压";
            case "BLOOD_SUGAR" -> "血糖";
            case "HEART_RATE" -> "心率";
            case "WEIGHT" -> "体重";
            case "BMI" -> "BMI";
            default -> indicator;
        };
    }

    private record AlertInfo(String label, String title, String detail) {
    }
}
