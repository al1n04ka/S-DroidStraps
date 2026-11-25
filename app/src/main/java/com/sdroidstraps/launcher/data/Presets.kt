package com.sdroidstraps.launcher.data

const val FPS_FLAG = "DFIntTaskSchedulerTargetFps"

val ALLOWLISTED: Set<String> = setOf(
    "DFIntCSGLevelOfDetailSwitchingDistance",
    "DFIntCSGLevelOfDetailSwitchingDistanceL12",
    "DFIntCSGLevelOfDetailSwitchingDistanceL23",
    "DFIntCSGLevelOfDetailSwitchingDistanceL34",
    "FFlagHandleAltEnterFullscreenManually",
    "DFFlagTextureQualityOverrideEnabled",
    "DFIntTextureQualityOverride",
    "FIntDebugForceMSAASamples",
    "DFFlagDisableDPIScale",
    "FFlagDebugGraphicsPreferD3D11",
    "FFlagDebugSkyGray",
    "DFFlagDebugPauseVoxelizer",
    "DFIntDebugFRMQualityLevelOverride",
    "FIntFRMMaxGrassDistance",
    "FIntFRMMinGrassDistance",
    "FFlagDebugGraphicsPreferVulkan",
    "FFlagDebugGraphicsPreferOpenGL",
    "FIntGrassMovementReducedMotionFactor"
)

data class Preset(
    val id: String,
    val title: String,
    val description: String,
    val flags: Map<String, String>
)

val PRESETS: List<Preset> = listOf(
    Preset(
        id = "default",
        title = "Default",
        description = "Empty set",
        flags = emptyMap()
    ),
    Preset(
        id = "performance",
        title = "Performance",
        description = "Gray sky, low textures, no grass or post effects",
        flags = mapOf(
            "FFlagDebugSkyGray" to "True",
            "DFFlagTextureQualityOverrideEnabled" to "True",
            "DFIntTextureQualityOverride" to "1",
            "FIntDebugForceMSAASamples" to "1",
            "DFIntDebugFRMQualityLevelOverride" to "1",
            "FIntFRMMaxGrassDistance" to "0",
            "FIntFRMMinGrassDistance" to "0",
            "DFFlagDebugPauseVoxelizer" to "True",
            "FIntGrassMovementReducedMotionFactor" to "100",
            "DFIntCSGLevelOfDetailSwitchingDistance" to "40",
            "DFIntCSGLevelOfDetailSwitchingDistanceL12" to "40",
            "DFIntCSGLevelOfDetailSwitchingDistanceL23" to "40",
            "DFIntCSGLevelOfDetailSwitchingDistanceL34" to "40"
        )
    ),
    Preset(
        id = "experimental",
        title = "Experimental",
        description = "Not on the allowlist — written anyway, Roblox may ignore",
        flags = mapOf(
            "FFlagDisablePostFx" to "True",
            "FIntRenderShadowIntensity" to "0"
        )
    ),
    Preset(
        id = "fps60",
        title = "FPS 60",
        description = "Frame cap, experimental flag",
        flags = mapOf(FPS_FLAG to "60")
    ),
    Preset(
        id = "fps90",
        title = "FPS 90",
        description = "Frame cap, experimental flag",
        flags = mapOf(FPS_FLAG to "90")
    ),
    Preset(
        id = "fps120",
        title = "FPS 120",
        description = "Frame cap, experimental flag",
        flags = mapOf(FPS_FLAG to "120")
    ),
    Preset(
        id = "fps144",
        title = "FPS 144",
        description = "Frame cap, experimental flag",
        flags = mapOf(FPS_FLAG to "144")
    )
)

fun flagTag(key: String): String? = when {
    key in ALLOWLISTED -> "allowlist"
    else -> "experimental"
}
