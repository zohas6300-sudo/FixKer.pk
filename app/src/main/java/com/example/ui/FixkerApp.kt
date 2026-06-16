package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.R
import com.example.data.Professional
import com.example.data.ServiceRequest
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixkerApp(
    viewModel: MarketplaceViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    // Observe State
    val filteredProfessionals by viewModel.filteredProfessionals.collectAsState()
    val allProfessionals by viewModel.professionals.collectAsState()
    val serviceRequests by viewModel.serviceRequests.collectAsState()
    val cityFilter by viewModel.selectedCityFilter.collectAsState()
    val serviceFilter by viewModel.selectedServiceFilter.collectAsState()

    // Form entries (Customer)
    val custName by viewModel.custName.collectAsState()
    val custMobile by viewModel.custMobile.collectAsState()
    val custCity by viewModel.custCity.collectAsState()
    val custArea by viewModel.custArea.collectAsState()
    val custService by viewModel.custService.collectAsState()
    val custDescription by viewModel.custDescription.collectAsState()

    // Form entries (Professional)
    val proName by viewModel.proName.collectAsState()
    val proPhone by viewModel.proPhone.collectAsState()
    val proWhatsApp by viewModel.proWhatsApp.collectAsState()
    val proCity by viewModel.proCity.collectAsState()
    val proProfession by viewModel.proProfession.collectAsState()
    val proExperience by viewModel.proExperience.collectAsState()

    // UI Dialog controllers
    var successRequestDialog by remember { mutableStateOf<ServiceRequest?>(null) }
    var successProDialog by remember { mutableStateOf<Professional?>(null) }
    var showLeadsPanel by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showCitySelectionModal by remember { mutableStateOf(false) }

    val displayProfessionals = remember(filteredProfessionals, searchQuery) {
        if (searchQuery.isBlank()) {
            filteredProfessionals
        } else {
            filteredProfessionals.filter {
                it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.profession.contains(searchQuery, ignoreCase = true) ||
                it.city.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Dropdown expanded states
    var custCityExpanded by remember { mutableStateOf(false) }
    var custServiceExpanded by remember { mutableStateOf(false) }
    var proCityExpanded by remember { mutableStateOf(false) }
    var proServiceExpanded by remember { mutableStateOf(false) }

    // Listen to ViewModel events
    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is MarketplaceViewModel.UiEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                is MarketplaceViewModel.UiEvent.RequestSuccess -> {
                    successRequestDialog = event.request
                }
                is MarketplaceViewModel.UiEvent.RegisterSuccess -> {
                    successProDialog = event.professional
                }
            }
        }
    }

    // List of cities and services for dropdown selection
    val pakCities = listOf("Lahore", "Karachi", "Islamabad", "Rawalpindi", "Multan", "Faisalabad", "Gujranwala", "Sialkot", "Peshawar", "Bahawalpur")
    val servicesList = listOf("Electrician", "Plumber", "Carpenter", "Welder", "Solar Installer", "AC Technician", "Painter", "Handyman")

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        containerColor = Color(0xFFF8F9FA),
        bottomBar = {
            StickyBottomNavBarScroll(
                scrollState = scrollState,
                context = context
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main continuous landing page scrollable content
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA))
            ) {
                // 1. Navigation Header
                item {
                    HeaderBar(
                        selectedCity = cityFilter ?: "All",
                        onCityClick = { showCitySelectionModal = true },
                        onRequestQuoteClick = {
                            scope.launch {
                                // Scroll straight to the Customer Request Form
                                scrollState.animateScrollToItem(4)
                            }
                        },
                        onJoinProClick = {
                            scope.launch {
                                // Scroll to Professional signup form
                                scrollState.animateScrollToItem(7)
                            }
                        }
                    )
                }

                // 2. Hero Section
                item {
                    HeroSection(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onRequestClick = {
                            scope.launch {
                                scrollState.animateScrollToItem(5)
                            }
                        },
                        onWhatsAppClick = {
                            openWhatsAppDirect(context, "923001234567", "Hi Fixker.pk! I am looking for verified skilled workers near me.")
                        },
                        onJoinClick = {
                            scope.launch {
                                scrollState.animateScrollToItem(7)
                            }
                        }
                    )
                }

                // 3. Trust Indicators Banner
                item {
                    TrustIndicators()
                }

                // 4. Popular Services Section
                item {
                    PopularServicesSection(
                        onServiceSelected = { serviceName ->
                            viewModel.updateCustService(serviceName)
                            viewModel.setServiceFilter(serviceName)
                            Toast.makeText(context, "Selected category: $serviceName. View matching pros listed below, or fill request form!", Toast.LENGTH_SHORT).show()
                            scope.launch {
                                // Scroll to Request Form beautifully
                                scrollState.animateScrollToItem(4)
                            }
                        }
                    )
                }

                // 5. How It Works Section
                item {
                    HowItWorksSection()
                }

                // 6. Lead Generation Customer Form
                item {
                    CustomerFormSection(
                        fullName = custName,
                        onFullNameChange = { viewModel.updateCustName(it) },
                        mobileNumber = custMobile,
                        onMobileNumberChange = { viewModel.updateCustMobile(it) },
                        selectedCity = custCity,
                        onCityClick = { custCityExpanded = true },
                        cityExpanded = custCityExpanded,
                        onCityDismiss = { custCityExpanded = false },
                        cities = pakCities,
                        onCitySelect = {
                            viewModel.updateCustCity(it)
                            custCityExpanded = false
                        },
                        requiredArea = custArea,
                        onAreaChange = { viewModel.updateCustArea(it) },
                        selectedService = custService,
                        onServiceClick = { custServiceExpanded = true },
                        serviceExpanded = custServiceExpanded,
                        onServiceDismiss = { custServiceExpanded = false },
                        services = servicesList,
                        onServiceSelect = {
                            viewModel.updateCustService(it)
                            custServiceExpanded = false
                        },
                        problemDescription = custDescription,
                        onDescriptionChange = { viewModel.updateCustDescription(it) },
                        onSubmit = {
                            viewModel.submitServiceRequest { req ->
                                // Optional fast action
                            }
                        }
                    )
                }

                // 7. Why Choose Us Section
                item {
                    WhyChooseUsSection()
                }

                // 8. Professional Registration Section
                item {
                    ProfessionalRegistrationSection(
                        fullName = proName,
                        onFullNameChange = { viewModel.updateProName(it) },
                        phoneNumber = proPhone,
                        onPhoneChange = { viewModel.updateProPhone(it) },
                        whatsAppNumber = proWhatsApp,
                        onWhatsAppChange = { viewModel.updateProWhatsApp(it) },
                        selectedCity = proCity,
                        onCityClick = { proCityExpanded = true },
                        cityExpanded = proCityExpanded,
                        onCityDismiss = { proCityExpanded = false },
                        cities = pakCities,
                        onCitySelect = {
                            viewModel.updateProCity(it)
                            proCityExpanded = false
                        },
                        selectedProfession = proProfession,
                        onProfessionClick = { proServiceExpanded = true },
                        professionExpanded = proServiceExpanded,
                        onProfessionDismiss = { proServiceExpanded = false },
                        professions = servicesList,
                        onProfessionSelect = {
                            viewModel.updateProProfession(it)
                            proServiceExpanded = false
                        },
                        experienceYears = proExperience,
                        onExperienceChange = { viewModel.updateProExperience(it) },
                        onSubmit = {
                            viewModel.submitProfessionalRegistration()
                        }
                    )
                }

                // 9. Filterable Featured Professionals
                item {
                    FeaturedProfessionalsHeader(
                        selectedCity = cityFilter ?: "All",
                        selectedService = serviceFilter ?: "All",
                        cities = listOf("All") + pakCities,
                        services = listOf("All") + servicesList,
                        onCitySelect = { viewModel.setCityFilter(it) },
                        onServiceSelect = { viewModel.setServiceFilter(it) }
                    )
                }

                if (displayProfessionals.isEmpty()) {
                    item {
                        EmptyProfessionalsState(
                            onReset = {
                                searchQuery = ""
                                viewModel.setCityFilter("All")
                                viewModel.setServiceFilter("All")
                            }
                        )
                    }
                } else {
                    items(displayProfessionals) { pro ->
                        ProfessionalProfileCard(
                            professional = pro,
                            onWhatsAppClick = {
                                val msg = "Hello ${pro.fullName}, I found your verified profile on Fixker.pk for ${pro.profession} service in ${pro.city}. Are you available?"
                                openWhatsAppDirect(context, pro.whatsAppNumber, msg)
                            },
                            onCallClick = {
                                placePhoneCall(context, pro.phoneNumber)
                            }
                        )
                    }
                }

                // 9b. Top Rated Featured Professional Promo Banner
                item {
                    FeaturedProfessionalBanner(
                        onHireClick = {
                            openWhatsAppDirect(context, "923001234567", "Salam! I am looking to hire top-rated expert Muhammad Bilal for electrical work.")
                        }
                    )
                }

                // 10. Cities We Serve List
                item {
                    CitiesWeServeSection(
                        cities = pakCities,
                        selectedCity = cityFilter,
                        onCityClick = { city ->
                            viewModel.setCityFilter(city)
                            Toast.makeText(context, "Filtering professionals in $city. View featured pros below!", Toast.LENGTH_SHORT).show()
                            scope.launch {
                                // Scroll to Featured Professionals header beautiful index (which is index 8)
                                scrollState.animateScrollToItem(9)
                            }
                        }
                    )
                }

                // 11. Customer Reviews / Testimonials
                item {
                    CustomerReviewsSection()
                }

                // 12. Frequently Asked Questions (FAQ)
                item {
                    FAQSection()
                }

                // 13. About Section
                item {
                    AboutSection()
                }

                // 14. Live Lead Database Monitoring Panel Toggle (Admin / Review Tool for AI-Studio)
                item {
                    LiveLeadsTriggerButton(
                        leadCount = serviceRequests.size,
                        proCount = allProfessionals.size,
                        expanded = showLeadsPanel,
                        onToggle = { showLeadsPanel = !showLeadsPanel }
                    )
                }

                if (showLeadsPanel) {
                    if (serviceRequests.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                                border = BorderStroke(1.dp, BorderLight)
                            ) {
                                Text(
                                    text = "No Service Requests yet. Submit a request above in the Customer Request Form to see it persistent here in real-time!",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(serviceRequests) { req ->
                            RequestLeadItemCard(
                                request = req,
                                onCall = { placePhoneCall(context, req.mobileNumber) },
                                onWhatsApp = {
                                    val text = "Salam ${req.fullName}, regarding your Fixker.pk request for ${req.requiredService} in ${req.city}, ${req.area}."
                                    openWhatsAppDirect(context, req.mobileNumber, text)
                                }
                            )
                        }
                    }
                }

                // 15. Footer Section
                item {
                    FooterSection(
                        onHomeClick = { scope.launch { scrollState.animateScrollToItem(0) } },
                        onServicesClick = { scope.launch { scrollState.animateScrollToItem(3) } },
                        onRequestClick = { scope.launch { scrollState.animateScrollToItem(4) } },
                        onJoinClick = { scope.launch { scrollState.animateScrollToItem(7) } },
                        onFAQClick = { scope.launch { scrollState.animateScrollToItem(11) } }
                    )
                }
            }

            // FABs are replaced by our beautiful Sleek StickyBottomNavBarScroll at the scaffolding level
        }
    }

    // Success Dialog for Service Form Request Submission
    successRequestDialog?.let { req ->
        Dialog(onDismissRequest = { successRequestDialog = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(SuccessGreen.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = SuccessGreen,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Request Submitted!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Salam ${req.fullName}, your service request for ${req.requiredService} in ${req.city} has been received. Our team is matching you with local, highly-skilled professionals right now.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Divider(color = BorderLight)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Instant Match Assistance:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dual fast conversion buttons
                    Button(
                        onClick = {
                            successRequestDialog = null
                            val textMsg = "Salam Fixker! I just submitted a service request ID #${req.id} for ${req.requiredService} in ${req.city} (${req.area}). Please match me quickly."
                            openWhatsAppDirect(context, "923001234567", textMsg)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("success_whatsapp_match"),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connect via WhatsApp Now", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            successRequestDialog = null
                            placePhoneCall(context, "+923001234567")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DeepBlue40)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = DeepBlue40)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Call Direct Helpline", color = DeepBlue40, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { successRequestDialog = null }) {
                        Text("Close", color = TextGray)
                    }
                }
            }
        }
    }

    // Success Dialog for Professional Registration
    successProDialog?.let { pro ->
        Dialog(onDismissRequest = { successProDialog = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(DeepBlue40.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = "Success",
                            tint = DeepBlue40,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Registration Submitted!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Welcome ${pro.fullName}! Your request to join Pakistan's Fasting Growing Professional network as an experienced ${pro.profession} has been recorded.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Status: Verification Pending",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = CardOrange
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            successProDialog = null
                            val welcomeText = "Salam Fixker Support! I registered as an experienced ${pro.profession} in ${pro.city}. Let's proceed with verification."
                            openWhatsAppDirect(context, "923001234567", welcomeText)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Fast-Track My Verification", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = { successProDialog = null }) {
                        Text("Done", color = TextGray)
                    }
                }
            }
        }
    }

    if (showCitySelectionModal) {
        CitySelectionModal(
            currentCity = cityFilter ?: "All",
            cities = pakCities,
            onDismiss = { showCitySelectionModal = false },
            onCitySelected = { city ->
                viewModel.setCityFilter(city)
                showCitySelectionModal = false
            }
        )
    }
}

