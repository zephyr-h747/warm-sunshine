package com.eldercare.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AI 调用服务（DeepSeek，OpenAI 兼容接口）：
 * - 未配置有效 API Key（占位符）时直接跳过，避免无谓的网络调用
 * - 调用失败降级返回 null，由调用方提供兜底方案
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final ChatClient chatClient;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    public AiService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 阻塞调用 AI 并返回文本；未配置 Key 或调用失败时返回 null
     *
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     */
    public String chat(String systemPrompt, String userMessage) {
        if (!StringUtils.hasText(apiKey) || apiKey.startsWith("sk-placeholder")) {
            log.debug("未配置有效API Key，跳过 AI 调用");
            return null;
        }
        try {
            String content = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .content();
            log.debug("AI 调用成功，返回长度: {}", content == null ? 0 : content.length());
            return content;
        } catch (Exception e) {
            log.warn("AI 调用失败，使用降级方案: {}", e.getMessage());
            return null;
        }
    }
}
