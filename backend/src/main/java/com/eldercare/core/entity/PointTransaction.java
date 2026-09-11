package com.eldercare.core.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PointTransaction {
    private Long id;
    private Long userId;
    private String transactionType;
    private Integer amount;
    private Integer balanceAfter;
    private Integer remainAmount;
    private Long batchTxId;
    private String sourceType;
    private String sourceId;
    private String remark;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private Integer deleted;
}
