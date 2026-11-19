package app.revanced.patches.youtube.misc.layoutupdates.bytecode.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.misc.layoutupdates.fingerprints.ConfigPreferenceFingerprint
import app.revanced.shared.annotation.YouTubeCompatibility
import app.revanced.shared.extensions.exception

@Name("freeze-layout-updates-bytecode-patch")
@YouTubeCompatibility
class FreezeLayoutUpdatesBytecodePatch : BytecodePatch(
    listOf(ConfigPreferenceFingerprint)
) {
    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/utils/FreezeLayoutUpdatesPatch;"
    }

    override fun execute(context: BytecodeContext) {
        val result = ConfigPreferenceFingerprint.result
            ?: throw ConfigPreferenceFingerprint.exception

        val matches = result.scanResult.stringsScanResult!!.matches
        val coldConfigIndex = matches[0].index + 3
        val coldHashIndex = matches[1].index + 3
        val hotConfigIndex = matches[2].index + 2
        val hotHashIndex = matches[3].index + 2

        result.mutableMethod.apply {
            addInstructions(
                hotHashIndex + 1,
                """
                    invoke-static {v0}, $INTEGRATIONS_CLASS_DESCRIPTOR->getHotHashData(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v0
                """
            )
            addInstructions(
                hotConfigIndex + 1,
                """
                    invoke-static {v1}, $INTEGRATIONS_CLASS_DESCRIPTOR->getHotConfigGroup(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v1
                """
            )
            addInstructions(
                coldHashIndex + 1,
                """
                    invoke-static {v3}, $INTEGRATIONS_CLASS_DESCRIPTOR->getColdHashData(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v3
                """
            )
            addInstructions(
                coldConfigIndex + 1,
                """
                    invoke-static {v4}, $INTEGRATIONS_CLASS_DESCRIPTOR->getColdConfigGroup(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v4
                """
            )
        }
    }
}