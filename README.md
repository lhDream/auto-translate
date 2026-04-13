# Mindustry Chat Translator (Mindustry 聊天翻译器)

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/lhDream/auto-translate?style=for-the-badge)](https://github.com/lhDream/auto-translate/releases)
[![GitHub last commit](https://img.shields.io/github/last-commit/lhDream/auto-translate?style=for-the-badge)](https://github.com/lhDream/auto-translate/commits/main)
[![GitHub license](https://img.shields.io/github/license/lhDream/auto-translate?style=for-the-badge)](./LICENSE)

<!-- [English](./README.en.md) | **简体中文** -->

---

这是一款为 **Mindustry** 设计的游戏内聊天实时翻译 Mod。它旨在打破语言壁垒，让您能与来自世界各地的玩家顺畅沟通。当服务器内的其他玩家使用您不熟悉的语言发言时，此 Mod 将会自动将其翻译成您设定的目标语言。

## ✨ 功能特性

*   **实时翻译**: 自动翻译游戏内其他玩家发送的公共聊天信息。
*   **本地翻译**: 在聊天框输入消息后，末尾加上三个空格即可触发翻译，将您输入的消息翻译为目标语言后再发送。
*   **多引擎支持**: 内置 8 种翻译引擎，涵盖免费方案和主流商业翻译服务。
*   **异步处理**: 翻译请求在后台线程执行，不阻塞游戏主线程。
*   **TLS 兼容**: 对旧版 Java 环境提供 TLS 1.2/1.3 支持，确保网络请求正常。
*   **跨平台**: 支持 Mindustry 桌面版和安卓版。

## 🚀 安装与使用

我们提供两种安装方式：

### 方法一：通过游戏内 Mod 浏览器 (推荐)

1.  打开 Mindustry -> `设置` -> `游戏` -> `语言`，确保你的语言设置正确。
2.  返回主菜单，选择 `Mod` -> `社区浏览`。
3.  在搜索框中输入 `auto-translate` 并搜索。
4.  找到本 Mod 后，点击 `安装`。
5.  安装完成后，重启游戏即可生效。

### 方法二：手动安装

1.  前往本项目的 [**Releases**](https://github.com/lhDream/auto-translate/releases) 页面。
2.  下载最新版本的 `.jar` 文件。
3.  打开 Mindustry -> `Mod` -> `导入` -> `选择文件`，选择你刚刚下载的`.jar`文件。
4.  找到本 Mod 后, 点击 `确定` 进行安装。
5.  重启游戏即可生效。

## 🔧 配置

安装并启用 Mod 后，您可以在游戏的 `设置` -> `Mod 设置` -> `自动翻译设置` 中找到本 Mod 的专属配置页面。

### 基本设置

| 设置项 | 说明 | 默认值 |
|--------|------|--------|
| 翻译引擎 | 从列表中选择翻译服务 | Microsoft Free |
| 主要语言 | 接收到的消息翻译为此语言 | `zh` |
| 目标语言 | 本地翻译（三个空格触发）的目标语言 | `en` |

### API 密钥设置

| 设置项 | 对应引擎 | 说明 |
|--------|----------|------|
| Google API Key | Google 翻译 | [Google Cloud Translation API](https://cloud.google.com/translate) 密钥 |
| DeepL API Key | DeepL | [DeepL API](https://www.deepl.com/pro-api) 密钥 |
| Azure API Key | Azure | [Azure Translator](https://azure.microsoft.com/services/cognitive-services/translator/) 密钥 |
| Baidu APP ID | 百度翻译 | [百度翻译开放平台](https://fanyi-api.baidu.com/) 应用 ID |
| Baidu API Key | 百度翻译 | 百度翻译 API 密钥 |
| Tencent SecretId | 腾讯翻译 | [腾讯云机器翻译](https://cloud.tencent.com/product/tmt) 密钥 ID |
| Tencent SecretKey | 腾讯翻译 | 腾讯云 API 密钥 |
| Tencent Region | 腾讯翻译 | 腾讯云地域 | `ap-guangzhou` |
| OpenAI API Key | OpenAI | OpenAI API 密钥 |
| OpenAI Base URL | OpenAI | API 地址 | `https://api.openai.com/v1/` |
| OpenAI Model | OpenAI | 模型名称 | `gpt-4o-mini` |

## 🌐 支持的翻译引擎

| 引擎 | 需要配置 | 说明 |
|------|---------|------|
| **None** | 无 | 不进行翻译，关闭翻译功能 |
| **Microsoft Free** | 无 | 使用微软 Edge 翻译接口，**免费且无需 API Key**，开箱即用 |
| **Google 翻译** | API Key | Google Cloud Translation API |
| **DeepL** | API Key | DeepL Translation API |
| **Azure** | API Key | Azure Cognitive Services Translator |
| **百度翻译** | APP ID + API Key | 百度翻译开放平台 API |
| **腾讯翻译** | SecretId + SecretKey | 腾讯云机器翻译 API |
| **OpenAI** | API Key | OpenAI GPT 翻译，支持自定义 Base URL（兼容第三方 API）和模型名称 |

> 💡 **提示**: 如果您只需要免费使用，选择 **Microsoft Free** 引擎即可，无需任何配置。它作为默认引擎，安装后即可直接使用。

## 🗺️ 路线图 (Roadmap)

*   [ ] 提供本地化 UI，让 Mod 设置界面支持多语言。
*   [ ] 持续优化性能，减少网络延迟。

## 🤝 如何贡献

我们非常欢迎社区的贡献！无论您是提交 Bug、提出功能建议还是贡献代码。

*   **报告 Bug**: 如果您在使用中遇到任何问题，请通过 [**Issues**](https://github.com/lhDream/auto-translate/issues) 详细描述您的问题。
*   **功能建议**: 有什么好点子吗？同样可以通过 [**Issues**](https://github.com/lhDream/auto-translate/issues) 告诉我们。
*   **代码贡献**: 如果您想直接参与开发，请遵循以下步骤：
    1.  Fork [**lhDream/auto-translate**](https://github.com/lhDream/auto-translate) 仓库。
    2.  克隆你 Fork 的仓库 (`git clone https://github.com/<your-username>/auto-translate.git`)。
    3.  创建特性分支 (`git checkout -b feature/your-feature`)。
    4.  提交更改 (`git commit -m 'Add your feature'`)。
    5.  推送到你的 Fork (`git push origin feature/your-feature`)。
    6.  在 [**lhDream/auto-translate**](https://github.com/lhDream/auto-translate/pulls) 创建 Pull Request。

## 📄 许可证 (License)

本项目采用 [MIT](LICENSE) 许可证。详情请参阅 `LICENSE` 文件。

## 🙏 致谢

*   **Anuke** - 创作了 Mindustry 这款伟大的游戏。
*   **所有贡献者和用户** - 感谢你们的支持！
