# AI 智能养老社区管理系统 - Claude Code 分阶段执行提示词

> 使用说明：本项目体量大（17 表、80+ 接口、双前端），Claude Code 单次上下文无法一次完成。按 P0→P10 顺序执行，每个阶段一条提示词，直接复制粘贴即可。每阶段完成后运行验收检查，通过再进入下一阶段。
>
> 版本号策略：严格按需求文档版本号。若 Maven/npm 拉取时发现版本不存在，**立即停止并报告**，由用户决定替代版本，不得擅自降级。

---

## 全局上下文（每阶段提示词已内嵌，无需单独注入）

- 项目名：AI 智能养老社区管理系统（eldercare）
- 架构：前后端分离，单机部署
- **本地环境（已检测）**：JDK 25.0.3（Temurin，路径 C:\java-situ\jdk-25\jdk-25.0.3+9）、Maven 3.9.6（C:\Users\YOUR_USER\apache-maven-3.9.6）、Node.js 25.3.0、npm 11.6.2
- **Redis**：Windows 本地版 8.8.0，路径 C:\Users\YOUR_USER\Downloads\redis-windows-8.8.0，直接启动无需 Docker
- **MySQL**：8.0.46，安装在 WSL 中（Ubuntu），Spring Boot 通过 localhost:3306 连接（WSL 端口自动转发到宿主机）
- **Docker**：装在 WSL 中，本项目 Redis 已用 Windows 本地版，MySQL 已用 WSL 直装版，Docker 仅用于 P10 容器化部署
- 后端：JDK 25.0.3 + Spring Boot 4.1.0 + MyBatis 3.5.19 + MySQL 8.0.46(WSL) + Redis 8.8.0(Win本地) + Spring AI 2.0.0（DeepSeek）
- 会员端前端：Vue 3.5.40 + Vite 8.2.0 + Vant 4.10 + Pinia 4.0.2 + Axios 1.19.0（Node.js 25.3.0 构建）
- 管理端前端：Vue 3.5.40 + Vite 8.2.0 + Element Plus 2.14.4 + Pinia 4.0.2 + Vue Router 5.2.0（Node.js 25.3.0 构建）
- 后端包名根：`com.eldercare`
- 数据库名：`eldercare`，字符集 `utf8mb4`，排序 `utf8mb4_0900_ai_ci`，引擎 InnoDB
- 数据表 17 张，全部逻辑删除（`deleted` 字段），除 refresh_token 和 sms_code 外
- 统一响应：`{code, message, data}` + `traceId`
- 认证：JWT 双 Token（Access 2h / Refresh 7d），Redis 黑名单
- AI：Spring AI + DeepSeek，SSE 流式
- 目录结构、表结构、接口清单见需求规格说明书与详细设计文档（已提供）

---

## P0 - 项目初始化 + 数据库 + 基础设施

