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
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

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
            fun findGetStringResultAfter(constStringIndex: Int): Pair<Int, Int> {
                val keyReg = (getInstruction(constStringIndex) as OneRegisterInstruction).registerA
                for (i in (constStringIndex + 1) until implementation!!.instructions.size) {
                    val insn = getInstruction(i)
                    if (insn.opcode == Opcode.INVOKE_INTERFACE) {
                        val methodRef = (insn as ReferenceInstruction).reference as MethodReference
                        val fiveReg = insn as FiveRegisterInstruction
                        if (methodRef.name == "getString" && fiveReg.registerD == keyReg) {
                            val moveResult = getInstruction(i + 1)
                            val resultReg = (moveResult as OneRegisterInstruction).registerA
                            return Pair(i + 2, resultReg)
                        }
                    }
                }
                throw IllegalStateException("Could not find SharedPreferences.getString for register v$keyReg")
            }

            // matches[0..3] follow the exact strings order in ConfigPreferenceFingerprint:
            // [0] cold_config_group, [1] cold_hash_data, [2] hot_config_group, [3] hot_hash_data
            val (coldConfigInsert, coldConfigReg) = findGetStringResultAfter(matches[0].index)
            val (coldHashInsert, coldHashReg) = findGetStringResultAfter(matches[1].index)
            val (hotConfigInsert, hotConfigReg) = findGetStringResultAfter(matches[2].index)
            val (hotHashInsert, hotHashReg) = findGetStringResultAfter(matches[3].index)

            val insertions = listOf(
                Triple(coldConfigInsert, coldConfigReg, "getColdConfigGroup"),
                Triple(coldHashInsert, coldHashReg, "getColdHashData"),
                Triple(hotConfigInsert, hotConfigReg, "getHotConfigGroup"),
                Triple(hotHashInsert, hotHashReg, "getHotHashData")
            ).sortedByDescending { it.first }

            for ((insertIndex, reg, methodName) in insertions) {
                addInstructions(
                    insertIndex,
                    """
                        invoke-static {v$reg}, $INTEGRATIONS_CLASS_DESCRIPTOR->$methodName(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$reg
                    """
                )
            }
        }
    }
}