package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceRequestDao {
    @Query("SELECT * FROM service_requests ORDER BY timestamp DESC")
    fun getAllRequests(): Flow<List<ServiceRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: ServiceRequest): Long

    @Delete
    suspend fun deleteRequest(request: ServiceRequest)

    @Query("DELETE FROM service_requests")
    suspend fun deleteAllRequests()
}

@Dao
interface ProfessionalDao {
    @Query("SELECT * FROM professionals ORDER BY rating DESC, yearsOfExperience DESC")
    fun getAllProfessionals(): Flow<List<Professional>>

    @Query("SELECT * FROM professionals WHERE city = :city ORDER BY rating DESC")
    fun getProfessionalsByCity(city: String): Flow<List<Professional>>

    @Query("SELECT * FROM professionals WHERE profession = :profession ORDER BY rating DESC")
    fun getProfessionalsByProfession(profession: String): Flow<List<Professional>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfessional(professional: Professional): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfessionals(professionals: List<Professional>)

    @Delete
    suspend fun deleteProfessional(professional: Professional)

    @Query("DELETE FROM professionals")
    suspend fun deleteAllProfessionals()
}