```
你是一位资深 Java 后端架构师。请在当前目录下从零创建 AI 智能养老社区管理系统的后端项目骨架，严格遵循以下要求。

【技术栈版本（严格按此版本，不可变更）】
- JDK 25.0.3（本机已装，路径 C:\java-situ\jdk-25\jdk-25.0.3+9）
- Maven 3.9.6（本机已装，路径 C:\Users\YOUR_USER\apache-maven-3.9.6）
- Spring Boot 4.1.0
- MyBatis 3.5.19（通过 mybatis-spring-boot-starter 4.1.0 引入）
- MySQL 8.0.46（WSL 直装，非 Docker；application.yml 连接 localhost:3306）
- Redis 8.8.0（Windows 本地版，路径 C:\Users\YOUR_USER\Downloads\redis-windows-8.8.0；application.yml 连接 localhost:6379）
- JJWT 0.13.0
- Spring AI 2.0.0（spring-ai-openai-spring-boot-starter）
- Jakarta Validation 3.1.1
- Apache Commons Lang3 3.20.0
- PageHelper 6.x（兼容 MyBatis 3.5.19）

【若以上任何版本在 Maven Central 不存在，立即停止并报告哪个版本找不到，不要擅自降级，等待用户指定替代版本。】
【注意：JDK 和 Maven 路径需配置正确。Maven 的 JAVA_HOME 应指向 C:\java-situ\jdk-25\jdk-25.0.3+9】
【注意：MySQL 8.0.46 装在 WSL 中，需先 `wsl` 进入 WSL 再执行 mysql 命令；或通过 `wsl mysql -u root -p` 在宿主机调用。Spring Boot 连接 localhost:3306 即可（WSL 端口自动转发）。】
【注意：Redis 是 Windows 本地版，需先手动启动：双击 C:\Users\YOUR_USER\Downloads\redis-windows-8.8.0 下的 redis-server.exe，或用命令行启动。Spring Boot 连接 localhost:6379。】
【注意：MySQL 版本是 8.0.46 而非文档中的 9.7.2。8.0.x 完全兼容本项目所需的 SQL 语法和特性（utf8mb4_0900_ai_ci 排序规则需 MySQL 8.0+，已满足）。建表 SQL 无需修改。】

【任务清单】
1. 创建 Maven 项目，pom.xml 完整声明上述依赖，JDK 编译版本 25。
2. 创建目录结构：
   backend/src/main/java/com/eldercare/
     ├── core/{common,config,entity,mapper,service,filter,exception,util,enums,dto,vo}
     ├── api/{controller,service}        # 会员端
     └── admin/{controller,service}      # 管理端
   backend/src/main/resources/
     ├── application.yml
     ├── mapper/                          # MyBatis XML
     └── static/
3. application.yml 配置：
   - server.port=8080
   - spring.datasource（MySQL，密码用 ${DB_PASSWORD}）
   - spring.data.redis（密码用 ${REDIS_PASSWORD}）
   - spring.ai.openai（base-url=https://api.deepseek.com, api-key=${DEEPSEEK_API_KEY}, model=deepseek-flash）
   - jwt.secret/access-token-expire=7200/refresh-token-expire=604800
   - mybatis.mapper-locations=classpath:mapper/*.xml
   - pagehelper 合理配置
4. 核心配置类（core/config/）：
   - SecurityConfig.java（Spring Security 7.1.0，禁用 CSRF，无状态 session，放行 /api/auth/**、/api/sms/**，其余需认证）
   - MyBatisConfig.java（PageHelper 拦截器）
   - RedisConfig.java（RedisTemplate 序列化用 Jackson2JsonRedisSerializer）
   - CorsConfig.java（允许前端域名）
5. core/common/Result.java：统一响应封装
   - 字段：code(int)、message(String)、data(T)、traceId(String)
   - 静态方法：success()、success(data)、success(message,data)、error(code,message)、error(BusinessException)
6. core/exception/：
   - BusinessException.java（code + message）
   - AuthenticationException.java（401）
   - AccessDeniedException.java（403）
   - ResourceNotFoundException.java（404）
   - GlobalExceptionHandler.java（@RestControllerAdvice，处理上述异常 + MethodArgumentNotValidException + Exception，返回 Result）
7. core/util/JwtUtil.java：
   - 生成 Access Token（2h，含 userId、role、jti）
   - 生成 Refresh Token（7d，含 userId、jti）
   - 解析 Token，校验有效性
   - 提取 userId、role、jti
8. core/filter/JwtAuthenticationFilter.java：
   - 从 Header 提取 Bearer Token
   - 校验签名 + 黑名单（Redis 查 jwt:blacklist:{jti}）
   - 构建 Authentication 放入 SecurityContext
9. 数据库初始化 SQL 脚本 backend/sql/01_schema.sql：
   严格按详细设计文档 6.3 节创建 17 张表（user、refresh_token、sms_code、health_record、questionnaire、question、assessment_result、appointment_package、appointment_slot、appointment、community_activity、activity_registration、health_guidance、ai_conversation_session、ai_conversation_message、message、sys_config），包含所有字段、类型、约束、默认值、索引（按 6.5 节）。
   注意：
   - refresh_token 和 sms_code 不设 deleted 字段
   - 其余表 deleted TINYINT 默认 0
   - user 表 role/phone 不设默认值
10. 种子数据脚本 backend/sql/02_data.sql：
    - sys_config 插入 6 条（ai_chat_system_prompt、register_bonus_points=100、checkin_bonus_points=50、health_assessment_min_score=60、access_token_expire_hours=2、refresh_token_expire_days=7）
    - 管理员：phone=13800000000，密码 <ADMIN_PASSWORD>（BCrypt），role=ADMIN，points=99999，member_level=PLATINUM
    - 测试会员：phone=13800138000，密码 <MEMBER_PASSWORD>（BCrypt），role=MEMBER，points=1000
    - 1 个示例问卷 + 4 道题目
    - 3 个示例体检套餐
11. docker-compose.yml（项目根目录，仅供 P10 容器化部署用，本地开发不依赖）：
    - mysql:9.7.2（端口 3306，root 密码环境变量，挂载 sql 目录到 /docker-entrypoint-initdb.d）
    - redis:8.8.0（端口 6379，密码环境变量）
    - 两服务均加健康检查
    - 本地开发时**不启动此文件**，MySQL 用 WSL 直装版（8.0.46），Redis 用 Windows 本地版（8.8.0）
12. .gitignore（target/、*.iml、.idea/、node_modules/、logs/、data/upload/）
13. README.md：说明项目结构、环境要求、启动步骤（含 Redis 手动启动、WSL MySQL 连接说明）。

【验收检查】
- mvn -q -DskipTests compile 编译通过（若版本不存在则报告）
- 手动启动 Redis（C:\Users\YOUR_USER\Downloads\redis-windows-8.8.0\redis-server.exe）
- 通过 `wsl mysql -u root -p` 连接 WSL 中的 MySQL，执行 01_schema.sql + 02_data.sql 无报错，17 张表存在，种子数据可查
- 表结构字段与详细设计文档 6.3 节完全一致

完成后列出创建的所有文件清单。
```

---

## P1 - 认证授权模块

