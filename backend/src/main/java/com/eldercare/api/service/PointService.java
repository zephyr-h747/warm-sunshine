package com.eldercare.api.service;

import com.eldercare.core.common.PageResult;
import com.eldercare.core.entity.PointTransaction;
import com.eldercare.core.mapper.PointTransactionMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.vo.PointTransactionVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PointService {
    private final PointTransactionMapper transactionMapper;
    private final UserMapper userMapper;

    public PointService(PointTransactionMapper transactionMapper, UserMapper userMapper) {
        this.transactionMapper = transactionMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public void grant(Long userId, int amount, String sourceType, String sourceId, String remark) {
        if (amount <= 0) return;
        userMapper.updatePoints(userId, amount);
        PointTransaction t = base(userId, "EARN", amount, sourceType, sourceId, remark);
        transactionMapper.insert(t);
    }

    @Transactional
    public void consume(Long userId, int amount, String sourceType, String sourceId, String remark) {
        if (amount <= 0) return;
        int left = amount;
        List<PointTransaction> batches = transactionMapper.selectByUserId(userId, "EARN");
        for (PointTransaction batch : batches) {
            if (left == 0) break;
            if (batch.getRemainAmount() == null || batch.getRemainAmount() <= 0) continue;
            int used = Math.min(left, batch.getRemainAmount());
            if (transactionMapper.reduceRemain(batch.getId(), used) == 1) {
                left -= used;
                PointTransaction consume = base(userId, "CONSUME", -used, sourceType, sourceId, remark);
                consume.setBatchTxId(batch.getId());
                consume.setRemainAmount(0);
                transactionMapper.insert(consume);
            }
        }
        if (left > 0 || userMapper.deductPoints(userId, amount) == 0) {
            throw new IllegalStateException("积分不足或积分批次已发生并发变化");
        }
    }

    @Transactional
    public void refund(Long userId, int amount, String sourceType, String sourceId, String remark) {
        grant(userId, amount, sourceType, sourceId, remark);
    }

    @Transactional
    public void refundAppointment(Long userId, Long appointmentId) {
        List<PointTransaction> consumptions = transactionMapper.selectActiveConsumptions(userId, "APPOINTMENT", String.valueOf(appointmentId));
        if (consumptions.isEmpty()) throw new IllegalStateException("未找到预约对应的积分消费流水");
        int restored = 0;
        for (PointTransaction consumption : consumptions) {
            int amount = Math.abs(consumption.getAmount());
            if (consumption.getBatchTxId() != null && transactionMapper.logicDelete(consumption.getId()) == 1) {
                transactionMapper.restoreRemain(consumption.getBatchTxId(), amount); restored += amount;
            }
        }
        if (restored > 0) userMapper.updatePoints(userId, restored);
    }

    public PageResult<PointTransactionVO> list(Long userId, String type, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<PointTransaction> list = transactionMapper.selectByUserId(userId, type);
        PageInfo<PointTransaction> page = new PageInfo<>(list);
        return new PageResult<>(list.stream().map(this::toVO).toList(), page.getTotal(), page.getPageNum(), page.getPageSize(), page.getPages());
    }

    @Transactional
    public int expireExpired() {
        int count = 0;
        for (PointTransaction batch : transactionMapper.selectExpirable(LocalDateTime.now())) {
            int amount = batch.getRemainAmount() == null ? 0 : batch.getRemainAmount();
            if (amount <= 0 || transactionMapper.expire(batch.getId()) != 1) continue;
            userMapper.deductPoints(batch.getUserId(), amount);
            PointTransaction expired = base(batch.getUserId(), "EXPIRE", -amount, "POINT_EXPIRE", String.valueOf(batch.getId()), "积分批次到期");
            transactionMapper.insert(expired); count++;
        }
        return count;
    }

    private PointTransaction base(Long userId, String type, int amount, String sourceType, String sourceId, String remark) {
        PointTransaction t = new PointTransaction(); t.setUserId(userId); t.setTransactionType(type); t.setAmount(amount);
        t.setRemainAmount("EARN".equals(type) ? amount : 0); t.setSourceType(sourceType); t.setSourceId(sourceId); t.setRemark(remark);
        t.setExpireTime("EARN".equals(type) ? LocalDateTime.now().plusYears(1) : null); return t;
    }
    private PointTransactionVO toVO(PointTransaction t) { PointTransactionVO v = new PointTransactionVO(); v.setId(t.getId()); v.setTransactionType(t.getTransactionType()); v.setAmount(t.getAmount()); v.setRemainAmount(t.getRemainAmount()); v.setSourceType(t.getSourceType()); v.setRemark(t.getRemark()); v.setExpireTime(t.getExpireTime()); v.setCreateTime(t.getCreateTime()); return v; }
}
