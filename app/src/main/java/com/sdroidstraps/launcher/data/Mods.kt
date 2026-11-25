package com.sdroidstraps.launcher.data

data class ClientMod(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val flags: Map<String, String>
)

val MODS: List<ClientMod> = listOf(
    ClientMod(
        id = "sky_gray",
        title = "Gray sky",
        description = "Flat gray skybox without stars or atmosphere",
        category = "Visuals",
        flags = mapOf("FFlagDebugSkyGray" to "True")
    ),
    ClientMod(
        id = "no_postfx",
        title = "Disable post-processing",
        description = "Skips bloom, color correction and other post effects",
        category = "Visuals",
        flags = mapOf("FFlagDisablePostFx" to "True")
    ),
    ClientMod(
        id = "flat_shadows",
        title = "Flat shadows",
        description = "Renders shadows at minimal intensity",
        category = "Visuals",
        flags = mapOf("FIntRenderShadowIntensity" to "0")
    ),
    ClientMod(
        id = "no_grass",
        title = "Remove grass",
        description = "Disables terrain grass rendering",
        category = "Performance",
        flags = mapOf(
            "FIntFRMMaxGrassDistance" to "0",
            "FIntFRMMinGrassDistance" to "0"
        )
    ),
    ClientMod(
        id = "low_textures",
        title = "Low texture quality",
        description = "Forces the lowest texture quality level",
        category = "Performance",
        flags = mapOf(
            "DFFlagTextureQualityOverrideEnabled" to "True",
            "DFIntTextureQualityOverride" to "1"
        )
    ),
    ClientMod(
        id = "no_msaa",
        title = "Disable anti-aliasing",
        description = "Forces a single MSAA sample",
        category = "Performance",
        flags = mapOf("FIntDebugForceMSAASamples" to "1")
    ),
    ClientMod(
        id = "low_quality",
        title = "Low render quality",
        description = "Overrides the render quality level to 1",
        category = "Performance",
        flags = mapOf("DFIntDebugFRMQualityLevelOverride" to "1")
    ),
    ClientMod(
        id = "lod",
        title = "Aggressive CSG LOD",
        description = "Culls CSG geometry sooner at every LOD distance",
        category = "Performance",
        flags = mapOf(
            "DFIntCSGLevelOfDetailSwitchingDistance" to "40",
            "DFIntCSGLevelOfDetailSwitchingDistanceL12" to "40",
            "DFIntCSGLevelOfDetailSwitchingDistanceL23" to "40",
            "DFIntCSGLevelOfDetailSwitchingDistanceL34" to "40"
        )
    ),
    ClientMod(
        id = "grass_motion",
        title = "Reduce grass motion",
        description = "Accessibility factor that calms grass swaying",
        category = "Performance",
        flags = mapOf("FIntGrassMovementReducedMotionFactor" to "100")
    ),
    ClientMod(
        id = "fps_240",
        title = "Unlock FPS (240)",
        description = "Raises the frame cap; Roblox may ignore it",
        category = "Experimental",
        flags = mapOf(FPS_FLAG to "240")
    )
)

fun isModEnabled(flags: Map<String, String>, mod: ClientMod): Boolean =
    mod.flags.all { (key, value) -> flags[key] == value }

fun isModExperimental(mod: ClientMod): Boolean =
    mod.flags.keys.any { it !in ALLOWLISTED }
