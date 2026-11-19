## V0.2.0-alpha

自 V0.1.0-alpha 以来的第一个功能版本：翻译引擎从 4 个扩展到 8 个，聊天框翻译改为协程防抖，代码结构整体重构。

> ⚠️ **本版本提供两个安装包，请按你的游戏版本下载**（Mindustry 7.0 / 8.0 对应不同的最低版本要求）
>
> | 你的游戏版本 | 下载 |
> |--------------|------|
> | **Mindustry 7.0**（Build 136 起，含 7.0 全系） | `auto-translate-v7.jar` |
> | **Mindustry 8.0**（Build 147 及以上） | `auto-translate-v8.jar` |
>
> 下错了游戏会提示 "mod 不兼容当前版本"，换另一个包即可。

### ✨ 新增翻译引擎

- **Azure** —— Azure Cognitive Services Translator（API Key）
- **百度翻译** —— 百度翻译开放平台（APP ID + 密钥）
- **腾讯云机器翻译** —— 腾讯云 TMT（SecretId + SecretKey + Region，TC3-HMAC-SHA256 签名）
- **OpenAI** —— 支持自定义 Base URL 与模型名，可对接第三方 / 中转接口，默认 `gpt-4o-mini`

至此共支持 8 个引擎：None、Microsoft Free、Google、DeepL、Azure、百度、腾讯、OpenAI。设置页已补齐全部配置项。

### 🔧 改进

- **聊天框翻译防抖**：末尾三个空格触发的本地翻译改为协程执行，连续输入会取消上一次请求，避免旧的翻译结果覆盖新输入
- **异步模型调整**：后台翻译由裸线程改为 `SupervisorJob + Dispatchers.IO`，单条消息翻译失败不会影响后续翻译
- **引擎注册制**：`TranslationManager` 统一注册与切换引擎，未配置密钥的引擎会跳过翻译并给出日志提示
- **依赖精简**：移除试做阶段的 langchain4j，改为自研轻量 HTTP 客户端（体积与启动开销更小）；新增 kotlinx-coroutines
- **代码分层**：重构为 `data / domain / presentation / infrastructure` 四层，便于后续新增引擎
- **CI**：升级 `actions/checkout` 与 `actions/setup-java` 版本

### 🐛 修复

- 引擎未配置或请求失败时的异常处理收敛，不再中断翻译流程

### ⚠️ 兼容性说明

- 本 mod 为客户端 mod，**服务器无需安装**
- **DeepL 仍不支持 v7 版本**：v7 用户若需要 DeepL，请自行升级到 v8
- v7 包与 v8 包除最低版本标记外完全一致，功能无差别

### 📦 安装

1. 按上面的表格下载对应版本的 jar
2. 游戏内 `Mod` → `导入` → 选择该文件
3. 重启游戏
4. 在 `设置` → `Mod 设置` → `自动翻译设置` 中选择引擎并填写密钥（默认 Microsoft Free，免密钥可直接用）

> 如果你之前装过旧版本，直接导入覆盖即可，配置文件会保留。
> 游戏内「社区浏览」暂未收录本 mod，请使用上面的手动导入方式。