```
你是 AI 智能养老社区管理系统后端开发者。项目骨架已由 P0 创建完成（包名 com.eldercare，Spring Boot 4.1.0，JDK 25.0.3）。现实现认证授权模块。

【前置确认】
- user、refresh_token、sms_code 表已存在
- JwtUtil、JwtAuthenticationFilter、SecurityConfig、Result、GlobalExceptionHandler 已存在
- 本阶段只写 api（会员端）层的认证接口，不碰 admin 层

【任务清单】
1. 实体类（core/entity/）：
   - User.java（对应 user 表全字段，@TableName 或手动映射均可）
   - RefreshToken.java
   - SmsCode.java
2. Mapper（core/mapper/ + resources/mapper/*.xml）：
   - UserMapper：selectByPhone、selectById、insert、updatePassword、updatePoints（原子加减）、updateStatus、updateMemberLevel
   - RefreshTokenMapper：insert、selectByToken、selectByUserId、deleteByToken、deleteByUserId
   - SmsCodeMapper：insert、selectLatestByPhone、updateUsed
   - 所有查询带 AND deleted = 0（refresh_token、sms_code 无此字段）
3. DTO（core/dto/）：
   - SendCodeRequest{phone}
   - RegisterRequest{phone, code, password}（密码校验：长度≥8，含字母+数字）
   - LoginRequest{phone, password}
   - RefreshRequest{refreshToken}
   - ResetPasswordRequest{phone, code, newPassword}
   - UpdatePasswordRequest{oldPassword, newPassword}
4. VO（core/vo/）：
   - LoginVO{accessToken, refreshToken, userId, phone, realName, role, memberLevel, points, avatar}
5. Service（api/service/）：
   - AuthService：
     - sendSmsCode(phone)：限流校验（3次/分钟/手机号，10次/天/手机号，Redis 滑动窗口）→ 生成 6 位验证码 → 存 sms_code 表（有效期 5 分钟）→ 调用阿里云短信发送（短信服务可 Mock 打日志，但要留 SmsService 接口）
     - register(req)：校验验证码 → 校验手机号未注册 → BCrypt 加密密码 → 插入 user（role=MEMBER, points=100, member_level=NORMAL）→ 删除已用验证码
     - login(req)：查用户 → 校验密码 → 校验 status=ENABLED → 生成双 Token → RefreshToken 存库 → 返回 LoginVO
     - refresh(refreshToken)：校验签名 → 查库确认存在且未过期 → 检查 access token 是否在黑名单 → 生成新 Access Token
     - logout(accessToken, refreshToken)：Access Token jti 加入 Redis 黑名单（TTL=剩余有效期）→ 物理删除 RefreshToken 记录 → RefreshToken jti 也加黑名单
     - resetPassword(req)：校验验证码 → BCrypt 新密码 → 更新 → 删除该用户所有 RefreshToken + Access Token jti 加用户级黑名单 jwt:blacklist:user:{userId}
   - 限流实现：Redis + Lua 脚本滑动窗口，Key 规范见设计文档 8.4 节
6. Controller（api/controller/AuthController.java，路径 /api/auth）：
   - POST /api/auth/sms-code        发送验证码
   - POST /api/auth/register        注册
   - POST /api/auth/login           登录
   - POST /api/auth/refresh         刷新 Token
   - POST /api/auth/logout          登出（需认证）
   - POST /api/auth/reset-password  密码找回
7. 登录失败锁定：同一手机号密码错误 5 次，锁定 30 分钟（Redis key rl:login:err:{phone}）
8. 单元测试（src/test/）：AuthService 的 register、login 核心路径

【验收检查】
- mvn -q -DskipTests compile 通过
- 启动应用，用 curl/Postman 验证：发送验证码→注册→登录→刷新→登出 全流程
- 登录接口连续错误 5 次后锁定
- 短信接口 1 分钟内第 4 次请求返回 429
- 登出后 Access Token 再请求返回 401

完成后列出本阶段创建/修改的文件清单。
```

---

## P2 - 健康记录 + 健康评测模块

```
你是 AI 智能养老社区管理系统后端开发者。P0、P1 已完成。现实现健康记录模块和健康评测模块（会员端）。

【前置确认】
- health_record、questionnaire、question、assessment_result 表已存在
- UserMapper 已有 selectById（可取 height 计算 BMI）
- Spring AI 已配置（DeepSeek）

【任务清单 - 健康记录模块】
1. 实体：HealthRecord.java
2. Mapper：HealthRecordMapper
   - insert、selectById、selectByUserId（分页）、selectRecent6Months(userId)（用于趋势分析）
3. DTO：
   - HealthRecordRequest{systolic, diastolic, bloodSugar, heartRate, weight, memo}
4. VO：
   - HealthRecordVO（含所有字段 + recordedAt）
   - HealthTrendVO{indicator, avgValue, maxValue, minValue, records:[{date,value}]}
5. Service（api/service/HealthRecordService.java）：
   - create(userId, req)：
     a. 从 User 取 height，计算 BMI = weight / (height/100)^2，保留 1 位小数
     b. 保存 health_record
     c. 健康提醒判断（按设计文档 5.2 节阈值表）：
        - 收缩压<90 或≥140、舒张压<60或≥90、空腹血糖<3.9或>6.1、心率<60或>100、BMI<18.5或≥24
        - 命中则生成 health_guidance 记录（type=DATA_SUMMARY）+ 调用 AI 生成建议 + 插入 message（type=HEALTH_REMINDER）
        - 去重：同一 user_id + indicator + date 当日只推一次（查 health_guidance 表）
   - list(userId, pageNum, pageSize)：分页查询历史
   - trend(userId)：查最近 6 个月数据，按指标分组算 avg/max/min，返回趋势数据
   - detail(userId, recordId)：详情，校验归属
6. Controller（api/controller/HealthRecordController.java，/api/member/health）：
   - POST /api/member/health          录入
   - GET  /api/member/health/list     历史（分页）
   - GET  /api/member/health/trend    趋势

【任务清单 - 健康评测模块】
7. 实体：Questionnaire.java、Question.java、AssessmentResult.java
8. Mapper：
   - QuestionnaireMapper：selectPublishedList（分页）、selectById
   - QuestionMapper：selectByQuestionnaireId（按 sort_order 排序）
   - AssessmentResultMapper：insert、selectByUserId（分页）、selectById
9. DTO：
   - AssessmentSubmitRequest{questionnaireId, answers:[{qid, type, value}]}（value 类型根据 type 变化）
10. VO：
    - QuestionnaireVO（含 questions 列表）
    - AssessmentResultVO（含 aiScore、aiSuggestion、createTime）
11. Service（api/service/AssessmentService.java）：
    - listQuestionnaires(pageNum, pageSize)
    - getQuestionnaire(id)：返回问卷 + 题目列表
    - submit(userId, req)：
      a. 保存 answers 快照 JSON（结构见设计文档 6.3.7）
      b. 调用 Spring AI（DeepSeek）对答案评分（0-100）+ 生成个性化建议
      c. 保存 assessment_result（ai_score + ai_suggestion）
      d. 原子加积分 20（完成评测奖励）
    - history(userId, pageNum, pageSize)
    - detail(userId, id)：校验归属
12. Controller（api/controller/AssessmentController.java，/api/member/assessment）：
    - GET  /api/member/assessment/list            问卷列表
    - GET  /api/member/assessment/{id}            问卷详情（含题目）
    - POST /api/member/assessment/submit          提交评测
    - GET  /api/member/assessment/history         评测历史
    - GET  /api/member/assessment/{id}/detail     评测详情

【验收检查】
- 编译通过
- 录入健康数据后 BMI 自动计算，超标指标生成 health_guidance + message
- 同指标当日重复录入不再重复推送
- 趋势接口返回 6 个月数据的 avg/max/min
- 提交评测后 AI 返回分数 + 建议，积分 +20
- 查看他人评测详情返回 403

完成后列出本阶段文件清单。
```

