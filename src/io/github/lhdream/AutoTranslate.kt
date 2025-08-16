package io.github.lhdream

import arc.Core
import arc.Events
import arc.scene.ui.TextField
import arc.scene.ui.layout.Table
import arc.util.*
import io.github.lhdream.core.TranslationManager
import io.github.lhdream.ui.EngineDialog
//import io.github.lhdream.utils.SecurityProviderManager
import mindustry.Vars
import mindustry.game.EventType
import mindustry.game.EventType.ClientLoadEvent
import mindustry.gen.Player
import mindustry.mod.Mod
import mindustry.ui.Styles


class AutoTranslate: Mod() {

    init{
        Log.info("Loaded auto-translate mod.")
    }

    override fun init() {
        if (Vars.headless) return
        System.setProperty("https.protocols", "TLSv1.2,TLSv1.3")

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
        Threads.daemon("translator-${sender.id}") {
            try {
                // 使用管理器进行翻译
                val translatedText = TranslationManager.translate(originalMessage)

                Core.app.post {
                    val formattedMessage = "${sender.name} [#80b4ff][翻][white]: $translatedText"
                    Vars.player.sendMessage(formattedMessage)
                }
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
            val currentEngineId = Core.settings.getString("default-translator", "none")
            val engineButton = table.button(currentEngineId, Styles.flatt) {}
                .width(220f).get()

            engineButton.clicked {
                EngineDialog{
                    engineButton.setText(Core.settings.getString("default-translator", "none"))
                    engineButton.draw()
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

        }
    }

    private fun buildChatListener(){
        val javaClass = Vars.ui.chatfrag.javaClass
        val nameField = javaClass.getDeclaredField("chatfield")
        nameField.isAccessible = true
        val chatfield = nameField.get(Vars.ui.chatfrag) as TextField
        chatfield.setTextFieldListener { textField, c ->
            val text = textField.text
            if(text.length > 3 && text.endsWith("   ")){
                Threads.daemon("translator-local" + System.currentTimeMillis()) {
                    val translateText = TranslationManager.translate(text.trim(), TranslationManager.targetLanguage)
                    Core.app.post {
                        textField.text = translateText
                        Vars.ui.chatfrag.updateCursor()
                    }
                }

            }
        }
    }

}
