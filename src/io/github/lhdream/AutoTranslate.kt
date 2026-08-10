package io.github.lhdream

import arc.Core
import arc.Events
import arc.scene.ui.TextField
import arc.scene.ui.layout.Table
import arc.util.*
import io.github.lhdream.data.remote.translation.NoneEngine
import io.github.lhdream.domain.translation.TranslationManager
import io.github.lhdream.presentation.ui.EngineDialog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mindustry.Vars
import mindustry.game.EventType
import mindustry.game.EventType.ClientLoadEvent
import mindustry.gen.Player
import mindustry.mod.Mod
import mindustry.ui.Styles


class AutoTranslate: Mod() {

    companion object {
        private const val TRIGGER_SUFFIX = "   "
        private const val MIN_TEXT_LENGTH = 3
    }

    private val translationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var chatInputJob: Job? = null

    init{
        Log.info("Loaded auto-translate mod.")
    }

    override fun init() {
        if (Vars.headless) return
        // 初始化管理器
        TranslationManager.init()
        // 监听聊天事件
        Events.on(EventType.PlayerChatEvent::class.java) { event ->
            // 自己的消息不翻译
            if (event.player == Vars.player || Vars.player == null) return@on
            handleTranslationAsync(event.player, event.message)
        }

        // 为客户端建立设置菜单
        Events.on(ClientLoadEvent::class.java) {
            Core.app.post { buildSettingsUI() }
        }

        // 监听聊天消息框, 增加连续三个空格翻译
        buildChatListener()
    }

    private fun handleTranslationAsync(sender: Player, originalMessage: String) {
        translationScope.launch {
            try {
                val translatedText = TranslationManager.translateAsync(originalMessage)

                Core.app.post {
                    val formattedMessage = "${sender.name} [#80b4ff][翻][white]: $translatedText"
                    Vars.player.sendMessage(formattedMessage)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.err("翻译失败", e)
                Core.app.post {
                    val errorMessage = "[#ff6961][error] ${sender.name}: $originalMessage"
                    Vars.player.sendMessage(errorMessage)
                }
            }
        }
    }

    private fun buildSettingsUI() {
        Vars.ui.settings.addCategory("自动翻译设置") { table: Table ->
            table.background(Styles.black6) // 给设置区域加个背景，更美观

            table.add("翻译引擎").padLeft(10f)
            val currentEngineId = Core.settings.getString("default-translator", NoneEngine.id)
            val engineButton = table.button(currentEngineId, Styles.flatt) {}
                .width(220f).get()

            engineButton.clicked {
                EngineDialog{
                    Log.info("回调翻译引擎id ;${it}")
                    engineButton.setText(it)
                    table.pack()
                }.show()
            }

            table.row()
            // --- 目标语言设置 ---
            table.add("主要语言").padTop(8f)
            table.field(TranslationManager.mainLanguage) { lang ->
                Core.settings.put("translation-main-lang", lang.trim().lowercase())
            }.width(220f).padLeft(10f).get()
            table.row()
            // --- 目标语言设置 ---
            table.add("目标语言").padTop(8f)
            table.field(TranslationManager.targetLanguage) { lang ->
                Core.settings.put("translation-target-lang", lang.trim().lowercase())
            }.width(220f).padLeft(10f).get()
            table.row()
            // --- API Key 设置 ---
            table.add("--- API 密钥设置 ---").colspan(2).center().padTop(20f).row()

            table.add("Google API Key").padTop(8f)
            table.field(Core.settings.getString("google-api-key", ""), Styles.areaField) { key ->
                Core.settings.put("google-api-key", key.trim())
            }.width(400f).padLeft(10f).get()
            table.row()

            table.add("DeepL API Key").padTop(8f)
            table.field(Core.settings.getString("deepl-api-key", ""), Styles.areaField) { key ->
                Core.settings.put("deepl-api-key", key.trim())
            }.width(400f).padLeft(10f).get()
            table.row()

            table.add("Azure API Key").padTop(8f)
            table.field(Core.settings.getString("azure-api-key", ""), Styles.areaField) { key ->
                Core.settings.put("azure-api-key", key.trim())
            }.width(400f).padLeft(10f).get()
            table.row()

            table.add("Baidu APP ID").padTop(8f)
            table.field(Core.settings.getString("baidu-app-id", ""), Styles.areaField) { id ->
                Core.settings.put("baidu-app-id", id.trim())
            }.width(400f).padLeft(10f).get()
            table.row()

            table.add("Baidu API Key").padTop(8f)
            table.field(Core.settings.getString("baidu-api-key", ""), Styles.areaField) { key ->
                Core.settings.put("baidu-api-key", key.trim())
            }.width(400f).padLeft(10f).get()
            table.row()

            table.add("Tencent SecretId").padTop(8f)
            table.field(Core.settings.getString("tencent-secret-id", ""), Styles.areaField) { id ->
                Core.settings.put("tencent-secret-id", id.trim())
            }.width(400f).padLeft(10f).get()
            table.row()

            table.add("Tencent SecretKey").padTop(8f)
            table.field(Core.settings.getString("tencent-secret-key", ""), Styles.areaField) { key ->
                Core.settings.put("tencent-secret-key", key.trim())
            }.width(400f).padLeft(10f).get()
            table.row()

            table.add("Tencent Region").padTop(8f)
            table.field(Core.settings.getString("tencent-region", "ap-guangzhou"), Styles.areaField) { region ->
                Core.settings.put("tencent-region", region.trim())
            }.width(400f).padLeft(10f).get()
            table.row()

            table.add("OpenAI API Key").padTop(8f)
            table.field(Core.settings.getString("openai-api-key", ""), Styles.areaField) { key ->
                Core.settings.put("openai-api-key", key.trim())
            }.width(400f).padLeft(10f).get()
            table.row()

            table.add("OpenAI Base URL").padTop(8f)
            table.field(Core.settings.getString("openai-base-url", "https://api.openai.com/v1/"), Styles.areaField) { url ->
                Core.settings.put("openai-base-url", url.trim())
            }.width(400f).padLeft(10f).get()
            table.row()

            table.add("OpenAI Model").padTop(8f)
            table.field(Core.settings.getString("openai-model-name", "gpt-4o-mini"), Styles.areaField) { model ->
                Core.settings.put("openai-model-name", model.trim())
            }.width(400f).padLeft(10f).get()
            table.row()

        }
    }

    private fun buildChatListener() {
        val chatfield: TextField
        try {
            val field = Vars.ui.chatfrag.javaClass.getDeclaredField("chatfield")
            field.isAccessible = true
            chatfield = field.get(Vars.ui.chatfrag) as TextField
        } catch (e: Exception) {
            Log.warn("[AutoTranslate] 无法访问 chatfield，聊天框翻译功能已禁用。", e)
            return
        }

        chatfield.setTextFieldListener { textField, _ ->
            val text = textField.text
            if (text.length > MIN_TEXT_LENGTH && text.endsWith(TRIGGER_SUFFIX) && !text.isBlank()) {
                chatInputJob?.cancel()
                chatInputJob = translationScope.launch {
                    try {
                        val translated = TranslationManager.translateAsync(
                            text.trim(),
                            TranslationManager.targetLanguage
                        )
                        Core.app.post {
                            if (textField.text == text) {
                                textField.text = translated
                                Vars.ui.chatfrag.updateCursor()
                            }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.err("[AutoTranslate] 聊天输入翻译失败。", e)
                    }
                }
            }
        }
    }

}