---

## P3 - AI 对话模块

```
你是 AI 智能养老社区管理系统后端开发者。P0-P2 已完成。现实现 AI 对话模块（会员端），核心是 SSE 流式响应。

【前置确认】
- ai_conversation_session、ai_conversation_message 表已存在
- Spring AI 2.0.0 已配置 DeepSeek（spring.ai.openai）
- sys_config 有 ai_chat_system_prompt 配置项

【任务清单】
1. 实体：AiConversationSession.java、AiConversationMessage.java
   - message 表增加 status 字段说明：实际表结构无 status 字段，用 message 字段内容标记失败（失败时 content 存错误信息，role=assistant）。若需区分成功/失败，可在 message 前加标识。按现有表结构实现。
2. Mapper：
   - AiConversationSessionMapper：insert、selectByUserId（分页）、selectById、deleteById（逻辑删除）
   - AiConversationMessageMapper：insert、selectBySessionId（最近 N 条，按 create_time 升序）、selectRecent10Messages(sessionId)
3. DTO：
   - CreateSessionRequest{sessionName}
   - SendMessageRequest{sessionId, message}
4. VO：
   - SessionVO{id, sessionName, createTime, lastMessage}
   - MessageVO{id, role, message, createTime}
5. Service（api/service/ChatService.java）：
   - createSession(userId, sessionName)
   - listSessions(userId, pageNum, pageSize)：返回会话列表 + 最后一条消息预览
   - deleteSession(userId, sessionId)：校验归属，逻辑删除会话 + 消息
   - getHistory(userId, sessionId, pageNum, pageSize)：校验会话归属，分页返回消息
   - streamChat(userId, sessionId, userMessage, emitter: SseEmitter)：
     a. 校验会话归属
     b. 保存用户消息（role=user）
     c. 取最近 10 轮对话（20 条消息）作为上下文
     d. 从 sys_config 读取 system prompt
     e. 调用 Spring AI ChatClient.stream()，逐 token 推送到 SseEmitter
     f. 流结束后保存 AI 回复（role=assistant）
     g. 异常处理：
        - 客户端断开：捕获 IOException，已接收内容保存为完整消息
        - AI 超时（60s）：主动 complete，保存已接收片段
        - AI 错误：保存用户消息，AI 消息 content 存错误描述
6. Controller（api/controller/ChatController.java，/api/member/chat）：
   - POST /api/member/chat/session           创建会话
   - GET  /api/member/chat/sessions          会话列表
   - DELETE /api/member/chat/session/{id}    删除会话
   - GET  /api/member/chat/history/{id}      对话历史
   - POST /api/member/chat/send              发送消息（SSE）
     - 返回类型 text/event-stream
     - 使用 SseEmitter，超时设置 60s
     - 事件格式：data: {token}\n\n，结束事件 data: [DONE]\n\n

【验收检查】
- 创建会话→发送消息→收到 SSE 流式响应→历史可查
- 多轮对话上下文连贯（AI 能记住前 10 轮）
- 客户端中途断开，已接收内容不丢失
- AI 错误时用户消息仍保存
- 删除会话后再查历史返回空
- 访问他人会话返回 403

完成后列出文件清单。
```

---

## P4 - 体检预约模块

```
你是 AI 智能养老社区管理系统后端开发者。P0-P3 已完成。现实现体检预约模块（会员端），核心是并发安全。

【前置确认】
- appointment_package、appointment_slot、appointment 表已存在
- UserMapper.updatePoints 原子操作已存在
- 短信服务（SmsService）已存在

【任务清单】
1. 实体：AppointmentPackage.java、AppointmentSlot.java、Appointment.java
2. Mapper：
   - AppointmentPackageMapper：selectList（分页，按 status=ENABLED）、selectById
   - AppointmentSlotMapper：
     - selectByPackageAndDate(packageId, date)
     - selectByIdForUpdate（SELECT ... FOR UPDATE，用于悲观锁备选）
     - incrementCurrentCount(slotId)：UPDATE appointment_slot SET current_count=current_count+1 WHERE id=? AND current_count<max_count AND deleted=0，返回影响行数
     - decrementCurrentCount(slotId)：UPDATE ... SET current_count=current_count-1 WHERE id=? AND current_count>0
   - AppointmentMapper：insert、selectById、selectByUserId（分页）、updateStatus、updateReportUrl
3. DTO：
   - AppointmentQueryRequest{packageId, date}
   - CreateAppointmentRequest{slotId, packageId}
4. VO：
   - PackageVO（含 items JSON 解析为 List<String>）
   - SlotVO（含剩余名额 remaining = max_count - current_count）
   - AppointmentVO（含套餐名、时段信息、状态）
5. Service（api/service/AppointmentService.java）：
   - listPackages(pageNum, pageSize)
   - getPackageDetail(id)
   - listSlots(packageId, date)
   - createAppointment(userId, req)：@Transactional
     a. 查用户积分是否≥套餐价格
     b. 原子扣积分：UPDATE user SET points=points-? WHERE id=? AND points>=?，返回影响行数，0 则抛 BusinessException("积分不足")
     c. 原子加名额：incrementCurrentCount，影响行数 0 则回滚（退积分）抛 BusinessException("名额已满")
     d. 插入 appointment（status=PENDING）
     e. 发送预约成功短信（容错，失败记日志不回滚）
   - cancelAppointment(userId, appointmentId)：@Transactional
     a. 查预约，校验归属 + 状态可取消（PENDING/CONFIRMED）
     b. 原子退积分
     c. 原子减名额
     d. 更新状态 CANCELED
     e. 发取消短信（容错）
   - listMyAppointments(userId, pageNum, pageSize)
   - getReport(userId, appointmentId)：校验归属，返回报告下载 URL（短期签名 5 分钟）
     - 报告文件路径：data/upload/report/yyyyMM/{uuid}.pdf
     - 下载接口代理（不直链磁盘），URL 含签名 token
6. Controller（api/controller/AppointmentController.java，/api/member/appointment）：
   - GET  /api/member/appointment/packages          套餐列表
   - GET  /api/member/appointment/packages/{id}     套餐详情
   - GET  /api/member/appointment/slots             时段列表（?packageId=&date=）
   - POST /api/member/appointment                   预约
   - POST /api/member/appointment/{id}/cancel       取消
   - GET  /api/member/appointment/mine              我的预约
   - GET  /api/member/appointment/{id}/report       报告下载（带签名）

【验收检查】
- 积分不足时预约返回 409 "积分不足"
- 名额满后预约返回 409 "名额已满"
- 模拟 10 并发预约同一只剩 1 名额的时段，只有 1 个成功（可用 CountDownLatch 测试）
- 取消后积分退还、名额恢复
- 取消他人预约返回 403
- 报告下载 URL 5 分钟后失效

完成后列出文件清单。
```

