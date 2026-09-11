package com.eldercare.core.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PointTransactionVO {
    private Long id;
    private String transactionType;
    private Integer amount;
    private Integer remainAmount;
    private String sourceType;
    private String remark;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}
