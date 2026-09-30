package com.aistudio.richfarmaciw.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val version: Int = 1,
    val profiles: List<Profile> = emptyList(),
    val medications: List<Medication> = emptyList(),
    val sentRequests: List<SentRequest> = emptyList(),
    val donationCount: Int = 0,
    val onboardingShown: Boolean = true,
    val disclaimerAccepted: Boolean = true
)