---

## P5 - 社区活动 + 积分 + 消息 + 个人中心模块

```
你是 AI 智能养老社区管理系统后端开发者。P0-P4 已完成。现实现剩余会员端模块：社区活动、消息通知、个人中心。

【前置确认】
- community_activity、activity_registration、message 表已存在
- UserMapper 原子加减积分已存在

【任务清单 - 社区活动】
1. 实体：CommunityActivity.java、ActivityRegistration.java
2. Mapper：
   - CommunityActivityMapper：selectList（分页，按 status）、selectById、incrementParticipants（原子）、decrementParticipants（原子）
   - ActivityRegistrationMapper：insert、selectByUserAndActivity（查重）、selectByUserId（分页）、selectById、updateCheckInStatus
3. DTO：
   - ActivityListQuery{status, pageNum, pageSize}
4. VO：
   - ActivityVO（含 remaining = max_participants - current_participants）
   - MyActivityVO（含活动信息 + 签到状态）
5. Service（api/service/ActivityService.java）：
   - listActivities(query)
   - getActivityDetail(id)
   - register(userId, activityId)：@Transactional
     a. 校验活动状态=REGISTRATING
     b. 校验报名时间范围内
     c. 唯一索引 uk_user_activity 防重复（捕获 DuplicateKeyException）
     d. 原子加人数：UPDATE ... SET current_participants=current_participants+1 WHERE id=? AND current_participants<max_participants，0 则抛"名额已满"
     e. 插入报名记录
   - checkIn(userId, activityId)：@Transactional
     a. 校验已报名
     b. 校验活动状态=IN_PROGRESS
     c. 校验未签到
     d. 更新签到状态 CHECKED_IN
     e. 原子加积分 50
   - listMyActivities(userId, pageNum, pageSize)
   - getCheckInStatus(userId, activityId)
6. Controller（api/controller/ActivityController.java，/api/member/activity）：
   - GET  /api/member/activity/list                活动列表
   - GET  /api/member/activity/{id}                详情
   - POST /api/member/activity/{id}/register       报名
   - POST /api/member/activity/{id}/checkin        签到
   - GET  /api/member/activity/mine                我的活动
   - GET  /api/member/activity/{id}/checkin-status 签到状态

【任务清单 - 消息通知】
7. 实体：Message.java
8. Mapper：MessageMapper（selectByUserId 分页、selectById、updateIsRead、countUnread）
9. VO：MessageVO、UnreadCountVO
10. Service（api/service/MessageService.java）：
    - list(userId, pageNum, pageSize, type?)：支持按类型筛选
    - detail(userId, id)：校验归属
    - markAsRead(userId, id)
    - unreadCount(userId)
11. Controller（api/controller/MessageController.java，/api/member/message）：
    - GET  /api/member/message/list          消息列表
    - GET  /api/member/message/{id}          详情
    - PUT  /api/member/message/{id}/read     标记已读
    - GET  /api/member/message/unread-count  未读统计

【任务清单 - 个人中心】
12. DTO：
    - UpdateProfileRequest{realName, gender, birthDate, height, avatar, emergencyContact}
    - ChangePasswordRequest{oldPassword, newPassword}
13. VO：UserProfileVO
14. Service（api/service/ProfileService.java）：
    - getProfile(userId)
    - updateProfile(userId, req)
    - changePassword(userId, req)：校验旧密码 → BCrypt 新密码 → 更新 → 强制下线（加用户级黑名单）
15. Controller（api/controller/ProfileController.java，/api/member/profile）：
    - GET  /api/member/profile           获取信息
    - PUT  /api/member/profile           更新信息
    - PUT  /api/member/profile/password  修改密码

【验收检查】
- 活动报名满后返回 409
- 重复报名返回 409（唯一索引）
- 签到后积分 +50
- 未报名签到返回 400
- 消息标记已读后未读数减少
- 修改密码后旧 Token 失效（黑名单）

完成后列出文件清单。
```

---

## P6 - 管理端后端（全部）

