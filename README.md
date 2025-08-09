# Mindustry Chat Translator (Mindustry 聊天翻译器)

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/lhDream/auto-translate?style=for-the-badge)](https://github.com/lhDream/auto-translate/releases)
[![GitHub last commit](https://img.shields.io/github/last-commit/lhDream/auto-translate?style=for-the-badge)](https://github.com/lhDream/auto-translate/commits/main)
[![GitHub license](https://img.shields.io/github/license/lhDream/auto-translate?style=for-the-badge)](./LICENSE)

<!-- [English](./README.en.md) | **简体中文** -->

---

这是一款为 **Mindustry** 设计的游戏内聊天实时翻译 Mod。它旨在打破语言壁垒，让您能与来自世界各地的玩家顺畅沟通。当服务器内的其他玩家使用您不熟悉的语言发言时，此 Mod 将会自动将其翻译成您设定的目标语言。

## ✨ 功能特性

*   **实时翻译**: 自动翻译游戏内的公共聊天信息。
*   **多引擎支持**: 内置多种主流翻译引擎，您可以根据需求和喜好自由切换。
*   **高度可定制**: 支持设置目标翻译语言、排除特定玩家或语言等。
*   **易于扩展**: 模块化的设计，便于未来添加更多新的翻译服务。
*   **低延迟**: 经过优化，力求在不影响游戏体验的前提下提供快速翻译。
<!--
## 📸 效果截图

*(在此处插入一张或多张展示 Mod 效果的截图)*

![聊天翻译效果图](https://your-image-host.com/path/to/screenshot.png)
> *一个清晰的截图能让用户快速了解 Mod 的用途。*
-->

## 🚀 安装与使用

我们提供两种安装方式：

### 方法一：通过游戏内 Mod 浏览器 (推荐)

1.  打开 Mindustry -> `设置` -> `游戏` -> `语言`，确保你的语言设置正确。
2.  返回主菜单，选择 `Mod` -> `社区浏览`。
3.  在搜索框中输入 `YOUR_MOD_NAME` (你的Mod名称) 并搜索。
4.  找到本 Mod 后，点击 `安装`。
5.  安装完成后，重启游戏即可生效。

### 方法二：手动安装

1.  前往本项目的 [**Releases**](https://github.com/lhDream/auto-translate/releases) 页面。
2.  下载最新版本的 `.jar` 文件。
3.  将下载好的 `.jar` 文件放入您的 Mindustry `mods` 文件夹。
    *   **Windows**: `%APPDATA%/Mindustry/mods/`
    *   **Linux**: `~/.config/Mindustry/mods/` or `~/.local/share/Mindustry/mods/`
    *   **macOS**: `~/Library/Application Support/Mindustry/mods/`
    *   **Android**: `内部存储/Mindustry/mods/`
4.  重启游戏即可生效。

## 🔧 配置

安装并启用 Mod 后，您可以在游戏的 `设置` -> `Mod 设置` 中找到本 Mod 的专属配置页面。

在这里，您可以:
*   **选择翻译引擎**: 从下拉列表中选择您偏好的翻译服务 (如 Google, DeepL)。
*   **设置目标语言**: 设置您希望将聊天信息翻译成的语言。
*   **配置 API 密钥**: 部分翻译服务 (如 DeepL) 可能需要您提供个人的 API Key 以获得更佳的翻译质量或更高的配额。
*   **管理黑名单**: 添加不需要翻译的玩家名称或语言代码。

## 🌐 支持的翻译引擎

我们目前支持以下翻译引擎，并将持续增加：

*   ✅ **Google 翻译**: 需在设置中填入您的 Google API Key 以获得高质量翻译。
*   ✅ **DeepL**: 需在设置中填入您的 DeepL API Key 以获得高质量翻译。
*   ☑️ **有道翻译** (计划中)
*   ☑️ **百度翻译** (计划中)

如果您希望我们支持其他翻译服务，欢迎通过 [Issues](https://github.com/lhDream/auto-translate/issues) 提出建议！

## 🗺️ 路线图 (Roadmap)

我们对这个项目有一些未来的规划：

*   [ ] 允许用户自定义翻译接口地址。
*   [ ] 提供本地化 UI，让 Mod 设置界面支持多语言。
*   [ ] 持续优化性能，减少网络延迟。

## 🤝 如何贡献

我们非常欢迎社区的贡献！无论您是提交 Bug、提出功能建议还是贡献代码。

*   **报告 Bug**: 如果您在使用中遇到任何问题，请通过 [**Issues**](https://github.com/lhDream/auto-translate/issues) 详细描述您的问题。
*   **功能建议**: 有什么好点子吗？同样可以通过 [**Issues**](https://github.com/lhDream/auto-translate/issues) 告诉我们。
*   **代码贡献**: 如果您想直接参与开发，请遵循以下步骤：
    1.  Fork 本仓库。
    2.  创建您的特性分支 (`git checkout -b feature/AmazingFeature`)。
    3.  提交您的更改 (`git commit -m 'Add some AmazingFeature'`)。
    4.  将您的分支推送到远程仓库 (`git push origin feature/AmazingFeature`)。
    5.  创建并提交一个 Pull Request。

## 📄 许可证 (License)

本项目采用 [MIT](LICENSE) 许可证。详情请参阅 `LICENSE` 文件。

## 🙏 致谢

*   **Anuke** - 创作了 Mindustry 这款伟大的游戏。
*   **所有贡献者和用户** - 感谢你们的支持！
