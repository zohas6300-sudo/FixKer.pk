package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ServiceRepository(
    private val serviceRequestDao: ServiceRequestDao,
    private val professionalDao: ProfessionalDao
) {
    val allRequests: Flow<List<ServiceRequest>> = serviceRequestDao.getAllRequests()
    val allProfessionals: Flow<List<Professional>> = professionalDao.getAllProfessionals()

    suspend fun insertRequest(request: ServiceRequest): Long {
        return serviceRequestDao.insertRequest(request)
    }

    suspend fun insertProfessional(professional: Professional): Long {
        return professionalDao.insertProfessional(professional)
    }

    suspend fun deleteRequest(request: ServiceRequest) {
        serviceRequestDao.deleteRequest(request)
    }

    suspend fun deleteProfessional(professional: Professional) {
        professionalDao.deleteProfessional(professional)
    }

    suspend fun checkAndSeedProfessionals() {
        val currentProfs = allProfessionals.first()
        if (currentProfs.isEmpty()) {
            val seedList = listOf(
                Professional(
                    fullName = "Muhammad Ali",
                    phoneNumber = "+923001234567",
                    whatsAppNumber = "923001234567",
                    city = "Lahore",
                    profession = "Electrician",
                    yearsOfExperience = 10,
                    rating = 4.9f,
                    isVerified = true
                ),
                Professional(
                    fullName = "Sajid Mahmood",
                    phoneNumber = "+923129876543",
                    whatsAppNumber = "923129876543",
                    city = "Karachi",
                    profession = "Plumber",
                    yearsOfExperience = 8,
                    rating = 4.8f,
                    isVerified = true
                ),
                Professional(
                    fullName = "Tariq Javed",
                    phoneNumber = "+923334567890",
                    whatsAppNumber = "923334567890",
                    city = "Islamabad",
                    profession = "Carpenter",
                    yearsOfExperience = 12,
                    rating = 4.9f,
                    isVerified = true
                ),
                Professional(
                    fullName = "Kamran Shah",
                    phoneNumber = "+923215551234",
                    whatsAppNumber = "923215551234",
                    city = "Rawalpindi",
                    profession = "Solar Installer",
                    yearsOfExperience = 6,
                    rating = 4.7f,
                    isVerified = true
                ),
                Professional(
                    fullName = "Zain-ul-Abidin",
                    phoneNumber = "+923456781234",
                    whatsAppNumber = "923456781234",
                    city = "Multan",
                    profession = "AC Technician",
                    yearsOfExperience = 7,
                    rating = 4.8f,
                    isVerified = true
                ),
                Professional(
                    fullName = "Abdul Rehman",
                    phoneNumber = "+923157778899",
                    whatsAppNumber = "923157778899",
                    city = "Faisalabad",
                    profession = "Welder",
                    yearsOfExperience = 9,
                    rating = 4.6f,
                    isVerified = true
                ),
                Professional(
                    fullName = "Imran Malik",
                    phoneNumber = "+923014441122",
                    whatsAppNumber = "923014441122",
                    city = "Peshawar",
                    profession = "Painter",
                    yearsOfExperience = 5,
                    rating = 4.8f,
                    isVerified = true
                ),
                Professional(
                    fullName = "Samiullah Khan",
                    phoneNumber = "+923249990011",
                    whatsAppNumber = "923249990011",
                    city = "Gujranwala",
                    profession = "Handyman",
                    yearsOfExperience = 11,
                    rating = 4.9f,
                    isVerified = true
                )
            )
            professionalDao.insertProfessionals(seedList)
        }
    }
}
