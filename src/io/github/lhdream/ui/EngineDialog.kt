package io.github.lhdream.ui

import arc.Core
import arc.scene.ui.ButtonGroup
import arc.scene.ui.ScrollPane
import arc.scene.ui.TextButton
import arc.scene.ui.layout.Table
import io.github.lhdream.core.TranslationManager
import mindustry.ui.Styles
import mindustry.ui.dialogs.BaseDialog

class EngineDialog(
    private val func: (String) -> Unit = { }
): BaseDialog("engine") {

    init {
        addCloseButton()
        setup()
    }

    private fun setup() {
        val engines = Table()
        engines.marginRight(24f).marginLeft(24f)
        val pane = ScrollPane(engines)
        pane.setScrollingDisabled(true, false)
        val group = ButtonGroup<TextButton>()

        for (engine in TranslationManager.engines) {
            val button = TextButton(engine.key, Styles.flatTogglet)
            button.clicked {
                if (def() == engine.key) return@clicked
                Core.settings.put("default-translator", engine.key)
                TranslationManager.setActiveEngine(engine.key)
                func(engine.key)
            }
            engines.add(button)
                .group(group)
                .update { t: TextButton -> t.setChecked(t.text == def()) }
                .size(400f, 50f)
                .row()
        }

        cont.add(pane)
    }

    private fun def() = Core.settings.getString("default-translator", "None")

}