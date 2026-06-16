package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_requests")
data class ServiceRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val mobileNumber: String,
    val city: String,
    val area: String,
    val requiredService: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Pending Match" // "Pending Match", "Connected", "Done"
)

@Entity(tableName = "professionals")
data class Professional(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val phoneNumber: String,
    val whatsAppNumber: String,
    val city: String,
    val profession: String, // "Electrician", "Plumber", etc.
    val yearsOfExperience: Int,
    val rating: Float = 4.8f,
    val photoUrl: String = "", // Placeholders or empty string for offline profiling
    val isVerified: Boolean = true,
    val status: String = "Active", // "Active", "Vetting", "Suspended"
    val timestamp: Long = System.currentTimeMillis()
)
