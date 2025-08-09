package io.github.lhdream.core

interface TranslationEngine {

    /** 引擎的唯一标识符，用于保存在设置中。*/
    val id: String
    /** 显示在设置界面中的名称。*/
    val displayName: String
    /**
     * 检查此引擎是否已正确配置（例如，API密钥是否已设置）。
     * @return 如果配置完成则为 true。
     */
    fun isConfigured(): Boolean
    /**
     * 执行翻译的核心方法。
     * @param text 要翻译的文本。
     * @param targetLang 目标语言代码 (e.g., "zh", "en")。
     * @return 翻译后的文本。
     * @throws Exception 如果翻译失败。
     */
    @Throws(Exception::class)
    fun translate(text: String, targetLang: String): String

}