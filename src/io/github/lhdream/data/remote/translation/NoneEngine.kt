package io.github.lhdream.data.remote.translation

import io.github.lhdream.domain.translation.TranslationEngine

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
