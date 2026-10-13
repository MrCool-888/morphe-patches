package hoodles.morphe.patches.smartlauncher.widgets

import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction3rc
import hoodles.morphe.compatibility.Compat
import hoodles.morphe.patches.smartlauncher.shared.signature.disableSignatureCheckPatch

private val widgetExtensionPatch = bytecodePatch {
    extendWith("extensions/smartlauncher.mpe")
}

@Suppress("unused")
val fixBudsWidgetPatch = bytecodePatch(
    name = "Fix Galaxy Buds3 Pro widget",
    description = "Fixes widget configuration returning cancelled on Samsung One UI 8.5 and later."
) {
    compatibleWith(Compat.SMART_LAUNCHER)
    dependsOn(widgetExtensionPatch, disableSignatureCheckPatch)

    execute {
        listOf(WidgetConfigurationFingerprint, WidgetReconfigurationFingerprint).forEach { fingerprint ->
            val match = fingerprint.instructionMatches.single()
            val instruction = match.getInstruction<Instruction3rc>()
            if (instruction.registerCount != 6) {
                throw PatchException("Unexpected widget configuration arguments")
            }
            val first = instruction.startRegister
            val last = first + instruction.registerCount - 1
            fingerprint.method.replaceInstruction(
                match.index,
                "invoke-static/range {v$first .. v$last}, " +
                    "Lhoodles/morphe/extension/smartlauncher/WidgetConfiguration;" +
                    "->startConfiguration(Landroid/appwidget/AppWidgetHost;" +
                    "Landroid/app/Activity;IIILandroid/os/Bundle;)V"
            )
        }
    }
}
