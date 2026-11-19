package app.revanced.patches.youtube.misc.missingresources.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object NavigationBarGetDrawableFingerprint : MethodFingerprint(
    returnType = "Landroid/graphics/drawable/Drawable;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    parameters = listOf("Landroid/content/Context;", "I"),
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "a" && classDef.type == "Laik;"
    }
)