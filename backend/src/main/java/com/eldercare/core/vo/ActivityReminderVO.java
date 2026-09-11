package com.eldercare.core.vo;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class ActivityReminderVO {
    private Long activityId;
    private Long userId;
    private String phone;
    private String title;
    private LocalDateTime activityStart;
}
