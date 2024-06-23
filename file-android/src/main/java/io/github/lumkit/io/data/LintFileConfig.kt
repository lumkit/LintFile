package io.github.lumkit.io.data

import kotlinx.serialization.Serializable

@Serializable
data class LintFileConfig(
    val ioModel: IoModel = IoModel.NORMAL
)
