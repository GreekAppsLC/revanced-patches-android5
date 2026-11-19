package app.revanced.patches.youtube.misc.missingresources.resource.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.misc.missingresources.bytecode.patch.AddMissingResourcesBytecodePatch
import app.revanced.shared.annotation.YouTubeCompatibility
import app.revanced.shared.util.FileCopyCompat
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import org.w3c.dom.Element

@Patch
@Name("add-missing-resources")
@Description("Adds missing resources and maps newer server icons to existing 16.40.36 drawables.")
@DependsOn([AddMissingResourcesBytecodePatch::class])
@YouTubeCompatibility
class AddMissingResourcesPatch : ResourcePatch {
    override fun execute(context: ResourceContext) {
        val targetDrawable = context["res"].resolve("drawable/ic_waveform_elements.xml")
        targetDrawable.parentFile?.mkdirs()
        try {
            Files.copy(
                this.javaClass.classLoader.getResourceAsStream("youtube/addmissingresources/drawable/ic_waveform_elements.xml")!!,
                targetDrawable.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        } catch (e: NoSuchMethodError) {
            FileCopyCompat.copy(
                this.javaClass.classLoader.getResourceAsStream("youtube/addmissingresources/drawable/ic_waveform_elements.xml")!!,
                targetDrawable
            )
        }

        val drawableAliases = mapOf(
            "ic_right_like_off_shadowed" to "@drawable/ic_right_like_off_32c",
            "ic_right_like_on_shadowed" to "@drawable/ic_right_like_on_32c",
            "ic_right_dislike_off_shadowed" to "@drawable/ic_right_dislike_off_32c",
            "ic_right_dislike_on_shadowed" to "@drawable/ic_right_dislike_on_32c",
            "ic_right_comment_shadowed" to "@drawable/ic_right_comment_32c",
            "ic_right_share_shadowed" to "@drawable/ic_right_share_32c",
            "yt_outline_thumb_up_black_18" to "@drawable/yt_outline_thumb_up_black_24",
            "yt_outline_thumb_down_black_18" to "@drawable/yt_outline_thumb_down_black_24",
            "yt_fill_thumb_up_black_18" to "@drawable/yt_fill_thumb_up_black_24",
            "yt_fill_thumb_down_black_18" to "@drawable/yt_fill_thumb_down_black_24",
            "yt_fill_spark_black_24" to "@drawable/button_color_transparent_background"
        )

        context.xmlEditor["res/values/drawables.xml"].use { editor ->
            val resourcesNode = editor.file.getElementsByTagName("resources").item(0) as Element
            drawableAliases.forEach { (aliasName, targetRef) ->
                val newItem = editor.file.createElement("item")
                newItem.setAttribute("type", "drawable")
                newItem.setAttribute("name", aliasName)
                newItem.appendChild(editor.file.createTextNode(targetRef))
                resourcesNode.appendChild(newItem)
            }
        }
    }
}