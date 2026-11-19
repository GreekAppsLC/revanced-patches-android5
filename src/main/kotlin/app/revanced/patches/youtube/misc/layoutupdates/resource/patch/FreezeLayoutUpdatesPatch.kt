package app.revanced.patches.youtube.misc.layoutupdates.resource.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.misc.layoutupdates.bytecode.patch.FreezeLayoutUpdatesBytecodePatch
import app.revanced.patches.youtube.misc.settings.resource.patch.SettingsPatch
import app.revanced.shared.annotation.YouTubeCompatibility
import app.revanced.shared.util.resources.ResourceHelper

@Patch
@Name("freeze-layout-updates")
@Description("Freezes server-side layout updates.")
@DependsOn([FreezeLayoutUpdatesBytecodePatch::class, SettingsPatch::class])
@YouTubeCompatibility
class FreezeLayoutUpdatesPatch : ResourcePatch {
    override fun execute(context: ResourceContext) {
        ResourceHelper.patchSuccess(context, "freeze-layout-updates")
    }
}