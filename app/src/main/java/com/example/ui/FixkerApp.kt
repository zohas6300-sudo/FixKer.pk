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
import android.location.Geocoder
import java.util.Locale
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import androidx.compose.ui.window.DialogProperties
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
    val custLatitude by viewModel.custLatitude.collectAsState()
    val custLongitude by viewModel.custLongitude.collectAsState()
    val custMapAddress by viewModel.custMapAddress.collectAsState()

    // Form entries (Professional)
    val proName by viewModel.proName.collectAsState()
    val proPhone by viewModel.proPhone.collectAsState()
    val proWhatsApp by viewModel.proWhatsApp.collectAsState()
    val proCity by viewModel.proCity.collectAsState()
    val proProfession by viewModel.proProfession.collectAsState()
    val proExperience by viewModel.proExperience.collectAsState()

    // UI Dialog controllers
    var isUrdu by remember { mutableStateOf(true) }
    var successRequestDialog by remember { mutableStateOf<ServiceRequest?>(null) }
    var successProDialog by remember { mutableStateOf<Professional?>(null) }
    var showLeadsPanel by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showCitySelectionModal by remember { mutableStateOf(false) }
    var showMapDialog by remember { mutableStateOf(false) }

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
                isUrdu = isUrdu,
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
                        isUrdu = isUrdu,
                        onLanguageToggle = { isUrdu = it },
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
                        isUrdu = isUrdu,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onRequestClick = {
                            scope.launch {
                                scrollState.animateScrollToItem(5)
                            }
                        },
                        onWhatsAppClick = {
                            openWhatsAppDirect(context, "923001234567", t(isUrdu, "Hi Fixker.pk! I am looking for verified skilled workers near me.", "السلام علیکم! مجھے اپنے قریب تصدیق شدہ ہنر مند کاریگر کی ضرورت ہے۔"))
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
                    TrustIndicators(isUrdu = isUrdu)
                }

                // 4. Popular Services Section
                item {
                    PopularServicesSection(
                        isUrdu = isUrdu,
                        onServiceSelected = { serviceName ->
                            viewModel.updateCustService(serviceName)
                            viewModel.setServiceFilter(serviceName)
                            Toast.makeText(context, t(isUrdu, "Selected category: $serviceName. View matching pros listed below, or fill request form!", "پیشہ منتخب کیا گیا: ${getDisplayService(serviceName, true)}۔ نیچے دیے گئے ماہرین دیکھیں یا فارم پُر کریں!"), Toast.LENGTH_SHORT).show()
                            scope.launch {
                                // Scroll to Request Form beautifully
                                scrollState.animateScrollToItem(4)
                            }
                        }
                    )
                }

                // 5. How It Works Section
                item {
                    HowItWorksSection(isUrdu = isUrdu)
                }

                // 6. Lead Generation Customer Form
                item {
                    CustomerFormSection(
                        isUrdu = isUrdu,
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
                        latitude = custLatitude,
                        longitude = custLongitude,
                        mapAddress = custMapAddress,
                        onOpenMap = { showMapDialog = true },
                        onClearMap = { viewModel.clearCustLocation() },
                        onSubmit = {
                            viewModel.submitServiceRequest { req ->
                                // Optional fast action
                            }
                        }
                    )
                }

                // 7. Why Choose Us Section
                item {
                    WhyChooseUsSection(isUrdu = isUrdu)
                }

                // 8. Professional Registration Section
                item {
                    ProfessionalRegistrationSection(
                        isUrdu = isUrdu,
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
                        isUrdu = isUrdu,
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
                            isUrdu = isUrdu,
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
                            isUrdu = isUrdu,
                            professional = pro,
                            onWhatsAppClick = {
                                val msg = t(isUrdu,
                                    "Hello ${pro.fullName}, I found your verified profile on Fixker.pk for ${pro.profession} service in ${pro.city}. Are you available?",
                                    "السلام علیکم ${pro.fullName}! میں نے آپ کی تصدیق شدہ پروفائل Fixker.pk پر دیکھی ہے، مجھے ${getDisplayService(pro.profession, true)} کے کام کے لیے آپ کی خدمات درکار ہیں۔ کیا آپ دستیاب ہیں؟"
                                )
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
                        isUrdu = isUrdu,
                        onHireClick = {
                            openWhatsAppDirect(context, "923001234567", t(isUrdu, "Salam! I am looking to hire top-rated expert Muhammad Bilal for electrical work.", "اسلام علیکم! میں الیکٹریکل کام کے لیے محمد بلال (ٹاپ ریٹڈ ایکسپرٹ) کو ہائر کرنا چاہتا ہوں۔"))
                        }
                    )
                }

                // 10. Cities We Serve List
                item {
                    CitiesWeServeSection(
                        isUrdu = isUrdu,
                        cities = pakCities,
                        selectedCity = cityFilter,
                        onCityClick = { city ->
                            viewModel.setCityFilter(city)
                            Toast.makeText(context, t(isUrdu, "Filtering professionals in $city. View featured pros below!", "شہر ${getDisplayCity(city, true)} کے لیے ماہرین فلٹر ہو رہے ہیں!"), Toast.LENGTH_SHORT).show()
                            scope.launch {
                                // Scroll to Featured Professionals header beautiful index (which is index 8)
                                scrollState.animateScrollToItem(9)
                            }
                        }
                    )
                }

                // 11. Customer Reviews / Testimonials
                item {
                    CustomerReviewsSection(isUrdu = isUrdu)
                }

                // 12. Frequently Asked Questions (FAQ)
                item {
                    FAQSection(isUrdu = isUrdu)
                }

                // 13. About Section
                item {
                    AboutSection(isUrdu = isUrdu)
                }

                // 14. Live Lead Database Monitoring Panel Toggle (Admin / Review Tool for AI-Studio)
                item {
                    LiveLeadsTriggerButton(
                        isUrdu = isUrdu,
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
                                    text = t(isUrdu,
                                        "No Service Requests yet. Submit a request above in the Customer Request Form to see it persistent here in real-time!",
                                        "ابھی تک کوئی درخواست موصول نہیں ہوئی۔ یہاں لائیو اپ ڈیٹ دیکھنے کے لیے اوپر کسٹمر ریکویسٹ فارم جمع کروائیں!"
                                    ),
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
                                isUrdu = isUrdu,
                                request = req,
                                onCall = { placePhoneCall(context, req.mobileNumber) },
                                onWhatsApp = {
                                    val text = t(isUrdu,
                                        "Salam ${req.fullName}, regarding your Fixker.pk request for ${req.requiredService} in ${req.city}, ${req.area}.",
                                        "السلام علیکم ${req.fullName}! آپ کی Fixker.pk پر کی جانے والی درخواست برائے ${getDisplayService(req.requiredService, true)} جو کہ ${getDisplayCity(req.city, true)} (${req.area}) کے لیے ہے، کے سلسلے میں رابطہ کیا گیا ہے۔"
                                    )
                                    openWhatsAppDirect(context, req.mobileNumber, text)
                                }
                            )
                        }
                    }
                }

                // 15. Footer Section
                item {
                    FooterSection(
                        isUrdu = isUrdu,
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

    if (showMapDialog) {
        GoogleMapSelectionDialog(
            isUrdu = isUrdu,
            initialCity = custCity,
            onLocationSelected = { lat, lng, address ->
                viewModel.updateCustLocation(lat, lng, address)
                // Autofill area if user picked on map and it was empty
                val areaSuggestion = when {
                    address.contains("Gulberg", ignoreCase = true) -> "Gulberg"
                    address.contains("DHA", ignoreCase = true) -> "DHA"
                    address.contains("Sector F-11", ignoreCase = true) -> "F-11"
                    address.contains("Sector G-11", ignoreCase = true) -> "G-11"
                    address.contains("Madina Town", ignoreCase = true) -> "Madina Town"
                    address.contains("Clifton", ignoreCase = true) -> "Clifton"
                    address.contains("Saddar", ignoreCase = true) -> "Saddar"
                    address.contains("Johar Town", ignoreCase = true) -> "Johar Town"
                    address.contains("Samanabad", ignoreCase = true) -> "Samanabad"
                    address.contains("Blue Area", ignoreCase = true) -> "Blue Area"
                    else -> ""
                }
                if (areaSuggestion.isNotEmpty()) {
                    viewModel.updateCustArea(areaSuggestion)
                }
                showMapDialog = false
            },
            onDismiss = { showMapDialog = false },
            context = context
        )
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
                        text = t(isUrdu, "Request Submitted!", "درخواست جمع کر دی گئی ہے!"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isUrdu) {
                            "پیارے/پیاری ${req.fullName}، آپ کی ${getDisplayCity(req.city, true)} میں ${getDisplayService(req.requiredService, true)} کی درخواست ہمیں موصول ہو گئی ہے۔ ہماری ٹیم جلد سے جلد آپ کو بہترین قریبی ہنر مند کاریگر سے ملا رہی ہے۔"
                        } else {
                            "Salam ${req.fullName}, your service request for ${req.requiredService} in ${req.city} has been received. Our team is matching you with local, highly-skilled professionals right now."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Divider(color = BorderLight)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = t(isUrdu, "Instant Match Assistance:", "فوری کاریگر سے بات چیت:"),
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
                            val textMsg = t(isUrdu,
                                "Salam Fixker! I just submitted a service request ID #${req.id} for ${req.requiredService} in ${req.city} (${req.area}). Please match me quickly.",
                                "سلام فکسر! میں نے ابھی سروس کی درخواست (ID #${req.id}) برائے ${getDisplayService(req.requiredService, true)} جو کہ ${getDisplayCity(req.city, true)} (${req.area}) کے لیے ہے، جمع کروائی ہے۔ مہربانی فرما کر میرا قریبی کاریگر سے رابطہ کروائیں۔"
                            )
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
                        Text(t(isUrdu, "Connect via WhatsApp Now", "ابھی واٹس ایپ سے رابطہ کریں"), fontWeight = FontWeight.Bold)
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
                        Text(t(isUrdu, "Call Direct Helpline", "مددگار ہیلپ لائن پر کال کریں"), color = DeepBlue40, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { successRequestDialog = null }) {
                        Text(t(isUrdu, "Close", "بند کریں"), color = TextGray)
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
                        text = t(isUrdu, "Registration Submitted!", "رجسٹریشن فارم جمع کر دیا گیا!"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isUrdu) {
                            "خوش آمدید ${pro.fullName}! پاکستان کے سب سے بڑے اور تیز ترین نیٹ ورک میں بطورِ تجربہ کار ${getDisplayService(pro.profession, true)} شامل ہونے کی آپ کی درخواست موصول ہو گئی ہے۔"
                        } else {
                            "Welcome ${pro.fullName}! Your request to join Pakistan's Fasting Growing Professional network as an experienced ${pro.profession} has been recorded."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = t(isUrdu, "Status: Verification Pending", "حیثیت: تصدیق زیرِ التوا"),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = CardOrange
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            successProDialog = null
                            val welcomeText = t(isUrdu,
                                "Salam Fixker Support! I registered as an experienced ${pro.profession} in ${pro.city}. Let's proceed with verification.",
                                "سلام فکسر سپورٹ! میں نے بطورِ تجربہ کار ${getDisplayService(pro.profession, true)} شہر ${getDisplayCity(pro.city, true)} سے رجسٹریشن کروائی ہے۔ مہربانی فرما کر میری تصدیق شروع کریں۔"
                            )
                            openWhatsAppDirect(context, "923001234567", welcomeText)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(t(isUrdu, "Fast-Track My Verification", "میری تصدیق فوری طور پر شروع کریں"), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = { successProDialog = null }) {
                        Text(t(isUrdu, "Done", "مکمل"), color = TextGray)
                    }
                }
            }
        }
    }

    if (showCitySelectionModal) {
        CitySelectionModal(
            isUrdu = isUrdu,
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
    isUrdu: Boolean,
    onLanguageToggle: (Boolean) -> Unit,
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

            // Quick actions on right: Location Badge and Language Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Location Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(100.dp))
                        .clickable { onCityClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFEF4444), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedCity.equals("All", ignoreCase = true)) (if (isUrdu) "پاکستان" else "Pakistan") else getDisplayCity(selectedCity, isUrdu),
                        fontSize = 11.sp,
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

                Spacer(modifier = Modifier.width(6.dp))

                // Language Switcher Chip using standard M3 style colors
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(100.dp))
                        .clickable { onLanguageToggle(!isUrdu) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isUrdu) "English" else "اردو",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }
            }
        }
    }
}