```
你是 AI 智能养老社区管理系统后端开发者。P0-P5 已完成（会员端全部完成）。现实现管理端全部接口。

【前置确认】
- 所有 Mapper、实体已存在
- SecurityConfig 已配置 /api/admin/** 需 ADMIN 角色
- RBAC：MEMBER 不能访问 /api/admin/**

【任务清单 - 仪表盘】
1. Service（admin/service/DashboardService.java）：
   - getStatistics()：返回 会员总数、今日新增会员、今日预约数、今日活动报名数、待完成预约数
2. Controller（admin/controller/DashboardController.java，/api/admin/dashboard）：
   - GET /api/admin/dashboard

【任务清单 - 会员管理】
3. Service（admin/service/AdminMemberService.java）：
   - list(query)：分页 + 条件筛选（phone、realName、status、memberLevel）
   - detail(id)：基本信息 + 积分 + 近期健康记录 + 预约记录
   - enable(id) / disable(id)
   - updateMemberLevel(id, level)
   - adjustPoints(id, delta, reason)：原子操作
   - resetPassword(id)：重置为默认密码（如 <RESET_PASSWORD>），BCrypt 加密
4. Controller（admin/controller/AdminMemberController.java，/api/admin/members）：
   - GET  /api/admin/members           列表
   - GET  /api/admin/members/{id}      详情
   - PUT  /api/admin/members/{id}/status      启用/禁用
   - PUT  /api/admin/members/{id}/level       调整等级
   - PUT  /api/admin/members/{id}/points      调整积分
   - POST /api/admin/members/{id}/reset-password

【任务清单 - 体检管理】
5. Service（admin/service/AdminAppointmentService.java）：
   - 套餐 CRUD（createPackage、updatePackage、deletePackage 逻辑删除、listPackages、getPackage）
   - 批量生成时段（generateSlots(packageId, startDate, endDate, timeRanges[], maxCount)）
   - 预约列表管理（listAppointments，按状态/日期筛选）
   - 上传报告（uploadReport(appointmentId, file)）：存 data/upload/report/yyyyMM/{uuid}.pdf，更新 appointment.report_url
   - 文件大小≤20MB，仅 PDF
6. Controller（admin/controller/AdminAppointmentController.java，/api/admin/appointment）：
   - POST   /api/admin/appointment/packages           创建套餐
   - PUT    /api/admin/appointment/packages/{id}      编辑
   - DELETE /api/admin/appointment/packages/{id}      删除
   - GET    /api/admin/appointment/packages           列表
   - POST   /api/admin/appointment/slots/generate     批量生成时段
   - GET    /api/admin/appointment/list               预约列表
   - POST   /api/admin/appointment/{id}/report        上传报告（multipart）

【任务清单 - 评测管理】
7. Service（admin/service/AdminAssessmentService.java）：
   - 问卷 CRUD + 发布/下架（status: DRAFT/PUBLISHED）
   - 题目 CRUD（含 options JSON）
   - 题目排序
8. Controller（admin/controller/AdminAssessmentController.java，/api/admin/assessment）：
   - POST   /api/admin/assessment/questionnaires          创建问卷
   - PUT    /api/admin/assessment/questionnaires/{id}     编辑
   - DELETE /api/admin/assessment/questionnaires/{id}     删除
   - PUT    /api/admin/assessment/questionnaires/{id}/publish    发布
   - PUT    /api/admin/assessment/questionnaires/{id}/unpublish 下架
   - POST   /api/admin/assessment/questions               添加题目
   - PUT    /api/admin/assessment/questions/{id}          编辑题目
   - DELETE /api/admin/assessment/questions/{id}          删除题目

【任务清单 - 活动管理】
9. Service（admin/service/AdminActivityService.java）：
   - 活动 CRUD
   - 查看报名列表、签到情况
10. Controller（admin/controller/AdminActivityController.java，/api/admin/activity）：
    - POST   /api/admin/activity            创建
    - PUT    /api/admin/activity/{id}       编辑
    - DELETE /api/admin/activity/{id}       删除
    - GET    /api/admin/activity            列表
    - GET    /api/admin/activity/{id}       详情
    - GET    /api/admin/activity/{id}/registrations  报名列表

【任务清单 - 消息管理】
11. Service（admin/service/AdminMessageService.java）：
    - 消息列表（全站，分页）
    - 推送消息（单个用户，站内 + 短信双通道）
    - 批量推送（多用户）
12. Controller（admin/controller/AdminMessageController.java，/api/admin/message）：
    - GET  /api/admin/message                    列表
    - POST /api/admin/message/push               推送单个（{userId, title, content, type, sms:false}）
    - POST /api/admin/message/push-batch         批量推送（{userIds:[], title, content, type, sms:false}）

【任务清单 - 系统配置】
13. Service（admin/service/SysConfigService.java）：
    - list()：所有配置项
    - get(key)
    - update(key, value)：更新配置（含 ai_chat_system_prompt、积分规则参数等）
    - 缓存：配置更新后刷新 Redis 缓存
14. Controller（admin/controller/SysConfigController.java，/api/admin/config）：
    - GET  /api/admin/config              列表
    - GET  /api/admin/config/{key}        获取
    - PUT  /api/admin/config/{key}        更新

【验收检查】
- MEMBER 角色 Token 访问任意 /api/admin/** 返回 403
- 仪表盘统计数据正确
- 会员禁用后该用户登录返回 403
- 批量生成时段正常（如生成某套餐某周每天 3 个时段）
- 上传非 PDF 报告返回 400
- 上传 >20MB 文件返回 400
- 问卷发布后会员端可见
- 修改系统提示词后 AI 对话使用新提示词

完成后列出文件清单。
```

---

## P7 - 定时任务

