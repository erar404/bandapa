package com.bandapa.feature.band.domain

data class BandMemberWithProfile(
    val member: BandMember,
    val profile: Profile,
) {
    val displayName: String get() = profile.name?.takeIf { it.isNotBlank() }
        ?: profile.email?.substringBefore('@')
        ?: member.userId.take(8)
}