// ==================== COMPOSE SUBCOMPONENTS ====================

@Composable
fun HeaderBar(
    selectedCity: String,
    onCityClick: () -> Unit,
    onRequestQuoteClick: () -> Unit,
    onJoinProClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Cool brand logo symbol
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF2563EB), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "F",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Fixker",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF1E3A8A)
                    )
                    Text(
                        text = ".pk",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF16A34A)
                    )
                }
            }

            // Location Badge matching the mockup
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(100.dp))
                    .clickable { onCityClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFFEF4444), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedCity.equals("All", ignoreCase = true)) "Pakistan" else selectedCity,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select City",
                    tint = Color.Gray,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun HeroSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRequestClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onJoinClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA))
            .padding(top = 28.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Large main headline
        Text(
            text = "Trusted Professionals\nat Your Doorstep",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 28.sp,
                lineHeight = 36.sp
            ),
            color = Color(0xFF0F172A), // Slate-900
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Subheadline italicized
        Text(
            text = "\"Pakistan's Trusted Network of Skilled Professionals\"",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFF64748B), // Slate-500
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Premium search input bar with custom search icon
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search for Electrician, Plumber...", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFFE2E8F0),
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedTextColor = Color(0xFF1E293B),
                unfocusedTextColor = Color(0xFF1E293B)
            ),
            shape = RoundedCornerShape(20.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Professional high aesthetic Banner Image
        AsyncImage(
            model = R.drawable.img_hero_banner_1781632086238,
            contentDescription = "Pakistani Service Professionals",
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, BorderLight, RoundedCornerShape(24.dp))
                .shadow(2.dp, RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Crop,
            error = painterResource(id = android.R.drawable.stat_notify_error),
            fallback = painterResource(id = R.drawable.img_hero_banner_1781632086238)
        )
    }
}