```
你是 AI 智能养老社区管理系统后端开发者。P0-P6 已完成（后端全部业务接口完成）。现实现定时任务模块。

【任务清单】
1. 在主类加 @EnableScheduling
2. 创建 core/scheduler/SchedulerTask.java（@Scheduled），实现 6 个任务：

| 任务 | Cron | 说明 |
|------|------|------|
| 清理过期积分 | 0 0 2 * * ? | 每天凌晨 2 点，清理 1 年前获取且未使用的积分（需 points 变更记录，若无则按 user.register_time + 1 年判断，记录清理日志） |
| 清理过期短信验证码 | 0 0/10 * * * ? | 每 10 分钟，物理删除 expire_time < NOW() 的 sms_code 记录 |
| 清理过期 AI 对话消息 | 0 0 3 * * ? | 每天凌晨 3 点，逻辑删除 6 个月前的 ai_conversation_message |
| 归档过期历史预约 | 0 0 4 * * ? | 每天凌晨 4 点，逻辑删除 2 年前已完成的 appointment |
| 预约提醒推送 | 0 0 9 * * ? | 每天上午 9 点，查次日待完成预约（PENDING/CONFIRMED），发短信提醒 |
| 同步健康数据每日统计 | 0 30 0 * * ? | 每天 0:30，汇总昨日健康录入数据（可写入统计表或日志） |

3. 每个任务需：
   - try-catch 包裹，失败记 ERROR 日志（不中断后续任务）
   - 记录执行开始/结束 INFO 日志
4. 清理过期积分的实现说明：
   - 若无独立积分流水表，则按 create_time 判断：逻辑删除 user 表中 register_time 超 1 年且 points>0 的用户积分（置 0），记录日志
   - 更优方案：创建 point_record 表记录积分流水（获取时间、金额、是否已用），按此清理。若选择此方案，需回溯修改 P0 的 schema 并补充 Mapper/Service。请选择更合理的方案实现。

【验收检查】
- 应用启动后定时任务注册成功（日志可见）
- 手动触发清理验证码任务，过期记录被删除
- 任务执行失败不影响其他任务
- 每个任务有执行日志

完成后列出文件清单。
```

---

## P8 - 会员端前端（Vue3 + Vant）

```
你是资深前端开发者。后端 API 已全部完成（端口 8080）。现创建会员端前端项目，面向社区老人，移动端适配。

【技术栈版本（严格按此，npm 拉不到报告）】
- Node.js 25.3.0（本机已装）
- npm 11.6.2（本机已装）
- Vue 3.5.40
- Vite 8.2.0
- Vant 4.10
- Pinia 4.0.2
- Axios 1.19.0
- Vue Router（兼容 Vue 3 的版本）
- postcss-px-to-viewport（移动端适配）

【任务清单】
1. 项目初始化（member/ 目录）：
   - npm create vite@latest member -- --template vue
   - 安装上述依赖
   - vite.config.js：端口 5173，代理 /api 到 localhost:8080
2. 目录结构：
   member/src/
     ├── api/           # 接口封装（按模块：auth.js、health.js、chat.js、appointment.js、activity.js、message.js、profile.js、assessment.js）
     ├── router/        # 路由
     ├── stores/        # Pinia（user.js、chat.js）
     ├── utils/         # request.js（axios 封装，Token 无感刷新）、auth.js
     ├── components/    # 公共组件
     ├── views/         # 页面
     └── App.vue
3. request.js（axios 封装）：
   - 请求拦截器：加 Authorization: Bearer {accessToken}
   - 响应拦截器：
     - 401 时用 refreshToken 自动刷新，刷新成功重发原请求
     - 刷新失败跳登录页
     - 统一处理 code != 200 的错误提示（Vant Toast）
   - SSE 请求单独封装（EventSource 或 fetch stream）
4. 路由（带守卫）：
   - /login、/register、/forgot-password（无需认证）
   - /（首页：功能入口）
   - /health（健康记录：录入、历史、趋势图表）
   - /assessment（评测列表）
   - /assessment/{id}（答题）
   - /assessment/result/{id}（结果）
   - /chat（AI 对话列表）
   - /chat/{sessionId}（对话页，SSE 打字机效果）
   - /appointment（套餐列表）
   - /appointment/{id}（套餐详情 + 时段选择 + 预约）
   - /appointment/mine（我的预约）
   - /activity（活动列表）
   - /activity/{id}（详情 + 报名 + 签到）
   - /message（消息列表）
   - /profile（个人中心）
5. 老年人适配（全局样式）：
   - 字体默认放大 1.2 倍（html { font-size: 37.5px } 配合 rem，或直接 body font-size: 18px）
   - 按钮点击区域≥48px
   - 高对比度色彩
   - 关键操作二次确认（Dialog）
6. 核心页面实现：
   - 登录/注册：手机号 + 验证码 + 密码
   - 首页：九宫格功能入口（大图标）
   - 健康录入：表单 + 提交后展示 BMI
   - 健康趋势：用 ECharts 或 Vant Chart 展示折线图
   - AI 对话：聊天气泡布局 + SSE 打字机效果 + 自动滚动
   - 体检预约：套餐卡片 + 时段选择 + 积分显示
   - 活动列表：卡片式 + 报名/签到按钮
   - 消息列表：未读标记 + 标记已读
   - 个人中心：信息展示 + 修改 + 头像上传
7. Pinia stores/user.js：
   - token 管理（accessToken、refreshToken）
   - userInfo
   - login、logout、refreshUserInfo

【验收检查】
- npm run dev 启动无报错
- 登录→首页→各功能页面可正常跳转
- Token 过期自动刷新无感
- AI 对话有打字机效果
- 健康趋势有图表
- 字体明显偏大（老年友好）
- 预约/取消有二次确认

完成后列出文件清单和启动方式。
```

---

## P9 - 管理端前端（Vue3 + Element Plus）

