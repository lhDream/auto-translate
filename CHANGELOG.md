# 更新日志

本文件记录各版本的变更。更早版本的原始说明可在 [Releases](https://github.com/lhDream/auto-translate/releases) 页面查看。

## [Unreleased]

## [V0.2.0-alpha] - 待发布

### 新增
- Azure 翻译引擎
- 百度翻译引擎（APP ID + 密钥）
- 腾讯云机器翻译引擎（TC3-HMAC-SHA256 签名）
- OpenAI 翻译引擎，支持自定义 Base URL 与模型名

### 改进
- 聊天框三空格翻译改为协程执行并防抖，连续触发会取消上一次请求
- 后台翻译改用 `SupervisorJob + Dispatchers.IO`，单次失败不影响后续
- 翻译引擎改为注册制，设置页补齐全部引擎配置项
- 移除 langchain4j，改用自研轻量 HTTP 客户端
- 代码重构为 `data / domain / presentation / infrastructure` 分层
- CI 升级 `actions/checkout`、`actions/setup-java`

### 修复
- 引擎未配置或请求失败时不再中断翻译流程

### 发布形态
- 双版本产物：`auto-translate-v7.jar`（minGameVersion 136，面向 Mindustry 7.0，Build 136 起）
- 双版本产物：`auto-translate-v8.jar`（minGameVersion 147，面向 Mindustry 8.0，Build 147 起）
- 两个包除最低版本标记外内容一致；DeepL 不支持 v7

## [V0.1.0-alpha] - 2025-08-21

### 新增
- DeepL 翻译引擎
- 适配 Mindustry v8

### 说明
- DeepL 不支持 v7，故本版本未提供 v7 构建；可自行修改配置文件为 v7，除 DeepL 外功能正常

## [V0.0.2-alpha] - 2025-08-18

### 改进
- 优化配置界面
- 配置项修改后实时生效

### 修复
- 修复 Google 翻译模式无法正常使用的问题

## [V0.0.1-alpha] - 2025-08-16

首个尝鲜版：
- 其他玩家消息自动翻译
- 输入框连续三个空格触发本地翻译
- 微软免费翻译
- Google API Key 翻译