@Composable
fun TrustIndicators() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Card 1: Verified
        Box(
            modifier = Modifier
                .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text("Verified", fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8), fontSize = 13.sp)
                Text("Pros Only", color = Color(0xFF2563EB), fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }
        }
        // Card 2: Fast
        Box(
            modifier = Modifier
                .background(Color(0xFFF0FDF4), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFDCFCE7), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text("Fast", fontWeight = FontWeight.Bold, color = Color(0xFF15803D), fontSize = 13.sp)
                Text("15m Response", color = Color(0xFF16A34A), fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }
        }
        // Card 3: Affordable
        Box(
            modifier = Modifier
                .background(Color(0xFFFFF7ED), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFFFEDD5), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text("Affordable", fontWeight = FontWeight.Bold, color = Color(0xFFC2410C), fontSize = 13.sp)
                Text("Flat Rates", color = Color(0xFFEA580C), fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }
        }
        // Card 4: Pay Direct
        Box(
            modifier = Modifier
                .background(Color(0xFFFAF5FF), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFF3E8FF), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text("No Markup", fontWeight = FontWeight.Bold, color = Color(0xFF6B21A8), fontSize = 13.sp)
                Text("Pay Direct", color = Color(0xFF8B5CF6), fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun TrustItem(icon: ImageVector, label: String, tint: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
    }
}

@Composable
fun PopularServicesSection(
    onServiceSelected: (String) -> Unit
) {
    val services = listOf(
        ServiceCardData("⚡", "Electrician"),
        ServiceCardData("🚿", "Plumber"),
        ServiceCardData("❄️", "AC Technician"),
        ServiceCardData("☀️", "Solar Installer"),
        ServiceCardData("🪚", "Carpenter"),
        ServiceCardData("🎨", "Painter"),
        ServiceCardData("🔨", "Handyman"),
        ServiceCardData("🔩", "Welder")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Popular Services",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "Click a category to match nearby professionals instantly",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
            Text(
                text = "SEE ALL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB),
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        val rows = services.chunked(4)
        for (row in rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (item in row) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onServiceSelected(item.idName) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                                .shadow(1.dp, RoundedCornerShape(16.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.emojiLabel,
                                fontSize = 24.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.idName.substringBefore(" "),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

data class ServiceCardData(val emojiLabel: String, val idName: String)

@Composable
fun HowItWorksSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLight)
            .padding(24.dp)
    ) {
        Text(
            text = "How It Works",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = "Get connected in 4 easy steps",
            style = MaterialTheme.typography.bodySmall,
            color = TextGray,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp)
        )

        StepRow(step = "1", title = "Submit Your Service Request", description = "Fill the short form with your city, area, and problem details.")
        Spacer(modifier = Modifier.height(16.dp))
        StepRow(step = "2", title = "We Match You with Nearby Pros", description = "Our automated matching connects verified experts close to your area.")
        Spacer(modifier = Modifier.height(16.dp))
        StepRow(step = "3", title = "Receive a Call or WhatsApp Response", description = "Professionals will contact you with affordable and direct custom service quotes.")
        Spacer(modifier = Modifier.height(16.dp))
        StepRow(step = "4", title = "Get Your Job Done Quickly", description = "Relax as skilled workers complete your needs with guaranteed satisfaction.")
    }
}

@Composable
fun StepRow(step: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = DeepBlue40
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = step, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, fontSize = 13.sp, color = TextGray)
        }
    }
}

