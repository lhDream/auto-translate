package io.github.lhdream.core

import arc.Core
import arc.util.Log
import io.github.lhdream.engines.AzureEngine
import io.github.lhdream.engines.DeepLEngine
import io.github.lhdream.engines.GoogleEngine
import io.github.lhdream.engines.MicrosoftFreeEngine
import io.github.lhdream.engines.NoneEngine

object TranslationManager {
    val engines = mutableMapOf<String, TranslationEngine>()
    private lateinit var activeEngine: TranslationEngine

    // 目标语言也可以从设置中读取
    val mainLanguage: String
        get() = Core.settings.getString("translation-main-lang", "zh")

    // 目标语言也可以从设置中读取
    val targetLanguage: String
        get() = Core.settings.getString("translation-target-lang", "en")

    fun init() {
        // 注册所有可用的引擎
        register(NoneEngine)
        register(MicrosoftFreeEngine)
        register(GoogleEngine)
        register(DeepLEngine)
        register(AzureEngine)

        // 从设置中加载用户选择的引擎，默认为 "none"
        val selectedEngineId = Core.settings.getString("default-translator", MicrosoftFreeEngine.id)
        activeEngine = engines[selectedEngineId] ?: NoneEngine // 如果找不到，则使用 NoneEngine
        Log.info("[AutoTranslate] 当前翻译引擎: ${activeEngine.displayName}")
    }

    private fun register(engine: TranslationEngine) {
        engines[engine.id] = engine
    }

    fun getAvailableEngines(): List<TranslationEngine> {
        return engines.values.toList()
    }

    fun setActiveEngine(engineId: String) {
        this.activeEngine = engines[engineId] ?: NoneEngine
        Core.settings.put("default-translator", engineId)
        Log.info("[AutoTranslate] 翻译引擎已切换为: ${activeEngine.displayName}")
    }

    fun translate(text: String, targetLanguage: String = this.mainLanguage): String {
        // 如果当前引擎是 NoneEngine 或未配置，则不翻译
        if (activeEngine.id == NoneEngine.id || !activeEngine.isConfigured()) {
            if (!activeEngine.isConfigured() && activeEngine.id != "none") {
                Log.warn("[AutoTranslate] 引擎 '${activeEngine.displayName}' 未配置 API Key，跳过翻译。")
            }
            return text
        }
        // 调用当前激活引擎的翻译方法
        return activeEngine.translate(text, targetLanguage)
    }
}
