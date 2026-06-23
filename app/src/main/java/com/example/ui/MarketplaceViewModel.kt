package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Professional
import com.example.data.ServiceRepository
import com.example.data.ServiceRequest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ServiceRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ServiceRepository(db.serviceRequestDao(), db.professionalDao())
        
        // Seed professional data if empty
        viewModelScope.launch {
            repository.checkAndSeedProfessionals()
        }
    }

    // UI States
    val professionals: StateFlow<List<Professional>> = repository.allProfessionals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val serviceRequests: StateFlow<List<ServiceRequest>> = repository.allRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Form states for Customer Service Request
    private val _custName = MutableStateFlow("")
    val custName = _custName.asStateFlow()

    private val _custMobile = MutableStateFlow("")
    val custMobile = _custMobile.asStateFlow()

    private val _custCity = MutableStateFlow("Lahore")
    val custCity = _custCity.asStateFlow()

    private val _custArea = MutableStateFlow("")
    val custArea = _custArea.asStateFlow()

    private val _custService = MutableStateFlow("Electrician")
    val custService = _custService.asStateFlow()

    private val _custDescription = MutableStateFlow("")
    val custDescription = _custDescription.asStateFlow()

    private val _custLatitude = MutableStateFlow<Double?>(null)
    val custLatitude = _custLatitude.asStateFlow()

    private val _custLongitude = MutableStateFlow<Double?>(null)
    val custLongitude = _custLongitude.asStateFlow()

    private val _custMapAddress = MutableStateFlow<String?>(null)
    val custMapAddress = _custMapAddress.asStateFlow()

    // Form states for Professional Registration
    private val _proName = MutableStateFlow("")
    val proName = _proName.asStateFlow()

    private val _proPhone = MutableStateFlow("")
    val proPhone = _proPhone.asStateFlow()

    private val _proWhatsApp = MutableStateFlow("")
    val proWhatsApp = _proWhatsApp.asStateFlow()

    private val _proCity = MutableStateFlow("Lahore")
    val proCity = _proCity.asStateFlow()

    private val _proProfession = MutableStateFlow("Electrician")
    val proProfession = _proProfession.asStateFlow()

    private val _proExperience = MutableStateFlow("")
    val proExperience = _proExperience.asStateFlow()

    // Status notifications
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // Search and filtering options
    private val _selectedCityFilter = MutableStateFlow<String?>("All")
    val selectedCityFilter = _selectedCityFilter.asStateFlow()

    private val _selectedServiceFilter = MutableStateFlow<String?>("All")
    val selectedServiceFilter = _selectedServiceFilter.asStateFlow()

    // Filtered professionals list based on selected city & professional criteria
    val filteredProfessionals: StateFlow<List<Professional>> = combine(
        professionals,
        selectedCityFilter,
        selectedServiceFilter
    ) { profList, city, service ->
        profList.filter { prof ->
            val matchCity = city == "All" || city == null || prof.city.lowercase() == city.lowercase()
            val matchService = service == "All" || service == null || prof.profession.lowercase() == service.lowercase()
            matchCity && matchService
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Update Customer Form
    fun updateCustName(value: String) { _custName.value = value }
    fun updateCustMobile(value: String) { _custMobile.value = value }
    fun updateCustCity(value: String) { _custCustCity(value) }
    private fun _custCustCity(value: String) { _custCity.value = value }
    fun updateCustArea(value: String) { _custArea.value = value }
    fun updateCustService(value: String) { _custService.value = value }
    fun updateCustDescription(value: String) { _custDescription.value = value }
    fun updateCustLocation(lat: Double, lng: Double, address: String?) {
        _custLatitude.value = lat
        _custLongitude.value = lng
        _custMapAddress.value = address
    }
    fun clearCustLocation() {
        _custLatitude.value = null
        _custLongitude.value = null
        _custMapAddress.value = null
    }

    // Update Professional Form
    fun updateProName(value: String) { _proName.value = value }
    fun updateProPhone(value: String) { _proPhone.value = value }
    fun updateProWhatsApp(value: String) { _proWhatsApp.value = value }
    fun updateProCity(value: String) { _proCity.value = value }
    fun updateProProfession(value: String) { _proProfession.value = value }
    fun updateProExperience(value: String) { _proExperience.value = value }

    // Set Filters
    fun setCityFilter(city: String?) { _selectedCityFilter.value = city }
    fun setServiceFilter(service: String?) { _selectedServiceFilter.value = service }

    // Action Methods
    fun submitServiceRequest(onSuccess: (ServiceRequest) -> Unit) {
        val name = _custName.value.trim()
        val mobile = _custMobile.value.trim()
        val city = _custCity.value
        val area = _custArea.value.trim()
        val service = _custService.value
        val description = _custDescription.value.trim()

        if (name.isEmpty() || mobile.isEmpty() || area.isEmpty()) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowError("Please fill out Name, Mobile and Area fields."))
            }
            return
        }

        viewModelScope.launch {
            val request = ServiceRequest(
                fullName = name,
                mobileNumber = mobile,
                city = city,
                area = area,
                requiredService = service,
                description = description,
                latitude = _custLatitude.value,
                longitude = _custLongitude.value,
                mapAddress = _custMapAddress.value
            )
            val generatedId = repository.insertRequest(request)
            val savedRequest = request.copy(id = generatedId)
            
            // Clear customer form fields
            _custName.value = ""
            _custMobile.value = ""
            _custArea.value = ""
            _custDescription.value = ""
            _custLatitude.value = null
            _custLongitude.value = null
            _custMapAddress.value = null
            
            _uiEvent.emit(UiEvent.RequestSuccess(savedRequest))
            onSuccess(savedRequest)
        }
    }

    fun submitProfessionalRegistration() {
        val name = _proName.value.trim()
        val phone = _proPhone.value.trim()
        val whatsApp = _proWhatsApp.value.trim()
        val city = _proCity.value
        val profession = _proProfession.value
        val expStr = _proExperience.value.trim()

        if (name.isEmpty() || phone.isEmpty() || whatsApp.isEmpty() || expStr.isEmpty()) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowError("Please complete all professional registration fields."))
            }
            return
        }

        val yearsStr = expStr.filter { it.isDigit() }
        val years = if (yearsStr.isEmpty()) 1 else yearsStr.toInt()

        viewModelScope.launch {
            val newPro = Professional(
                fullName = name,
                phoneNumber = phone,
                whatsAppNumber = whatsApp,
                city = city,
                profession = profession,
                yearsOfExperience = years,
                rating = 4.8f, // Default start rating
                isVerified = false, // Pending verification, of course (realistic process!)
                status = "Vetting"
            )
            repository.insertProfessional(newPro)

            // Clear professional form
            _proName.value = ""
            _proPhone.value = ""
            _proWhatsApp.value = ""
            _proExperience.value = ""

            _uiEvent.emit(UiEvent.RegisterSuccess(newPro))
        }
    }

    sealed interface UiEvent {
        data class ShowError(val message: String) : UiEvent
        data class RequestSuccess(val request: ServiceRequest) : UiEvent
        data class RegisterSuccess(val professional: Professional) : UiEvent
    }
}
