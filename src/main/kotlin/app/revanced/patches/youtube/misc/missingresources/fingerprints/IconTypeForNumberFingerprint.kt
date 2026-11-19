package app.revanced.patches.youtube.misc.missingresources.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object IconTypeForNumberFingerprint : MethodFingerprint(
    returnType = "Larmu;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    parameters = listOf("I"),
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "b" && classDef.type == "Larmu;"
    }
)