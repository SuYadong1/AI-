# 数据库初始化
-- 创建库
create database if not exists sudada;

-- 切换库
use sudada;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 应用表
create table if not exists app
(
    id              bigint auto_increment comment 'id' primary key,
    appName         varchar(128)                       not null comment '应用名',
    appDesc         varchar(2048)                      null comment '应用描述',
    appIcon         varchar(1024)                      null comment '应用图标',
    appType         tinyint  default 0                 not null comment '应用类型（0-得分类，1-测评类）',
    scoringStrategy tinyint  default 0                 not null comment '评分策略（0-自定义，1-AI）',
    reviewStatus    int      default 0                 not null comment '审核状态：0-待审核, 1-通过, 2-拒绝',
    reviewMessage   varchar(512)                       null comment '审核信息',
    reviewerId      bigint                             null comment '审核人 id',
    reviewTime      datetime                           null comment '审核时间',
    userId          bigint                             not null comment '创建用户 id',
    createTime      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint  default 0                 not null comment '是否删除',
    index idx_appName (appName)
) comment '应用' collate = utf8mb4_unicode_ci;


-- 题目表
create table if not exists question
(
    id              bigint auto_increment comment 'id' primary key,
    questionContent text                               null comment '题目内容（json格式）',
    appId           bigint                             not null comment '应用 id',
    userId          bigint                             not null comment '创建用户 id',
    createTime      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint  default 0                 not null comment '是否删除',
    index idx_appId (appId)
) comment '题目' collate = utf8mb4_unicode_ci;


-- 评分结果表
create table if not exists scoring_result
(
    id               bigint auto_increment comment 'id' primary key,
    resultName       varchar(128)                       not null comment '结果名称，如物流师',
    resultDesc       text                               null comment '结果描述',
    resultPicture    varchar(1024)                      null comment '结果图片',
    resultProp       varchar(128)                       null comment '结果属性集合 JSON，如 [I,S,T,J]',
    resultScoreRange int                                null comment '结果得分范围，如 80，表示 80及以上的分数命中此结果',
    appId            bigint                             not null comment '应用 id',
    userId           bigint                             not null comment '创建用户 id',
    createTime       datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime       datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete         tinyint  default 0                 not null comment '是否删除',
    index idx_appId (appId)
) comment '评分结果' collate = utf8mb4_unicode_ci;


-- 用户答题记录表
create table if not exists user_answer
(
    id              bigint auto_increment primary key,
    appId           bigint                             not null comment '应用 id',
    appType         tinyint  default 0                 not null comment '应用类型（0-得分类，1-角色测评类）',
    scoringStrategy tinyint  default 0                 not null comment '评分策略（0-自定义，1-AI）',
    choices         text                               null comment '用户答案（JSON 数组）',
    resultId        bigint                             null comment '评分结果 id',
    resultName      varchar(128)                       null comment '结果名称，如物流师',
    resultDesc      text                               null comment '结果描述',
    resultPicture   varchar(1024)                      null comment '结果图标',
    resultScore     int                                null comment '得分',
    userId          bigint                             not null comment '用户 id',
    createTime      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint  default 0                 not null comment '是否删除',
    index idx_appId (appId),
    index idx_userId (userId)
) comment '用户答题记录' collate = utf8mb4_unicode_ci;


-- 得分 评分结果初始化
INSERT INTO scoring_result (id, resultName, resultDesc, resultPicture, resultProp, resultScoreRange, createTime, updateTime, isDelete, appId, userId) VALUES (17, '知识大师', '你真棒棒哦，知识掌握地非常出色！', null, null, 9, '2024-04-25 15:05:44', '2024-05-09 12:28:21', 0, 2, 1);
INSERT INTO scoring_result (id, resultName, resultDesc, resultPicture, resultProp, resultScoreRange, createTime, updateTime, isDelete, appId, userId) VALUES (18, '地理小能手！', '你对于地理知识了解得相当不错，但还有一些小地方需要加强哦！', null, null, 7, '2024-04-25 15:05:44', '2024-05-09 12:28:21', 0, 2, 1);
INSERT INTO scoring_result (id, resultName, resultDesc, resultPicture, resultProp, resultScoreRange, createTime, updateTime, isDelete, appId, userId) VALUES (19, '继续加油！', '还需努力哦', null, null, 0, '2024-04-25 15:05:44', '2024-05-09 12:28:21', 0, 2, 1);

-- mbti数据
INSERT INTO scoring_result (id, resultName, resultDesc, resultPicture, resultProp, resultScoreRange, appId, isDelete, userId)
VALUES
    (3, 'ISTJ（物流师）', '忠诚可靠，被公认为务实，注重细节。', 'icon_url_istj', '["I","S","T","J"]', 0, 2, 0, 1),
    (4, 'ISFJ（守护者）', '善良贴心，以同情心和责任为特点。', 'icon_url_isfj', '["I","S","F","J"]', 0, 2, 0, 1),
    (5, 'INFJ（占有者）', '理想主义者，有着深刻的洞察力，善于理解他人。', 'icon_url_infj', '["I","N","F","J"]', 0, 2, 0, 1),
    (6, 'INTJ（设计师）', '独立思考者，善于规划和实现目标，理性而果断。', 'icon_url_intj', '["I","N","T","J"]', 0, 2, 0, 1),
    (7, 'ISTP（运动员）', '冷静自持，善于解决问题，擅长实践技能。', 'icon_url_istp', '["I","S","T","P"]', 0, 2, 0, 1),
    (8, 'ISFP（艺术家）', '具有艺术感和敏感性，珍视个人空间和自由。', 'icon_url_isfp', '["I","S","F","P"]', 0, 2, 0, 1),
    (9, 'INFP（治愈者）', '理想主义者，富有创造力，以同情心和理解他人著称。', 'icon_url_infp', '["I","N","F","P"]', 0, 2, 0, 1),
    (10, 'INTP（学者）', '思维清晰，探索精神，独立思考且理性。', 'icon_url_intp', '["I","N","T","P"]', 0, 2, 0, 1),
    (11, 'ESTP（拓荒者）', '敢于冒险，乐于冒险，思维敏捷，行动果断。', 'icon_url_estp', '["E","S","T","P"]', 0, 2, 0, 1),
    (12, 'ESFP（表演者）', '热情开朗，善于社交，热爱生活，乐于助人。', 'icon_url_esfp', '["E","S","F","P"]', 0, 2, 0, 1),
    (13, 'ENFP（倡导者）', '富有想象力，充满热情，善于激发他人的活力和潜力。', 'icon_url_enfp', '["E","N","F","P"]', 0, 2, 0, 1),
    (14, 'ENTP（发明家）', '充满创造力，善于辩论，挑战传统，喜欢探索新领域。', 'icon_url_entp', '["E","N","T","P"]', 0, 2, 0, 1),
    (15, 'ESTJ（主管）', '务实果断，善于组织和管理，重视效率和目标。', 'icon_url_estj', '["E","S","T","J"]', 0, 2, 0, 1),
    (16, 'ESFJ（尽责者）', '友善热心，以协调、耐心和关怀为特点，善于团队合作。', 'icon_url_esfj', '["E","S","F","J"]', 0, 2, 0, 1),
    (17, 'ENFJ（教导着）', '热情关爱，善于帮助他人，具有领导力和社交能力。', 'icon_url_enfj', '["E","N","F","J"]', 0, 2, 0, 1),
    (18, 'ENTJ（统帅）', '果断自信，具有领导才能，善于规划和执行目标。', 'icon_url_entj', '["E","N","T","J"]', 0, 2, 0, 1);