@Composable
fun HeroSection(
    isUrdu: Boolean,
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
            text = t(isUrdu, "Trusted Professionals\nat Your Doorstep", "بھروسہ مند کاریگر\nآپ کی دہلیز پر"),
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
            text = t(isUrdu, "\"Pakistan's Trusted Network of Skilled Professionals\"", "\"پاکستان کا سب سے معتبر اور بڑا کاریگر نیٹ ورک\""),
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
            placeholder = { Text(t(isUrdu, "Search for Electrician, Plumber...", "الیکٹریشن، پلمبر، اے سی میکر تلاش کریں..."), fontSize = 13.sp, color = Color(0xFF94A3B8)) },
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
fun TrustIndicators(isUrdu: Boolean) {
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
                Text(t(isUrdu, "Verified", "تصدیق شدہ"), fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8), fontSize = 13.sp)
                Text(t(isUrdu, "Pros Only", "ویریفائیڈ کاریگر"), color = Color(0xFF2563EB), fontSize = 10.sp, fontWeight = FontWeight.Medium)
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
                Text(t(isUrdu, "Fast", "سپر فاسٹ"), fontWeight = FontWeight.Bold, color = Color(0xFF15803D), fontSize = 13.sp)
                Text(t(isUrdu, "15m Response", "15 منٹ میں رابطہ"), color = Color(0xFF16A34A), fontSize = 10.sp, fontWeight = FontWeight.Medium)
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
                Text(t(isUrdu, "Affordable", "مناسب ریٹ"), fontWeight = FontWeight.Bold, color = Color(0xFFC2410C), fontSize = 13.sp)
                Text(t(isUrdu, "Flat Rates", "کوئی اضافی ریٹ نہیں"), color = Color(0xFFEA580C), fontSize = 10.sp, fontWeight = FontWeight.Medium)
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
                Text(t(isUrdu, "No Markup", "سیدھی ڈیل"), fontWeight = FontWeight.Bold, color = Color(0xFF6B21A8), fontSize = 13.sp)
                Text(t(isUrdu, "Pay Direct", "براہ راست نقد ادائیگی"), color = Color(0xFF8B5CF6), fontSize = 10.sp, fontWeight = FontWeight.Medium)
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
    isUrdu: Boolean,
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
                    text = t(isUrdu, "Popular Services", "مقبول ترین سروسز"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = t(isUrdu, "Click a category to match nearby professionals instantly", "فوری رابطہ کرنے کے لیے کسی بھی شعبے پر کلک کریں"),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
            Text(
                text = t(isUrdu, "SEE ALL", "سب دیکھیں"),
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
                            text = if (isUrdu) getDisplayService(item.idName, true).substringBefore(" ") else item.idName.substringBefore(" "),
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
fun HowItWorksSection(isUrdu: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLight)
            .padding(24.dp)
    ) {
        Text(
            text = t(isUrdu, "How It Works", "کام کا طریقہ کار"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = t(isUrdu, "Get connected in 4 easy steps", "4 آسان طریقوں سے قریبی کاریگر سے رابطہ کریں"),
            style = MaterialTheme.typography.bodySmall,
            color = TextGray,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp)
        )

        StepRow(
            step = "1",
            title = t(isUrdu, "Submit Your Service Request", "اپنی سروس کی درخواست بھیجیں"),
            description = t(isUrdu, "Fill the short form with your city, area, and problem details.", "صرف ایک منٹ کا آسان فارم پُر کریں اور اپنے کام کی تفصیل بتائیں۔")
        )
        Spacer(modifier = Modifier.height(16.dp))
        StepRow(
            step = "2",
            title = t(isUrdu, "We Match You with Nearby Pros", "ہم بہترین قریبی کاریگر تلاش کریں گے"),
            description = t(isUrdu, "Our automated matching connects verified experts close to your area.", "ہمارا فکسر سسٹم آپ کے علاقے کے تصدیق شدہ ماہرین سے رابطہ ملائے گا۔")
        )
        Spacer(modifier = Modifier.height(16.dp))
        StepRow(
            step = "3",
            title = t(isUrdu, "Receive a Call or WhatsApp Response", "فون یا واٹس ایپ پر رابطہ موصول کریں"),
            description = t(isUrdu, "Professionals will contact you with affordable and direct custom service quotes.", "متعلقہ کاریگر براہِ راست آپ سے رابطہ کر کے مناسب ریٹ کا تخمینہ بتائیں گے۔")
        )
        Spacer(modifier = Modifier.height(16.dp))
        StepRow(
            step = "4",
            title = t(isUrdu, "Get Your Job Done Quickly", "اپنا کام تسلی بخش مکمل کروائیں"),
            description = t(isUrdu, "Relax as skilled workers complete your needs with guaranteed satisfaction.", "ہنر مند کاریگر نہایت ذمہ داری کے ساتھ آپ کا کام مکمل کریں گے۔")
        )
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
fun WhyChooseUsSection(isUrdu: Boolean) {
    val items = listOf(
        WhyItemData(
            Icons.Default.Security,
            t(isUrdu, "Verified Professionals", "تصدیق شدہ اور محفوظ ہنر مند"),
            t(isUrdu, "Every worker is verified with local NIC ID database audits, ensuring extreme safety.", "ہر کاریگر کی نادرا شناختی کارڈ کے ذریعے تصدیق کی جاتی ہے تاکہ آپ کا گھر اور فیملی محفوظ رہے۔")
        ),
        WhyItemData(
            Icons.Default.Timer,
            t(isUrdu, "Fast 15-Min Response", "15 منٹ میں تندہی سے رابطہ"),
            t(isUrdu, "Nearby matching algorithm triggers callbacks within minutes, solving emergency issues quickly.", "ہمارا سسٹم منٹوں میں قریبی کاریگر کو مطلع کرتا ہے جو فوری طور پر آپ سے رابطہ کرتا ہے۔")
        ),
        WhyItemData(
            Icons.Default.PinDrop,
            t(isUrdu, "Local Pakistani Experts", "مقامی ہنر مند کاریگر"),
            t(isUrdu, "Hire artisans who understand specific local designs, fitting plumbing systems and power grids.", "ایسے ہنر مند ہائر کریں جو پاکستان کے بجلی، گیس، سینیٹری اور دیگر سسٹمز کو بخوبی سمجھتے ہوں۔")
        ),
        WhyItemData(
            Icons.Default.Loyalty,
            t(isUrdu, "Affordable Rates", "سب سے مناسب اور سستے ریٹ"),
            t(isUrdu, "Communicate with professionals directly. No third-party marketplace commissions mean lower cost for you.", "کاریگروں سے براہِ راست ڈیل کریں۔ کسی تیسرے ادارے یا کمیشن ایجنٹ کا عمل دخل نہ ہونے سے آپ کے پیسے بچتے ہیں۔")
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = t(isUrdu, "Why Choose Fixker.pk", "ہمیں کیوں منتخب کریں؟"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = t(isUrdu, "Pakistan's leading trusted lead-gen marketplace", "پاکستان کا مقبول اور قابلِ اعتماد فکسنگ نیٹ ورک"),
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
    isUrdu: Boolean,
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
    latitude: Double? = null,
    longitude: Double? = null,
    mapAddress: String? = null,
    onOpenMap: () -> Unit,
    onClearMap: () -> Unit,
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
                text = t(isUrdu, "Contact Verified Experts Now", "تصدیق شدہ کاریگروں سے ابھی رابطہ کریں"),
                style = MaterialTheme.typography.titleLarge,
                color = DeepBlue40,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            Text(
                text = t(isUrdu, "Get calls and WhatsApp replies in minutes.", "منٹوں میں فون کال اور واٹس ایپ پر جواب حاصل کریں۔"),
                style = MaterialTheme.typography.bodySmall,
                color = TextGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Input: Full Name
            OutlinedTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = { Text(t(isUrdu, "Full Name", "پورا نام")) },
                placeholder = { Text(t(isUrdu, "e.g. Akbar Khan", "مثلاً اکبر خان")) },
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
                label = { Text(t(isUrdu, "Mobile Phone Number", "موبائل فون نمبر")) },
                placeholder = { Text(t(isUrdu, "e.g. 03001234567", "مثلاً 03001234567")) },
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
                            value = getDisplayCity(selectedCity, isUrdu),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(t(isUrdu, "City", "شہر")) },
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
                                    text = { Text(getDisplayCity(city, isUrdu)) },
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
                    label = { Text(t(isUrdu, "Area / Sector", "علاقہ یا سیکٹر")) },
                    placeholder = { Text(t(isUrdu, "e.g. DHA Phase 5", "مثلاً ڈی ایچ اے، گلبرگ")) },
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
                        value = getDisplayService(selectedService, isUrdu),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(t(isUrdu, "Required Service", "مطلوبہ سروس")) },
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
                                text = { Text(getDisplayService(s, isUrdu)) },
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
                label = { Text(t(isUrdu, "Short description of problem", "مسئلے کی مختصر تفصیل")) },
                placeholder = { Text(t(isUrdu, "Explain shortly what needs fixing (e.g. kitchen water outlet is broken and leaks)", "تفصیل بتائیں کہ کیا کام کروانا ہے (مثلاً کچن کا پائپ یا بجلی فرج خراب ہے)")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("cust_desc_input")
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(8.dp)
            )

            // Google Map Location Picker Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = t(isUrdu, "Site Location", "نقشے پر کام کی جگہ"),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        if (latitude != null && longitude != null) {
                            Text(
                                text = mapAddress ?: "Pinned Location",
                                style = MaterialTheme.typography.bodySmall,
                                color = Emerald40,
                                maxLines = 2,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "GPS: (${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray
                            )
                        } else {
                            Text(
                                text = t(isUrdu, "Pin location on Google Maps for fast arrival.", "مکان تلاش کرنے میں آسانی کے لیے نقشہ استعمال کریں۔"),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray
                            )
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (latitude != null && longitude != null) {
                            IconButton(onClick = onClearMap) {
                                Icon(Icons.Default.Close, contentDescription = "Clear location", tint = Color.Red, modifier = Modifier.size(20.dp))
                            }
                        }
                        Button(
                            onClick = onOpenMap,
                            colors = ButtonDefaults.buttonColors(containerColor = if (latitude != null) Emerald40 else DeepBlue40),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("cust_open_map_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (latitude != null) t(isUrdu, "Change Pin", "تبدیل کریں") else t(isUrdu, "Open Map", "نقشہ کھولیں"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

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
                    text = t(isUrdu, "Get Connected Now", "کاریگر سے ابھی رابطہ کریں"),
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
    isUrdu: Boolean,
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
            text = t(isUrdu, "Join Pakistan's Fastest Growing Professional Network", "پاکستان کے سب سے بڑے ہنرمندوں کے نیٹ ورک کا حصہ بنیں"),
            style = MaterialTheme.typography.titleLarge,
            color = DeepBlue40,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center
        )

        Text(
            text = t(isUrdu, "Register as an independent artisan and receive direct premium customer orders from Lahore, Karachi, and major cities.", "بطورِ آزاد کاریگر رجسٹریشن کروائیں اور لاہور، کراچی اور ملک بھر سے گاہکوں کے آرڈر براہِ راست حاصل کریں۔"),
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
            label = { Text(t(isUrdu, "Full Name", "پورا نام")) },
            placeholder = { Text(t(isUrdu, "e.g. Ustaad Muhammad", "مثلاً استاد محمد/علی")) },
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
                label = { Text(t(isUrdu, "Phone Number", "فون نمبر")) },
                placeholder = { Text(t(isUrdu, "e.g. 03001234567", "مثلاً 03001234567")) },
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
                label = { Text(t(isUrdu, "WhatsApp No", "واٹس ایپ نمبر")) },
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
                        value = getDisplayCity(selectedCity, isUrdu),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(t(isUrdu, "City Base", "مستقر شہر")) },
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
                                text = { Text(getDisplayCity(city, isUrdu)) },
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
                        value = getDisplayService(selectedProfession, isUrdu),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(t(isUrdu, "My Profession", "میرا پیشہ/ہنر")) },
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
                                text = { Text(getDisplayService(p, isUrdu)) },
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
            label = { Text(t(isUrdu, "Years of Experience", "تجربہ (سالوں میں)")) },
            placeholder = { Text(t(isUrdu, "e.g. 5", "مثلاً 5")) },
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
            Text(t(isUrdu, "Register as Professional", "بطورِ کاریگر رجسٹریشن مکمل کریں"), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeaturedProfessionalsHeader(
    isUrdu: Boolean,
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
            text = t(isUrdu, "Featured Professionals in Pakistan", "پاکستان کے نامور اور تصدیق شدہ کاریگر"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = TextDark
        )

        Text(
            text = t(isUrdu, "Browse, contact directly, and choose near you. Dynamic database handles verification credentials.", "اپنے قریبی کاریگر سے براہِ راست رابطہ کریں۔ ہمارے فکسر ریٹنگ سسٹم کے تحت تمام کاریگروں کا ریکارڈ موجود ہے۔"),
            style = MaterialTheme.typography.bodySmall,
            color = TextGray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // City Filters Horizontal Row
        Text(
            text = t(isUrdu, "Filter by City:", "شہر کے لحاظ سے فلٹر کریں:"),
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
                    label = { Text(if (city == "All") t(isUrdu, "All Pakistan", "پورے پاکستان") else getDisplayCity(city, isUrdu)) },
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
            text = t(isUrdu, "Filter by Skill:", "ہنر/کام کے لحاظ سے فلٹر کریں:"),
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
                    label = { Text(if (service == "All") t(isUrdu, "All Services", "تمام سروسز") else getDisplayService(service, isUrdu)) },
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
fun EmptyProfessionalsState(
    isUrdu: Boolean,
    onReset: () -> Unit
) {
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
                text = t(isUrdu, "No professionals found matching current filters.", "منتخب کردہ فلٹرز کے مطابق کوئی کاریگر دستیاب نہیں ہے۔"),
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = t(isUrdu, "Try clearing filters to find all registered technicians.", "تمام دستیاب ترین ہنرمندوں کو دیکھنے کے لیے فلٹر ختم کریں۔"),
                style = MaterialTheme.typography.bodySmall,
                color = TextGray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(containerColor = DeepBlue40)
            ) {
                Text(t(isUrdu, "Show All Professionals", "تمام کاریگر دکھائیں"))
            }
        }
    }
}

@Composable
fun ProfessionalProfileCard(
    isUrdu: Boolean,
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
                                text = t(isUrdu, "Vetting", "تصدیق جاری"),
                                fontSize = 8.sp,
                                color = CardOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${getDisplayService(professional.profession, isUrdu)} • ${professional.yearsOfExperience} " + t(isUrdu, "Years Exp", "سالہ تجربہ"),
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
                        text = getDisplayCity(professional.city, isUrdu),
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
                    Text(t(isUrdu, "WhatsApp", "واٹس ایپ"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                    Text(t(isUrdu, "Call", "کال کریں"), fontSize = 11.sp, color = DeepBlue40, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CitiesWeServeSection(
    isUrdu: Boolean,
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
            text = t(isUrdu, "Proudly Serving Major Pakistani Cities", "ہمارے نیٹ ورک میں شامل اہم ترین شہر"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = TextDark,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = t(isUrdu, "Click a city below to view nearby certified professionals instantly.", "اپنے شہر کا انتخاب کریں اور قریبی تصدیق شدہ کاریگروں سے رابطہ کریں۔"),
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
                                text = getDisplayCity(city, isUrdu),
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
fun CustomerReviewsSection(isUrdu: Boolean) {
    val reviews = listOf(
        ReviewData(
            t(isUrdu, "Zahid Farooq", "زاہد فاروق"),
            t(isUrdu, "Lahore, DHA Phase 6", "لاہور، ڈی ایچ اے فیز 6"),
            5,
            t(
                isUrdu,
                "Salam standard electricians here are phenomenal! Muhammad Ali came of the database request tool within 20 mins to fix our inverter AC unit. Saved us big money with zero commissions!",
                "اسلام علیکم، فکسر کے الیکٹریشن انتہائی لاجواب ہیں! محمد علی بھائی صرف 20 منٹ میں ہمارے گھر پہنچے اور اے سی کا مسئلہ حل کیا، وہ بھی کسی کمیشن کے بغیر!"
            )
        ),
        ReviewData(
            t(isUrdu, "Ayesha Siddiqua", "عائشہ صدیقہ"),
            t(isUrdu, "Karachi, Clifton", "کراچی، کلفٹن"),
            5,
            t(
                isUrdu,
                "I submitted a plumbing request on Fixker.pk on Sunday. Sajid was matched near our home within 10 mins. Exceptional service quality, polite behavior, and reasonable pricing.",
                "میں نے اتوار کے دن پلمبر کے لیے یہاں درخواست دی تھی اور ساجد بھائی صرف 10 منٹ میں Clifton میں ہمارے گھر حاضر ہوگئے۔ ان کا کام بہت اچھا اور ریٹ بھی مناسب تھا۔"
            )
        ),
        ReviewData(
            t(isUrdu, "Raja Hammad", "راجہ حماد"),
            t(isUrdu, "Islamabad, G-11", "اسلام آباد، جی الیون"),
            4,
            t(
                isUrdu,
                "Highly recommended startup! Tested their lead matcher for fitting solar panels, they assigned Tariq. He did a clean job. Very trustworthy workers.",
                "بہترین سروس ہے! ہم نے سولر پینل کی فٹنگ کے لیے یہاں رابطہ کیا تھا اور انہوں نے طارق بھائی کو بھیجا جنہوں نے نہایت صفائی سے سارا کام مکمل کیا۔ بہت ہی ایماندار لوگ ہیں۔"
            )
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = t(isUrdu, "Trusted Across Pakistan", "پورے پاکستان سے کسٹمرز کا اعتماد"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = t(isUrdu, "Realistic testimonials from actual households", "ہمارے صارفین اور عوامِ پاکستان کی آراء"),
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
fun FAQSection(isUrdu: Boolean) {
    val faqs = listOf(
        FaqData(
            t(isUrdu, "What is Fixker.pk?", "فکسر (Fixker.pk) کیا ہے؟"),
            t(
                isUrdu,
                "Fixker.pk is Pakistan's premier trusted network connecting households with verified local skilled technicians (electricians, plumbers, carpenters, etc.) anywhere in Pakistan. You submit a lead request, and local professionals call or WhatsApp you directly.",
                "فکسر پاکستان کا سب سے بڑا ہنرمندوں کا نیٹ ورک ہے جہاں آپ کو الیکٹریشن، پلمبر، کارپینٹر اور دیگر کاریگر ملتے ہیں۔ آپ فارم بھرتے ہیں اور قریبی کاریگر خود آپ سے فون یا واٹس ایپ پر رابطہ کرتے ہیں۔"
            )
        ),
        FaqData(
            t(isUrdu, "Do you charge any commission?", "کیا آپ گاہکوں سے کمیشن لیتے ہیں؟"),
            t(
                isUrdu,
                "Our marketplace is commission-free for household leads! Customers pay workers directly, which lowers service charges and provides fair wages for pakistani independent professionals.",
                "جی نہیں! فکسر نیٹ ورک کسٹمرز کے لیے بالکل مفت ہے۔ گاہک براہِ راست کاریگر کو پیسے دیتے ہیں جس سے پیسے بھی بچتے ہیں اور کاریگر کی مکمل مزدوری بھی اس تک پہنچتی ہے۔"
            )
        ),
        FaqData(
            t(isUrdu, "How do you verify professionals?", "کام کرنے والوں کی تصدیق کیسے ہوتی ہے؟"),
            t(
                isUrdu,
                "Artisans undergo a thorough vetting process including verification with CNIC cards, past references checks, and strict review tracking before they receive a verified badge on our dashboard.",
                "تمام کاریگروں کی نادرا شناختی کارڈ (CNIC)، ماضی کے ریکارڈ اور کسٹمرز کی ریٹنگز کے ذریعے تصدیق کی جاتی ہے تاکہ سیکیورٹی پر کوئی سمجھوتہ نہ ہو۔"
            )
        ),
        FaqData(
            t(isUrdu, "What cities are supported?", "کون کون سے شہر شامل ہیں؟"),
            t(
                isUrdu,
                "We actively cover Lahore, Karachi, Islamabad, Rawalpindi, Multan, Faisalabad, Gujranwala, Sialkot, Peshawar, Bahawalpur, and other major cities.",
                "ہم لاہور، کراچی، اسلام آباد، راولپنڈی، ملتان، فیصل آباد، گوجرانوالہ، سیالکوٹ، پشاور، بہاولپور اور دیگر بڑے شہروں میں خدمات فراہم کر رہے ہیں۔"
            )
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLight.copy(alpha = 0.5f))
            .padding(24.dp)
    ) {
        Text(
            text = t(isUrdu, "Frequently Asked Questions", "عام طور پر پوچھے جانے والے سوالات"),
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
fun AboutSection(isUrdu: Boolean) {
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
            text = t(isUrdu, "About Fixker.pk", "فکسر (Fixker.pk) کے بارے میں"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = t(
                isUrdu,
                "Our mission is to make it easy for every Pakistani household and business to find trusted service professionals quickly and safely. By prioritizing CNIC verification and bypassing heavy middleman commission platforms, we champion localized micro-entrepreneurs while providing homeowners superior reliability, safety, and swift repair completions.",
                "ہمارا عزم پاکستان کے ہر گھر اور کاروبار کے لیے سیکیورٹی اور سکون کے ساتھ مقامی ماہر کاریگر تلاش کرنا آسان بنانا ہے۔ نادرا شناختی کارڈ تصدیق کو اولیت دے کر اور کسی تیسرے ادارے کا کمیشن ختم کر کے، ہم مقامی ہنرمندوں کو مناسب روزگار اور آپ کو بہترین و سستی سروسز فراہم کرتے ہیں۔"
            ),
            style = MaterialTheme.typography.bodySmall,
            color = TextGray,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun LiveLeadsTriggerButton(
    isUrdu: Boolean,
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
                    Text(text = t(isUrdu, "Review Registered Leads", "حالیہ کسٹمرز کے آرڈرز دیکھیں"), fontWeight = FontWeight.Bold, color = TextDark, fontSize = 14.sp)
                    Text(text = t(isUrdu, "Total Artisans Registered: $proCount", "کُل رجسٹرڈ کاریگر: $proCount"), fontSize = 11.sp, color = TextGray)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (expanded) t(isUrdu, "Hide Leads", "چھپائیں") else t(isUrdu, "Inspect Leads", "تفصیل دیکھیں"),
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
    isUrdu: Boolean,
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
                    Text(text = "${getDisplayCity(request.city, isUrdu)} (${t(isUrdu, "Area", "علاقہ")}: ${request.area})", fontSize = 12.sp, color = TextGray)
                }

                Box(
                    modifier = Modifier
                        .background(SuccessGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = getDisplayService(request.requiredService, isUrdu), fontWeight = FontWeight.Bold, fontSize = 10.sp, color = SuccessGreen)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = t(isUrdu, "Problem description: ", "کام کی تفصیل : ") + request.description,
                fontSize = 12.sp,
                color = TextDark
            )

            if (!request.mapAddress.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PinDrop,
                        contentDescription = null,
                        tint = DeepBlue40,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = request.mapAddress,
                        fontSize = 11.sp,
                        color = TextGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = t(isUrdu, "Lead persistent in SQLite Room", "درخواست لوکل ڈیٹا بیس میں محفوظ ہے"),
                    fontSize = 10.sp,
                    color = NeutralGray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    if (request.latitude != null && request.longitude != null) {
                        val context = LocalContext.current
                        IconButton(
                            onClick = {
                                try {
                                    val uri = "geo:${request.latitude},${request.longitude}?q=${request.latitude},${request.longitude}(${request.fullName})"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No maps app installed", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Navigate to Location", tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

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
    isUrdu: Boolean,
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
                Text(t(isUrdu, "Pakistan's Trusted Skilled Network", "پاکستان کا بااعتماد ہنرمندوں کا نیٹ ورک"), color = Emerald80, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            // Quick Links
            Column(horizontalAlignment = Alignment.End) {
                FooterLinkText(text = t(isUrdu, "Home", "ہوم پیج"), onClick = onHomeClick)
                FooterLinkText(text = t(isUrdu, "Our Services", "ہماری سروسز"), onClick = onServicesClick)
                FooterLinkText(text = t(isUrdu, "Submit Request", "سروس درخواست بھیجیں"), onClick = onRequestClick)
                FooterLinkText(text = t(isUrdu, "Join as Professional", "بطورِ کاریگر شامل ہوں"), onClick = onJoinClick)
                FooterLinkText(text = t(isUrdu, "FAQs", "سوالات و جوابات"), onClick = onFAQClick)
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
                text = t(isUrdu, "© 2026 Fixker.pk. All CNIC Data Reserved.", "© 2026 فکسر۔ جملہ حقوق محفوظ ہیں۔"),
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
    isUrdu: Boolean,
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
                        text = t(isUrdu, "Muhammad Bilal", "محمد بلال"),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Text(
                        text = getDisplayService("Electrician", isUrdu) + " • 8 " + t(isUrdu, "yrs Exp.", "سالہ تجربہ"),
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
                                text = t(isUrdu, "TOP RATED", "اعلیٰ ریٹنگ"),
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
                    Text(t(isUrdu, "HIRE NOW", "رابطہ کریں"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun StickyBottomNavBarScroll(
    isUrdu: Boolean,
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
                        openWhatsAppDirect(context, "923001234567", t(isUrdu, "Hi Fixker.pk! I am looking for verified skilled workers near me.", "السلام علیکم! مجھے اپنے قریب تصدیق شدہ ہنر مند کاریگر کی ضرورت ہے۔"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(t(isUrdu, "WhatsApp Now", "واٹس ایپ کریں"), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
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
                    label = t(isUrdu, "Home", "ہوم"),
                    isActive = currentIndex < 3,
                    onClick = {
                        scope.launch { scrollState.animateScrollToItem(0) }
                    }
                )
                BottomTabItem(
                    icon = Icons.Default.Build,
                    label = t(isUrdu, "Services", "سروسز"),
                    isActive = currentIndex in 3..4,
                    onClick = {
                        scope.launch { scrollState.animateScrollToItem(3) }
                    }
                )
                BottomTabItem(
                    icon = Icons.Default.Description,
                    label = t(isUrdu, "Request", "درخواست"),
                    isActive = currentIndex in 5..6,
                    onClick = {
                        scope.launch { scrollState.animateScrollToItem(5) }
                    }
                )
                BottomTabItem(
                    icon = Icons.Default.Person,
                    label = t(isUrdu, "Profile", "پروفائل"),
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
    isUrdu: Boolean,
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
                        text = t(isUrdu, "Select Your Region", "اپنا علاقہ منتخب کریں"),
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
                    text = t(isUrdu, "Select your city in Pakistan to filter professionals nearby.", "اپنے قریبی ہنرمند کاریگروں کو دیکھنے کے لیے اپنا شہر منتخب کریں۔"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Search Box inside Modal
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(t(isUrdu, "Search for your city...", "اپنا شہر تلاش کریں..."), fontSize = 13.sp, color = Color(0xFF94A3B8)) },
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
                                    text = if (city == "All") t(isUrdu, "All Pakistan", "پورے پاکستان") else getDisplayCity(city, isUrdu),
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
                                    text = t(isUrdu, "No cities found matching \"$searchQuery\"", "تلاش کے مطابق کوئی شہر نہیں ملا: \"$searchQuery\""),
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

// ==================== TRANSLATION LOOKUP UTILITIES ====================

fun t(isUrdu: Boolean, en: String, ur: String): String = if (isUrdu) ur else en

fun getDisplayCity(city: String, isUrdu: Boolean): String {
    if (!isUrdu) return city
    return when (city) {
        "Lahore" -> "لاہور (Lahore)"
        "Karachi" -> "کراچی (Karachi)"
        "Islamabad" -> "اسلام آباد (Islamabad)"
        "Rawalpindi" -> "راولپنڈی (Rawalpindi)"
        "Multan" -> "ملتان (Multan)"
        "Faisalabad" -> "فیصل آباد (Faisalabad)"
        "Gujranwala" -> "گوجرانوالہ (Gujranwala)"
        "Sialkot" -> "سیالکوٹ (Sialkot)"
        "Peshawar" -> "پشاور (Peshawar)"
        "Bahawalpur" -> "بہاولپور (Bahawalpur)"
        "All" -> "تمام شہر"
        else -> city
    }
}

fun getDisplayService(service: String, isUrdu: Boolean): String {
    if (!isUrdu) return service
    val trimmed = service.trim()
    return when {
        trimmed.equals("Electrician", ignoreCase = true) -> "الیکٹریشن (بجلی کا کام)"
        trimmed.equals("Plumber", ignoreCase = true) -> "پلمبر (سینیٹری/پانی کا کام)"
        trimmed.equals("AC Technician", ignoreCase = true) -> "اے سی ٹیکنیشن (AC سروس)"
        trimmed.equals("Solar Installer", ignoreCase = true) -> "سولر پینل فٹنگ"
        trimmed.equals("Carpenter", ignoreCase = true) -> "کارپینٹر (لکڑی کا کام)"
        trimmed.equals("Painter", ignoreCase = true) -> "پینٹر (رنگ روغن)"
        trimmed.equals("Handyman", ignoreCase = true) -> "ہینڈی مین (متفرق کام)"
        trimmed.equals("Welder", ignoreCase = true) -> "ویلڈر (لوہے کا کام)"
        trimmed.equals("All", ignoreCase = true) -> "تمام سروسز"
        else -> service
    }
}

// ==================== GOOGLE MAPS SELECTION FEATURE ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleMapSelectionDialog(
    isUrdu: Boolean,
    initialCity: String,
    onLocationSelected: (Double, Double, String) -> Unit,
    onDismiss: () -> Unit,
    context: Context
) {
    val coroutineScope = rememberCoroutineScope()
    val initialLatLng = remember(initialCity) {
        when (initialCity.lowercase()) {
            "lahore" -> LatLng(31.5204, 74.3587)
            "karachi" -> LatLng(24.8607, 67.0011)
            "islamabad" -> LatLng(33.6844, 73.0479)
            "rawalpindi" -> LatLng(33.5984, 73.0441)
            "multan" -> LatLng(30.1575, 71.5249)
            "faisalabad" -> LatLng(31.4504, 73.1350)
            "peshawar" -> LatLng(33.9971, 71.4784)
            "sialkot" -> LatLng(32.4972, 74.5361)
            "gujranwala" -> LatLng(32.1877, 74.1945)
            "bahawalpur" -> LatLng(29.3544, 71.6911)
            else -> LatLng(31.5204, 74.3587)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 15f)
    }

    val quickAreas = remember(initialCity) {
        when (initialCity.lowercase()) {
            "lahore" -> listOf(
                "Gulberg" to LatLng(31.5204, 74.3587),
                "DHA Phase 6" to LatLng(31.4697, 74.4533),
                "Model Town" to LatLng(31.4805, 74.3256),
                "Johar Town" to LatLng(31.4697, 74.2973),
                "Samanabad" to LatLng(31.5362, 74.3013)
            )
            "karachi" -> listOf(
                "Clifton" to LatLng(24.8138, 67.0336),
                "DHA Phase 6" to LatLng(24.7956, 67.0674),
                "Gulshan-e-Iqbal" to LatLng(24.9180, 67.0970),
                "Saddar" to LatLng(24.8601, 67.0195)
            )
            "islamabad" -> listOf(
                "Blue Area" to LatLng(33.7118, 73.0683),
                "Sector F-11" to LatLng(33.6841, 72.9866),
                "Sector G-11" to LatLng(33.6657, 73.0039),
                "Centaurus Mall" to LatLng(33.7077, 73.0502)
            )
            "faisalabad" -> listOf(
                "Madina Town" to LatLng(31.4358, 73.1167),
                "People's Colony" to LatLng(31.4087, 73.1023),
                "D-Ground" to LatLng(31.4162, 73.1105)
            )
            "rawalpindi" -> listOf(
                "Saddar" to LatLng(33.5936, 73.0534),
                "Bahria Phase 7" to LatLng(33.5152, 73.0805),
                "Chaklala Scheme 3" to LatLng(33.5759, 73.1018)
            )
            "multan" -> listOf(
                "Gulgasht Colony" to LatLng(30.2224, 71.4912),
                "Multan Cantt" to LatLng(30.1945, 71.4423),
                "Shah Rukn-e-Alam" to LatLng(30.2104, 71.5034)
            )
            "peshawar" -> listOf(
                "Hayatabad" to LatLng(33.9805, 71.4357),
                "Peshawar Cantt" to LatLng(33.9991, 71.5243),
                "University Road" to LatLng(33.9912, 71.4883)
            )
            else -> listOf(
                "Center" to initialLatLng
            )
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var currentAddress by remember { mutableStateOf(t(isUrdu, "Pinning workspace location...", "ملازمت کی جگہ نشان زد کر رہے ہیں...")) }
    val currentTarget = cameraPositionState.position.target

    LaunchedEffect(currentTarget) {
        currentAddress = reverseGeocodeInPakistan(context, currentTarget.latitude, currentTarget.longitude, isUrdu)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = t(isUrdu, "Pin Customer Site Address", "صارف کی دکان یا گھر کا پتہ منتخب کریں"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepBlue40
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close Map", tint = TextGray)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar Row
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(t(isUrdu, "Search area (e.g. Liberty Market, Lahore)...", "سیکٹر یا جگہ سرچ کریں...")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("map_search_input"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Search
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSearch = {
                            if (searchQuery.isNotBlank()) {
                                val resolved = geocodeInPakistan(context, searchQuery)
                                if (resolved != null) {
                                    coroutineScope.launch {
                                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(resolved, 15f))
                                    }
                                } else {
                                    Toast.makeText(context, t(isUrdu, "Cannot find coordinates, dragging map manually is advised.", "مقام نہیں ملا۔ فزیکلی ڈریگ کریں!"), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Fast Location Chips LazyRow
                Text(
                    text = t(isUrdu, "Quick-Jump Locations", "مشہور مقامات پر جائیں"),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickAreas.forEach { item ->
                        SuggestionChip(
                            onClick = {
                                coroutineScope.launch {
                                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(item.second, 15f))
                                }
                            },
                            label = { Text(text = item.first, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Map View Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize().testTag("google_map_element"),
                        cameraPositionState = cameraPositionState
                    )

                    // Floating marker exactly in the viewport target center (Ux standard)
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (-18).dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Centered Target Address Pin",
                                tint = Color.Red,
                                modifier = Modifier.size(36.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp, 4.dp)
                                    .background(Color.Black.copy(alpha = 0.25f), CircleShape)
                            )
                        }
                    }

                    // Floating Zoom Controls Overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                coroutineScope.launch {
                                    cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                                }
                            },
                            containerColor = Color.White,
                            contentColor = DeepBlue40,
                            modifier = Modifier.size(38.dp),
                            shape = CircleShape
                        ) {
                            Text("+", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DeepBlue40)
                        }
                        FloatingActionButton(
                            onClick = {
                                coroutineScope.launch {
                                    cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                                }
                            },
                            containerColor = Color.White,
                            contentColor = DeepBlue40,
                            modifier = Modifier.size(38.dp),
                            shape = CircleShape
                        ) {
                            Text("-", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DeepBlue40)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer Address Lock Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PinDrop,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = t(isUrdu, "Identified Worksite Area", "کام کروانے کا منتخب کردہ مقام"),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = DeepBlue40
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDark,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "GPS: ${String.format("%.6f", currentTarget.latitude)}, ${String.format("%.6f", currentTarget.longitude)} (Zoom: ${String.format("%.1f", cameraPositionState.position.zoom)})",
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 10.sp,
                            color = TextGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Primary Set Location Button
                Button(
                    onClick = {
                        onLocationSelected(currentTarget.latitude, currentTarget.longitude, currentAddress)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("map_confirm_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald40),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = t(isUrdu, "Lock Workspace Location", "مکان کا نقشہ لاک کریں"),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Reverse Geocoding support in Pakistan
fun reverseGeocodeInPakistan(context: Context, latitude: Double, longitude: Double, isUrdu: Boolean): String {
    try {
        if (Geocoder.isPresent()) {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addressLine = addresses[0].getAddressLine(0)
                if (!addressLine.isNullOrEmpty()) {
                    return addressLine
                }
            }
        }
    } catch (e: Exception) {
        // Fallback silently to smart local simulation below
    }
    return getSimulatedPakistanAddress(latitude, longitude, isUrdu)
}

// Bounding box lookup for major Pakistani localities (realistic results offline)
fun getSimulatedPakistanAddress(latitude: Double, longitude: Double, isUrdu: Boolean): String {
    if (latitude in 31.42..31.59 && longitude in 74.24..74.46) {
        return if (isUrdu) {
            "گلبرگ III، لاہور، پنجاب، پاکستان"
        } else {
            "Gulberg III, Lahore, Punjab, Pakistan"
        }
    } else if (latitude in 24.78..24.96 && longitude in 67.0..67.15) {
        return if (isUrdu) {
            "کلفٹن بلاک 5، کراچی، سندھ، پاکستان"
        } else {
            "Clifton Block 5, Karachi, Sindh, Pakistan"
        }
    } else if (latitude in 33.61..33.74 && longitude in 72.95..73.09) {
        return if (isUrdu) {
            "سیکٹر ایف الیون، اسلام آباد، پاکستان"
        } else {
            "Sector F-11, Islamabad, Capital Territory, Pakistan"
        }
    } else if (latitude in 33.54..33.61 && longitude in 73.02..73.08) {
        return if (isUrdu) {
            "صدر روڈ، راولپنڈی، پنجاب، پاکستان"
        } else {
            "Main Saddar Road, Rawalpindi, Punjab, Pakistan"
        }
    } else if (latitude in 31.39..31.47 && longitude in 73.06..73.16) {
        return if (isUrdu) {
            "مدینہ ٹاؤن، فیصل آباد، پنجاب، پاکستان"
        } else {
            "Madina Town, Faisalabad, Punjab, Pakistan"
        }
    } else if (latitude in 30.12..30.25 && longitude in 71.42..71.55) {
        return if (isUrdu) {
            "گلگشت کالونی، ملتان، پنجاب، پاکستان"
        } else {
            "Gulgasht Colony, Multan, Punjab, Pakistan"
        }
    } else if (latitude in 33.95..34.05 && longitude in 71.42..71.58) {
        return if (isUrdu) {
            "حیات آباد، پشاور، خیبر پختونخوا، پاکستان"
        } else {
            "Hayatabad, Peshawar, Khyber Pakhtunkhwa, Pakistan"
        }
    } else {
        return if (isUrdu) {
            "مکان نمبر ${(latitude * 100).toInt() % 100 + 1}، گلی ${(longitude * 100).toInt() % 12 + 1}، فیز ${(latitude * 10).toInt() % 3 + 1}، پاکستان"
        } else {
            "House ${(latitude * 100).toInt() % 100 + 1}, Street ${(longitude * 100).toInt() % 12 + 1}, Phase ${(latitude * 10).toInt() % 3 + 1}, Pakistan"
        }
    }
}

// Forward Geocoding support in Pakistan
fun geocodeInPakistan(context: Context, query: String): LatLng? {
    try {
        if (Geocoder.isPresent()) {
            val geocoder = Geocoder(context)
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(query + ", Pakistan", 1)
            if (!addresses.isNullOrEmpty()) {
                return LatLng(addresses[0].latitude, addresses[0].longitude)
            }
        }
    } catch (e: Exception) {
        // Fallback to local parsing below
    }

    val q = query.lowercase().trim()
    return when {
        q.contains("lahore") || q.contains("gulberg") || q.contains("liberty") || q.contains("model town") || q.contains("johar town") -> LatLng(31.5204, 74.3587)
        q.contains("karachi") || q.contains("clifton") || q.contains("zamzama") || q.contains("defense") -> LatLng(24.8607, 67.0011)
        q.contains("islamabad") || q.contains("f-11") || q.contains("g-11") || q.contains("blue area") || q.contains("centaurus") -> LatLng(33.6844, 73.0479)
        q.contains("rawalpindi") || q.contains("saddar") || q.contains("chaklala") -> LatLng(33.5984, 73.0441)
        q.contains("faisalabad") || q.contains("madina town") || q.contains("sargodha") -> LatLng(31.4504, 73.1350)
        q.contains("multan") || q.contains("gulgasht") || q.contains("cantt") -> LatLng(30.1575, 71.5249)
        q.contains("peshawar") || q.contains("hayatabad") -> LatLng(33.9971, 71.4784)
        else -> null
    }
}
