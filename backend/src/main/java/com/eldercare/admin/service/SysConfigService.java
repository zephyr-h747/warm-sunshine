package com.eldercare.admin.service;

import com.eldercare.core.entity.SysConfig;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.SysConfigMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;

/**
 * 【管理端】系统配置服务：
 * - 查询优先走 Redis 缓存（整实体 JSON），未命中回源数据库并回填缓存，数据库始终为权威数据源（缓存解析失败自动回源）
 * - 更新配置后先删除旧缓存再回源重建，避免旧缓存覆盖新值导致不一致（含绕过接口直接改库的场景）
 */
@Service
public class SysConfigService {

    private static final Logger log = LoggerFactory.getLogger(SysConfigService.class);

    private static final String CONFIG_PREFIX = "sys:config:";
    /** 缓存 TTL 24 小时，更新时主动刷新 */
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    /** 缓存用 JSON 序列化器（注册 JavaTimeModule 以支持 LocalDateTime） */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final SysConfigMapper sysConfigMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public SysConfigService(SysConfigMapper sysConfigMapper, StringRedisTemplate stringRedisTemplate) {
        this.sysConfigMapper = sysConfigMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /** 所有配置项 */
    public List<SysConfig> list() {
        return sysConfigMapper.selectList();
    }

    /** 获取配置（Redis 优先，未命中回源数据库并回填缓存） */
    public SysConfig get(String key) {
        String cacheKey = CONFIG_PREFIX + key;
        // 1. 命中缓存且可正常反序列化时直接返回，不查库；脏缓存删除后回源兜底，不阻断业务
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            try {
                SysConfig hit = OBJECT_MAPPER.readValue(cached, SysConfig.class);
                if (hit != null) {
                    return hit;
                }
            } catch (Exception e) {
                log.warn("配置缓存解析失败，回源数据库: key={}", key);
                stringRedisTemplate.delete(cacheKey);
            }
        }
        // 2. 回源数据库并回填缓存（数据库为权威数据源，避免旧缓存覆盖新值）
        SysConfig config = sysConfigMapper.selectByKey(key);
        if (config == null) {
            throw new ResourceNotFoundException("配置项不存在");
        }
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, OBJECT_MAPPER.writeValueAsString(config), CACHE_TTL);
        } catch (Exception e) {
            log.warn("配置缓存回填失败: key={}", key);
        }
        return config;
    }

    /** 更新配置（先删旧缓存，回源后由 get() 重建缓存） */
    public SysConfig update(String key, String value) {
        int rows = sysConfigMapper.updateByKey(key, value);
        if (rows == 0) {
            throw new ResourceNotFoundException("配置项不存在");
        }
        stringRedisTemplate.delete(CONFIG_PREFIX + key);
        log.info("系统配置更新: key={}", key);
        return get(key);
    }
}
