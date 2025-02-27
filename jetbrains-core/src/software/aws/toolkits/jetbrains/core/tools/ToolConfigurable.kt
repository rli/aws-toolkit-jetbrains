// Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.core.tools

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.util.ClearableLazyValue
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.applyToComponent
import com.intellij.ui.layout.panel
import software.aws.toolkits.jetbrains.ui.ValidatingPanel
import software.aws.toolkits.resources.message
import java.nio.file.Path

class ToolConfigurable : BoundConfigurable(message("executableCommon.configurable.title")), SearchableConfigurable {
    private val settings = ToolSettings.getInstance()
    private val manager = ToolManager.getInstance()
    private val panel = ClearableLazyValue.create {
        ValidatingPanel(
            disposable ?: throw RuntimeException("Should never happen as `createPanel` is called after disposable is assigned"),
            panel {
                ToolType.EP_NAME.extensionList.forEach { toolType ->
                    row(toolType.displayName) {
                        textFieldWithBrowseButton(
                            getter = { settings.getExecutablePath(toolType) ?: "" },
                            setter = { settings.setExecutablePath(toolType, it.takeIf { v -> v.isNotBlank() }) },
                            fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
                        ).withValidationOnApply {
                            it.textField.text.takeIf { t -> t.isNotBlank() }?.let { path ->
                                manager.validateCompatability(Path.of(path), toolType).toValidationInfo(toolType, component)
                            }
                        }.applyToComponent {
                            setEmptyText(toolType, textField as JBTextField)
                        }.constraints(
                            growX,
                            pushX
                        )

                        browserLink(message("aws.settings.learn_more"), toolType.documentationUrl())
                    }
                }
            },
            emptyMap()
        )
    }

    override fun createPanel() = panel.value.contentPanel

    override fun apply() {
        panel.value.apply()
    }

    override fun disposeUIResources() {
        panel.drop()
    }

    override fun getId(): String = "aws.tools"

    private fun setEmptyText(toolType: ToolType<Version>, field: JBTextField) {
        val resolved = (toolType as? AutoDetectableToolType<*>)?.resolve()
        field.emptyText.text = when {
            resolved != null && toolType.getTool()?.path == resolved -> message("executableCommon.auto_resolved", resolved)
            toolType is ManagedToolType<*> -> message("executableCommon.auto_managed")
            else -> message("common.none")
        }
    }
}