```
你是资深前端开发者。后端 API 已完成。现创建管理端前端项目，PC 端后台管理系统。

【技术栈版本（严格按此）】
- Node.js 25.3.0（本机已装）
- npm 11.6.2（本机已装）
- Vue 3.5.40
- Vite 8.2.0
- Element Plus 2.14.4
- Pinia 4.0.2
- Vue Router 5.2.0
- Axios 1.19.0
- ECharts（仪表盘图表）

【任务清单】
1. 项目初始化（admin/ 目录）：
   - npm create vite@latest admin -- --template vue
   - 安装上述依赖
   - vite.config.js：端口 5174，代理 /api 到 localhost:8080
2. 目录结构：
   admin/src/
     ├── api/
     ├── router/
     ├── stores/
     ├── utils/request.js
     ├── layout/       # 后台布局（侧边栏 + 顶栏 + 内容区）
     ├── components/
     ├── views/
     └── App.vue
3. request.js：同会员端，Token 无感刷新
4. 布局（Layout）：
   - 侧边栏菜单：仪表盘、会员管理、体检管理、评测管理、活动管理、消息管理、系统配置
   - 顶栏：管理员信息、退出登录
   - 面包屑导航
5. 路由（带权限守卫，非 ADMIN 不可访问）：
   - /login
   - /dashboard
   - /members（列表）、/members/{id}（详情）
   - /appointment/packages（套餐）、/appointment/slots（时段）、/appointment/list（预约）
   - /assessment/questionnaires、/assessment/questions
   - /activity/list、/activity/{id}
   - /message/list、/message/push
   - /config
6. 核心页面实现：
   - 登录页
   - 仪表盘：ECharts 统计卡片 + 图表（会员增长趋势、预约趋势）
   - 会员管理：表格 + 搜索 + 分页 + 操作（启用/禁用、调整等级、调整积分、重置密码）
   - 会员详情：基本信息 + 健康记录 + 预约记录
   - 体检套餐管理：表格 + 新增/编辑弹窗（含 items 动态列表）
   - 时段管理：批量生成弹窗（选日期范围 + 时段）
   - 预约管理：表格 + 上传报告（el-upload，限 PDF 20MB）
   - 评测管理：问卷列表 + 题目管理（拖拽排序）
   - 活动管理：表格 + 新增/编辑
   - 消息管理：列表 + 推送弹窗（单发/群发）
   - 系统配置：表单（AI 提示词 textarea + 积分规则 input）

【验收检查】
- npm run dev 启动无报错
- 登录后侧边栏菜单正常
- 各 CRUD 功能正常
- 上传报告限制 PDF + 20MB
- 仪表盘图表正常渲染
- 非管理员 Token 访问被拦截

完成后列出文件清单和启动方式。
```

---

## P10 - Docker 部署 + 文档

```
你是 DevOps 工程师。后端、会员端、管理端代码已全部完成。现完成容器化部署和交付文档。

【任务清单】
1. 后端 Dockerfile（backend/Dockerfile）：
   - 多阶段构建：maven:3.9.6-eclipse-temurin-25 编译 -> eclipse-temurin:25.0.3-jre 运行（与本机 JDK 25.0.3 对齐）
   - 暴露 8080
   - 健康检查（curl /actuator/health）
2. 会员端 Dockerfile（member/Dockerfile）：
   - 多阶段：node:25.3.0-alpine 构建 -> nginx:alpine 运行
   - nginx.conf 配置 SPA history 模式 + /api 反代到后端
3. 管理端 Dockerfile（admin/Dockerfile）：同上
4. docker-compose.yml（项目根目录，完整版）：
   - mysql:9.7.2（挂载 sql 初始化、数据卷、健康检查）
   - redis:8.8.0（密码、数据卷、健康检查）
   - eldercare-backend（依赖 mysql、redis，环境变量注入密码/密钥）
   - eldercare-member（nginx，端口 5173→80）
   - eldercare-admin（nginx，端口 5174→81）
   - 网络、数据卷定义
5. nginx 反代配置（deploy/nginx.conf）：
   - HTTPS（预留证书路径）
   - /api → backend:8080
   - / → 会员端静态资源
   - /admin → 管理端静态资源
6. 部署文档（docs/deployment.md）：
   - 开发环境搭建步骤（JDK 25.0.3 + Maven 3.9.6 + Node.js 25.3.0 + WSL MySQL 8.0.46 + Windows Redis 8.8.0）
   - 本地开发说明：Redis 手动启动（C:\Users\YOUR_USER\Downloads\redis-windows-8.8.0\redis-server.exe），MySQL 通过 `wsl mysql -u root -p` 连接
   - Docker Compose 一键启动说明（用于容器化部署，镜像版本：mysql:9.7.2 + redis:8.8.0）
   - 生产环境部署步骤（证书配置、环境变量、备份策略）
   - 环境变量清单（DB_PASSWORD、REDIS_PASSWORD、JWT_SECRET、DEEPSEEK_API_KEY、SMS AccessKey 等）
   - Maven JAVA_HOME 配置说明（指向 C:\java-situ\jdk-25\jdk-25.0.3+9）
7. API 接口文档（docs/api.md）：
   - 所有 80+ 接口的路径、方法、参数、响应示例
   - 可用 Swagger/OpenAPI 生成（如集成 springdoc-openapi 则在 P0 补充，此处导出）
8. README.md 更新：完整项目说明、架构图、启动方式、默认账号

【验收检查】
- docker compose build 所有镜像构建成功
- docker compose up -d 全部服务启动
- 访问 http://localhost:5173 会员端正常
- 访问 http://localhost:5174 管理端正常
- API 通过反代正常访问
- 部署文档完整可执行

完成后列出文件清单。
```

---

## 执行顺序总览

| 阶段 | 内容 | 依赖 |
|------|------|------|
| P0 | 项目骨架 + 数据库 + 基础设施 | 无 |
| P1 | 认证授权 | P0 |
| P2 | 健康记录 + 健康评测 | P0、P1 |
| P3 | AI 对话 | P0、P1 |
| P4 | 体检预约 | P0、P1 |
| P5 | 社区活动 + 消息 + 个人中心 | P0、P1 |
| P6 | 管理端后端 | P0-P5 |
| P7 | 定时任务 | P0-P6 |
| P8 | 会员端前端 | 后端全部完成 |
| P9 | 管理端前端 | 后端全部完成 |
| P10 | Docker + 文档 | 全部完成 |

> P2、P3、P4、P5 之间无强依赖，可并行（若有多个 Claude Code 实例）。P8、P9 可并行。


