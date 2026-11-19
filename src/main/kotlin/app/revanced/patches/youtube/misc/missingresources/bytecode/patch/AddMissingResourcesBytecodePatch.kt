package app.revanced.patches.youtube.misc.missingresources.bytecode.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.misc.missingresources.fingerprints.IconTypeForNumberFingerprint
import app.revanced.patches.youtube.misc.missingresources.fingerprints.NavigationBarGetDrawableFingerprint
import app.revanced.shared.annotation.YouTubeCompatibility
import app.revanced.shared.extensions.exception

@Name("add-missing-resources-bytecode-patch")
@YouTubeCompatibility
class AddMissingResourcesBytecodePatch : BytecodePatch(
    listOf(
        NavigationBarGetDrawableFingerprint,
        IconTypeForNumberFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        // 1. Replace missing 0 drawable IDs with 16.40.36's transparent drawable (0x7f080115)
        NavigationBarGetDrawableFingerprint.result?.mutableMethod?.apply {
            addInstructionsWithLabels(
                0,
                """
                    if-nez p1, :original
                    const p1, 0x7f080115
                """,
                ExternalLabel("original", getInstruction(0))
            )
        } ?: throw NavigationBarGetDrawableFingerprint.exception

        // 2. Remap server-sent Cairo IconType numbers to 16.40.36's native 2021 armu enum IDs
        IconTypeForNumberFingerprint.result?.mutableMethod?.apply {
            addInstructionsWithLabels(
                0,
                """
                    sparse-switch p0, :cairo_switch
                    goto :original

                    :cairo_switch
                    .sparse-switch
                        0x484 -> :tab_activity       # 1156 TAB_ACTIVITY_CAIRO -> 355 TAB_ACTIVITY (Bell)
                        0x488 -> :search             # 1160 SEARCH_CAIRO -> 60 SEARCH
                        0x489 -> :creation_tab       # 1161 CREATION_TAB_LARGE_CAIRO -> 734 CREATION_TAB_LARGE
                        0x48a -> :settings           # 1162 SETTINGS_CAIRO -> 44 SETTINGS
                        0x4a1 -> :notifications      # 1185 NOTIFICATIONS_CAIRO -> 264 NOTIFICATIONS
                        0x49b -> :tab_home           # 1179 TAB_HOME_CAIRO -> 65 TAB_HOME
                        0x49c -> :tab_explore        # 1180 TAB_SHORTS_CAIRO -> 292 TAB_EXPLORE (Compass)
                        0x49d -> :tab_subscriptions  # 1181 TAB_SUBSCRIPTIONS_CAIRO -> 66 TAB_SUBSCRIPTIONS
                        0x49e -> :tab_library        # 1182 TAB_LIBRARY/YOU_CAIRO -> 68 TAB_LIBRARY
                    .end sparse-switch

                    :tab_activity
                    const/16 p0, 0x163
                    goto :original

                    :search
                    const/16 p0, 0x3c
                    goto :original

                    :creation_tab
                    const/16 p0, 0x2de
                    goto :original

                    :settings
                    const/16 p0, 0x2c
                    goto :original

                    :notifications
                    const/16 p0, 0x108
                    goto :original

                    :tab_home
                    const/16 p0, 0x41
                    goto :original

                    :tab_explore
                    const/16 p0, 0x124
                    goto :original

                    :tab_subscriptions
                    const/16 p0, 0x42
                    goto :original

                    :tab_library
                    const/16 p0, 0x44
                    goto :original
                """,
                ExternalLabel("original", getInstruction(0))
            )
        } ?: throw IconTypeForNumberFingerprint.exception
    }
}