@Composable
fun WhyChooseUsSection() {
    val items = listOf(
        WhyItemData(Icons.Default.Security, "Verified Professionals", "Every worker is verified with local NIC ID database audits, ensuring extreme safety."),
        WhyItemData(Icons.Default.Timer, "Fast 15-Min Response", "Nearby matching algorithm triggers callbacks within minutes, solving emergency issues quickly."),
        WhyItemData(Icons.Default.PinDrop, "Local Pakistani Experts", "Hire artisans who understand specific local designs, fitting plumbing systems and power grids."),
        WhyItemData(Icons.Default.Loyalty, "Affordable Rates", "Communicate with professionals directly. No third-party marketplace commissions mean lower cost for you.")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Why Choose Fixker.pk",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = "Pakistan's leading trusted lead-gen marketplace",
            style = MaterialTheme.typography.bodySmall,
            color = TextGray,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp)
        )

        for (item in items) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Emerald40.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = item.icon, contentDescription = null, tint = Emerald40, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = item.description, fontSize = 13.sp, color = TextGray)
                }
            }
        }
    }
}

data class WhyItemData(val icon: ImageVector, val title: String, val description: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormSection(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    mobileNumber: String,
    onMobileNumberChange: (String) -> Unit,
    selectedCity: String,
    onCityClick: () -> Unit,
    cityExpanded: Boolean,
    onCityDismiss: () -> Unit,
    cities: List<String>,
    onCitySelect: (String) -> Unit,
    requiredArea: String,
    onAreaChange: (String) -> Unit,
    selectedService: String,
    onServiceClick: () -> Unit,
    serviceExpanded: Boolean,
    onServiceDismiss: () -> Unit,
    services: List<String>,
    onServiceSelect: (String) -> Unit,
    problemDescription: String,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, DeepBlue40),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Heading
            Text(
                text = "Contact Verified Experts Now",
                style = MaterialTheme.typography.titleLarge,
                color = DeepBlue40,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            Text(
                text = "Get calls and WhatsApp replies in minutes.",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Input: Full Name
            OutlinedTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = { Text("Full Name") },
                placeholder = { Text("e.g. Akbar Khan") },
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cust_name_input")
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(8.dp)
            )

            // Input: Mobile Number
            OutlinedTextField(
                value = mobileNumber,
                onValueChange = onMobileNumberChange,
                label = { Text("Mobile Phone Number") },
                placeholder = { Text("e.g. 03001234567") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cust_mobile_input")
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(8.dp)
            )

            // Dynamic Row: City dropdown & Area Input
            Row(modifier = Modifier.fillMaxWidth()) {
                // Dropdown City selection
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(end = 4.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = cityExpanded,
                        onExpandedChange = { onCityClick() }
                    ) {
                        OutlinedTextField(
                            value = selectedCity,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("City") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .testTag("cust_city_dropdown"),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = cityExpanded,
                            onDismissRequest = onCityDismiss
                        ) {
                            cities.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city) },
                                    onClick = { onCitySelect(city) }
                                )
                            }
                        }
                    }
                }

                // Input: Area description (DHA, Clifton, Gulberg...)
                OutlinedTextField(
                    value = requiredArea,
                    onValueChange = onAreaChange,
                    label = { Text("Area / Sector") },
                    placeholder = { Text("e.g. DHA Phase 5") },
                    singleLine = true,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("cust_area_input")
                        .padding(start = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dropdown Service selection
            Box(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = serviceExpanded,
                    onExpandedChange = { onServiceClick() }
                ) {
                    OutlinedTextField(
                        value = selectedService,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Required Service") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("cust_service_dropdown"),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = serviceExpanded,
                        onDismissRequest = onServiceDismiss
                    ) {
                        services.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = { onServiceSelect(s) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Input: Problem Description
            OutlinedTextField(
                value = problemDescription,
                onValueChange = onDescriptionChange,
                label = { Text("Short description of problem") },
                placeholder = { Text("Explain shortly what needs fixing (e.g. kitchen water outlet is broken and leaks)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("cust_desc_input")
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(8.dp)
            )

            // High aesthetic: Large action submit button with solid, high-converting tag
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("cust_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald40),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Get Connected Now",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalRegistrationSection(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    whatsAppNumber: String,
    onWhatsAppChange: (String) -> Unit,
    selectedCity: String,
    onCityClick: () -> Unit,
    cityExpanded: Boolean,
    onCityDismiss: () -> Unit,
    cities: List<String>,
    onCitySelect: (String) -> Unit,
    selectedProfession: String,
    onProfessionClick: () -> Unit,
    professionExpanded: Boolean,
    onProfessionDismiss: () -> Unit,
    professions: List<String>,
    onProfessionSelect: (String) -> Unit,
    experienceYears: String,
    onExperienceChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepBlue40.copy(alpha = 0.03f))
            .padding(vertical = 28.dp, horizontal = 16.dp)
    ) {
        // Heading
        Text(
            text = "Join Pakistan's Fastest Growing Professional Network",
            style = MaterialTheme.typography.titleLarge,
            color = DeepBlue40,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Register as an independent artisan and receive direct premium customer orders from Lahore, Karachi, and major cities.",
            style = MaterialTheme.typography.bodySmall,
            color = TextGray,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Professional Registration fields
        OutlinedTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = { Text("Full Name") },
            placeholder = { Text("e.g. Ustaad Muhammad") },
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pro_name_input")
                .background(Color.White)
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(8.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onPhoneChange,
                label = { Text("Phone Number") },
                placeholder = { Text("e.g. 03001234567") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .testTag("pro_phone_input")
                    .background(Color.White)
                    .padding(end = 4.dp),
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = whatsAppNumber,
                onValueChange = onWhatsAppChange,
                label = { Text("WhatsApp No") },
                placeholder = { Text("923001234567") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .testTag("pro_whatsapp_input")
                    .background(Color.White)
                    .padding(start = 4.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = cityExpanded,
                    onExpandedChange = { onCityClick() }
                ) {
                    OutlinedTextField(
                        value = selectedCity,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("City Base") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .background(Color.White),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = cityExpanded,
                        onDismissRequest = onCityDismiss
                    ) {
                        cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = { onCitySelect(city) }
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = professionExpanded,
                    onExpandedChange = { onProfessionClick() }
                ) {
                    OutlinedTextField(
                        value = selectedProfession,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("My Profession") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = professionExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .background(Color.White),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = professionExpanded,
                        onDismissRequest = onProfessionDismiss
                    ) {
                        professions.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p) },
                                onClick = { onProfessionSelect(p) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = experienceYears,
            onValueChange = onExperienceChange,
            label = { Text("Years of Experience") },
            placeholder = { Text("e.g. 5") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pro_experience_input")
                .background(Color.White)
                .padding(bottom = 14.dp),
            shape = RoundedCornerShape(8.dp)
        )

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("pro_register_button"),
            colors = ButtonDefaults.buttonColors(containerColor = DeepBlue40),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AppRegistration, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Register as Professional", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeaturedProfessionalsHeader(
    selectedCity: String,
    selectedService: String,
    cities: List<String>,
    services: List<String>,
    onCitySelect: (String) -> Unit,
    onServiceSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Featured Professionals in Pakistan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = TextDark
        )

        Text(
            text = "Browse, contact directly, and choose near you. Dynamic database handles verification credentials.",
            style = MaterialTheme.typography.bodySmall,
            color = TextGray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // City Filters Horizontal Row
        Text(
            text = "Filter by City:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp)
        ) {
            cities.forEach { city ->
                val isSelected = selectedCity == city
                AssistChip(
                    onClick = { onCitySelect(city) },
                    label = { Text(city) },
                    modifier = Modifier.padding(end = 4.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isSelected) DeepBlue40 else Color.White,
                        labelColor = if (isSelected) Color.White else TextDark
                    )
                )
            }
        }

        // Service Filters Horizontal Row
        Text(
            text = "Filter by Skill:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp)
        ) {
            services.forEach { service ->
                val isSelected = selectedService == service
                AssistChip(
                    onClick = { onServiceSelect(service) },
                    label = { Text(service) },
                    modifier = Modifier.padding(end = 4.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isSelected) Emerald40 else Color.White,
                        labelColor = if (isSelected) Color.White else TextDark
                    )
                )
            }
        }

        Divider(color = BorderLight)
    }
}

@Composable
fun EmptyProfessionalsState(onReset: () -> Unit) {
    Card(
        modifier = Modifier
            .fillOuterMarginLayout()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.SentimentDissatisfied, contentDescription = null, tint = NeutralGray, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No professionals found matching current filters.",
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Try clearing filters to find all registered technicians.",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(containerColor = DeepBlue40)
            ) {
                Text("Show All Professionals")
            }
        }
    }
}

@Composable
fun ProfessionalProfileCard(
    professional: Professional,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image circle placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(DeepBlue40.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Large initial
                Text(
                    text = professional.fullName.first().toString(),
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = DeepBlue40
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1.3f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = professional.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (professional.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified badge",
                            tint = Emerald40,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .background(CardOrange.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Vetting",
                                fontSize = 8.sp,
                                color = CardOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${professional.profession} • ${professional.yearsOfExperience} Years Exp",
                    fontSize = 13.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = CardOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${professional.rating} / 5.0",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "City Location",
                        tint = NeutralGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = professional.city,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextGray
                    )
                }
            }

            // Interactive contact buttons
            Column(
                modifier = Modifier.weight(0.7f),
                horizontalAlignment = Alignment.End
            ) {
                // Direct WhatsApp Button
                Button(
                    onClick = onWhatsAppClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Direct Call
                OutlinedButton(
                    onClick = onCallClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    border = BorderStroke(1.dp, DeepBlue40),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = DeepBlue40, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 11.sp, color = DeepBlue40, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CitiesWeServeSection(
    cities: List<String>,
    selectedCity: String?,
    onCityClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLight.copy(alpha = 0.5f))
            .padding(24.dp)
    ) {
        Text(
            text = "Proudly Serving Major Pakistani Cities",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = TextDark,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = "Click a city below to view nearby certified professionals instantly.",
            style = MaterialTheme.typography.bodySmall,
            color = TextGray,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        // Hand-coded layouts avoiding nesting issues
        val rows = cities.chunked(2)
        for (row in rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (city in row) {
                    val isFiltered = selectedCity == city
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .clickable { onCityClick(city) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFiltered) DeepBlue40.copy(alpha = 0.08f) else Color.White
                        ),
                        border = BorderStroke(1.dp, if (isFiltered) DeepBlue40 else BorderLight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationCity,
                                contentDescription = city,
                                tint = if (isFiltered) DeepBlue40 else NeutralGray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = city,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isFiltered) DeepBlue40 else TextDark
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (isFiltered) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = DeepBlue40, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerReviewsSection() {
    val reviews = listOf(
        ReviewData("Zahid Farooq", "Lahore, DHA Phase 6", 5, "Salam standard electricians here are phenomenal! Muhammad Ali came of the database request tool within 20 mins to fix our inverter AC unit. Saved us big money with zero commissions!"),
        ReviewData("Ayesha Siddiqua", "Karachi, Clifton", 5, "I submitted a plumbing request on Fixker.pk on Sunday. Sajid was matched near our home within 10 mins. Exceptional service quality, polite behavior, and reasonable pricing."),
        ReviewData("Raja Hammad", "Islamabad, G-11", 4, "Highly recommended startup! Tested their lead matcher for fitting solar panels, they assigned Tariq. He did a clean job. Very trustworthy workers.")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Trusted Across Pakistan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = "Realistic testimonials from actual households",
            style = MaterialTheme.typography.bodySmall,
            color = TextGray,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 20.dp)
        )

        for (review in reviews) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = review.name, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 14.sp)
                            Text(text = review.location, fontSize = 11.sp, color = TextGray)
                        }

                        Row {
                            repeat(review.stars) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = CardOrange, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "\"${review.text}\"",
                        fontSize = 13.sp,
                        color = TextDark,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

data class ReviewData(val name: String, val location: String, val stars: Int, val text: String)

@Composable
fun FAQSection() {
    val faqs = listOf(
        FaqData("What is Fixker.pk?", "Fixker.pk is Pakistan's premier trusted network connecting households with verified local skilled technicians (electricians, plumbers, carpenters, etc.) anywhere in Pakistan. You submit a lead request, and local professionals call or WhatsApp you directly."),
        FaqData("Do you charge any commission?", "Our marketplace is commission-free for household leads! Customers pay workers directly, which lowers service charges and provides fair wages for pakistani independent professionals."),
        FaqData("How do you verify professionals?", "Artisans undergo a thorough vetting process including verification with CNIC cards, past references checks, and strict review tracking before they receive a verified badge on our dashboard."),
        FaqData("What cities are supported?", "We actively cover Lahore, Karachi, Islamabad, Rawalpindi, Multan, Faisalabad, Gujranwala, Sialkot, Peshawar, Bahawalpur, and other major cities.")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLight.copy(alpha = 0.5f))
            .padding(24.dp)
    ) {
        Text(
            text = "Frequently Asked Questions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(18.dp))

        for (faq in faqs) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                var expanded by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = faq.question,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(0.9f)
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = DeepBlue40,
                                modifier = Modifier.weight(0.1f)
                            )
                        }

                        AnimatedVisibility(
                            visible = expanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = faq.answer,
                                    fontSize = 13.sp,
                                    color = TextGray,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class FaqData(val question: String, val answer: String)

@Composable
fun AboutSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(DeepBlue40.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = DeepBlue40, modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "About Fixker.pk",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Our mission is to make it easy for every Pakistani household and business to find trusted service professionals quickly and safely. By prioritizing CNIC verification and bypassing heavy middleman commission platforms, we champion localized micro-entrepreneurs while providing homeowners superior reliability, safety, and swift repair completions.",
            style = MaterialTheme.typography.bodySmall,
            color = TextGray,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun LiveLeadsTriggerButton(
    leadCount: Int,
    proCount: Int,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Badge(containerColor = SuccessGreen) {
                    Text("$leadCount", color = Color.White)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = "Review Registered Leads", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 14.sp)
                    Text(text = "Total Artisans Registered: $proCount", fontSize = 11.sp, color = TextGray)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (expanded) "Hide Leads" else "Inspect Leads",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = DeepBlue40
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = DeepBlue40
                )
            }
        }
    }
}

@Composable
fun RequestLeadItemCard(
    request: ServiceRequest,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = request.fullName, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 14.sp)
                    Text(text = "${request.city} (Area: ${request.area})", fontSize = 12.sp, color = TextGray)
                }

                Box(
                    modifier = Modifier
                        .background(SuccessGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = request.requiredService, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = SuccessGreen)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Problem description: ${request.description}",
                fontSize = 12.sp,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Lead persistent in SQLite Room with CNIC routing",
                    fontSize = 10.sp,
                    color = NeutralGray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    IconButton(onClick = onCall, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = DeepBlue40, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onWhatsApp, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = SuccessGreen, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FooterSection(
    onHomeClick: () -> Unit,
    onServicesClick: () -> Unit,
    onRequestClick: () -> Unit,
    onJoinClick: () -> Unit,
    onFAQClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepBlue40)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Fixker.pk", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Pakistan's Trusted Skilled Network", color = Emerald80, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            // Quick Links
            Column(horizontalAlignment = Alignment.End) {
                FooterLinkText(text = "Home", onClick = onHomeClick)
                FooterLinkText(text = "Services", onClick = onServicesClick)
                FooterLinkText(text = "Request service", onClick = onRequestClick)
                FooterLinkText(text = "Join as Professional", onClick = onJoinClick)
                FooterLinkText(text = "FAQs", onClick = onFAQClick)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Divider(color = Color.White.copy(alpha = 0.1f))

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "© 2026 Fixker.pk. All CNIC Data Reserved.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp
            )

            // Social Icons
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Facebook",
                    tint = Color.White,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 4.dp)
                )
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Instagram",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun FooterLinkText(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.8f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 2.dp)
    )
}

@Composable
fun StickyCommunicationFABs(
    onWhatsAppClick: () -> Unit,
    onCallClick: () -> Unit
) {
    // Elegant floating bottom-right container holding direct-call & sticky whatsapp
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // Call Support FAB (Blue)
            FloatingActionButton(
                onClick = onCallClick,
                containerColor = DeepBlue40,
                contentColor = Color.White,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("sticky_call_fab"),
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Call direct help desk", modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sticky WhatsApp (Green)
            FloatingActionButton(
                onClick = onWhatsAppClick,
                containerColor = SuccessGreen,
                contentColor = Color.White,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("sticky_whatsapp_fab"),
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.Chat, contentDescription = "WhatsApp customer care team", modifier = Modifier.size(24.dp))
            }
        }
    }
}

// Modifier utility for standard layout density matching M3 guidelines
private fun Modifier.fillOuterMarginLayout() = this
    .fillMaxWidth()

// Custom dialer function
private fun placePhoneCall(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not launch direct call. CNIC number: $phoneNumber", Toast.LENGTH_SHORT).show()
    }
}

// Custom WhatsApp universal connection
private fun openWhatsAppDirect(context: Context, fullWhatsAppNumber: String, message: String) {
    try {
        val formattedNumber = fullWhatsAppNumber.replace("+", "").replace(" ", "")
        val encodedMessage = URLEncoder.encode(message, "UTF-8")
        val url = "https://api.whatsapp.com/send?phone=$formattedNumber&text=$encodedMessage"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could open WhatsApp links. Tech number: $fullWhatsAppNumber", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun FeaturedProfessionalBanner(
    onHireClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A)) // deep bg-blue-900
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar Initial
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("M", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Muhammad Bilal",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Expert Electrician • 8 yrs Exp.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("★★★★★", color = Color(0xFFFBBF24), fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TOP RATED",
                                color = Color(0xFF1E3A8A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onHireClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)), // bg-green-500
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("HIRE NOW", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun StickyBottomNavBarScroll(
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    context: Context
) {
    val scope = rememberCoroutineScope()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 6.dp)
        ) {
            // Row of CTA buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // WhatsApp Button
                Button(
                    onClick = {
                        openWhatsAppDirect(context, "923001234567", "Hi Fixker.pk! I am looking for verified skilled workers near me.")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp Now", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                }

                // Call Support button
                Button(
                    onClick = {
                        placePhoneCall(context, "03001234567")
                    },
                    modifier = Modifier.size(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue40),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            // Custom safe visual divider line
            Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderLight))

            // Bottom Nav tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentIndex = scrollState.firstVisibleItemIndex
                BottomTabItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    isActive = currentIndex < 3,
                    onClick = {
                        scope.launch { scrollState.animateScrollToItem(0) }
                    }
                )
                BottomTabItem(
                    icon = Icons.Default.Build,
                    label = "Services",
                    isActive = currentIndex in 3..4,
                    onClick = {
                        scope.launch { scrollState.animateScrollToItem(3) }
                    }
                )
                BottomTabItem(
                    icon = Icons.Default.Description,
                    label = "Request",
                    isActive = currentIndex in 5..6,
                    onClick = {
                        scope.launch { scrollState.animateScrollToItem(5) }
                    }
                )
                BottomTabItem(
                    icon = Icons.Default.Person,
                    label = "Profile",
                    isActive = currentIndex >= 7,
                    onClick = {
                        scope.launch { scrollState.animateScrollToItem(7) }
                    }
                )
            }
        }
    }
}

@Composable
fun BottomTabItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) Color(0xFF2563EB) else Color(0xFF9CA3AF),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isActive) Color(0xFF2563EB) else Color(0xFF9CA3AF),
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun CitySelectionModal(
    currentCity: String,
    cities: List<String>,
    onDismiss: () -> Unit,
    onCitySelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCities = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            cities
        } else {
            cities.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header of Modal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Your Region",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "Select your city in Pakistan to filter professionals nearby.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Search Box inside Modal
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search for your city...", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8F9FA),
                        unfocusedContainerColor = Color(0xFFF8F9FA),
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                // Scrollable Cities List
                val displayCities = listOf("All") + filteredCities
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(displayCities) { city ->
                        val isSelected = currentCity.equals(city, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) Color(0xFFEFF6FF) else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFFBFDBFE) else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onCitySelected(city) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (city == "All") Icons.Default.Explore else Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFF2563EB) else Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (city == "All") "All Pakistan" else city,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF1E3A8A) else Color(0xFF334155)
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    if (displayCities.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No cities found matching \"$searchQuery\"",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
