package com.dark.delhi
data class DatingUser(
    val id: String = "",
    val name: String = "",
    val age: Int = 0,
    val distance: String = "",
    val bio: String = "",
    val images: List<String> = emptyList(), // Multiple Photos
    val favSpot: String = "", // Delhi Specific
    val metroLine: String = "",
    val interests: List<String> = emptyList(),
    val isPrivate: Boolean = false
)