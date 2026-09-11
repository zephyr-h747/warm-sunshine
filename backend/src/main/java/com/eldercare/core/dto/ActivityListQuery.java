package com.eldercare.core.dto;

import lombok.Data;

/**
 * 活动列表查询参数
 */
@Data
public class ActivityListQuery {

    /** 状态筛选：REGISTRATING/IN_PROGRESS/DRAFT/ENDED/CANCELED，空查全部 */
    private String status;
    /** 页码（从 1 开始） */
    private int pageNum = 1;
    /** 每页条数 */
    private int pageSize = 10;


}
