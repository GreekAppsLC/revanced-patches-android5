package app.revanced.patches.youtube.misc.layoutupdates.bytecode.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.misc.layoutupdates.fingerprints.ConfigPreferenceFingerprint
import app.revanced.shared.annotation.YouTubeCompatibility
import app.revanced.shared.extensions.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

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

        result.mutableMethod.apply {
            // Find the exact MOVE_RESULT_OBJECT instruction after each getString(...) call
            fun findMoveResultAfter(startIndex: Int): Pair<Int, Int> {
                for (i in startIndex until implementation!!.instructions.size) {
                    val insn = getInstruction(i)
                    if (insn.opcode == Opcode.MOVE_RESULT_OBJECT) {
                        val reg = (insn as OneRegisterInstruction).registerA
                        return Pair(i + 1, reg)
                    }
                }
                throw IllegalStateException("Could not find MOVE_RESULT_OBJECT after index $startIndex")
            }

            val (coldConfigInsert, coldConfigReg) = findMoveResultAfter(matches[0].index)
            val (coldHashInsert, coldHashReg) = findMoveResultAfter(matches[1].index)
            val (hotConfigInsert, hotConfigReg) = findMoveResultAfter(matches[2].index)
            val (hotHashInsert, hotHashReg) = findMoveResultAfter(matches[3].index)

            // Insert from bottom-most index to top-most index so earlier indices stay valid
            addInstructions(
                hotHashInsert,
                """
                    invoke-static {v$hotHashReg}, $INTEGRATIONS_CLASS_DESCRIPTOR->getHotHashData(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$hotHashReg
                """
            )
            addInstructions(
                hotConfigInsert,
                """
                    invoke-static {v$hotConfigReg}, $INTEGRATIONS_CLASS_DESCRIPTOR->getHotConfigGroup(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$hotConfigReg
                """
            )
            addInstructions(
                coldHashInsert,
                """
                    invoke-static {v$coldHashReg}, $INTEGRATIONS_CLASS_DESCRIPTOR->getColdHashData(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$coldHashReg
                """
            )
            addInstructions(
                coldConfigInsert,
                """
                    invoke-static {v$coldConfigReg}, $INTEGRATIONS_CLASS_DESCRIPTOR->getColdConfigGroup(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$coldConfigReg
                """
            )
        }
    }
}