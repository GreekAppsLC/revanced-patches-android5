package app.revanced.patches.youtube.misc.layoutupdates.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object ConfigPreferenceFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = emptyList(),
    strings = listOf(
        "com.google.android.libraries.youtube.innertube.cold_config_group",
        "com.google.android.libraries.youtube.innertube.cold_hash_data",
        "com.google.android.libraries.youtube.innertube.hot_config_group",
        "com.google.android.libraries.youtube.innertube.hot_hash_data"
    ),
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "run" && classDef.type == "Laauq;"
    }
)