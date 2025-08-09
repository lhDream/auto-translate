package io.github.lhdream.engines

import io.github.lhdream.core.TranslationEngine

object NoneEngine: TranslationEngine {
    override val id: String = "None"
    override val displayName: String = "None"

    override fun isConfigured(): Boolean {
        return false
    }

    override fun translate(text: String, targetLang: String): String {
        return text
    }
}