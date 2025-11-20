# Dashboard Dynamic Data Implementation Plan

**Goal:** Transform the Dashboard screen from static hardcoded data to a fully functional, data-driven interface that displays real resume analysis history and statistics from Firestore.

---

## 1. Current State Summary

### What Dashboard Currently Shows

**Static/Hardcoded Elements:**
- **Total Resume Edits:** Hardcoded value "432" (line 123)
- **Graph Bars:** Hardcoded list of 7 values: `[40, 80, 60, 100, 90, 70, 50]` (line 138)
- **Resume Corrections Card:** Hardcoded value "30" (line 158)
- **AI Checker Card:** Hardcoded percentage "80%" (line 164)
- **Resume History:** 3 hardcoded items with fake data (lines 177-187)
  - Resume_Version_3: 5 corrections, 6 suggestions
  - Resume_Version_2: 2 corrections, 7 suggestions
  - Resume_Version_1: 7 corrections, 10 suggestions

**Dynamic Elements:**
- User profile data (name, email, username) loaded via `UserViewModel` from Firestore
- Profile sidebar displays real user info
- Navigation structure is functional

### Existing Data Infrastructure

**UserViewModel (`data/UserViewModel.kt`):**
- ✅ Loads user profile from Firestore (`users/{userId}`)
- ✅ Exposes `UserUiState` with `UserProfile` (name, email, username)
- ✅ Already integrated into DashboardScreen (line 39-44)

**FirestoreRepository (`data/FirestoreRepository.kt`):**
- ✅ `saveUserProfile(userId, data)` - saves user profile
- ✅ `getUserProfile(userId)` - retrieves user profile
- ✅ `saveResumeAnalysis(userId, analysisId, data)` - saves analysis results
- ✅ `getResumeAnalyses(userId)` - fetches ALL analyses for a user
- ✅ `observeResumeAnalyses(userId, onUpdate, onError)` - real-time listener

**ResumeAnalysisRepository (`analysis/ResumeAnalysisRepository.kt`):**
- ✅ `analyzeImageBytes(imageBytes)` - OCR + LLM analysis + Firestore save
- ✅ `analyzePdfBytes(pdfBytes)` - PDF extraction + LLM analysis + Firestore save
- ✅ Stores analysis data with structure:
  ```kotlin
  {
    "analysisId": String (timestamp),
    "source": "pdf" | null (camera),
    "score": Int,
    "summary": String,
    "resumeText": String,
    "suggestions": List<Map<String, String>>,
    "suggestionCount": Int,
    "createdAt": Timestamp
  }
  ```

**Resume Analysis Flow:**
1. User clicks "+" button → `PolishResumeScreen`
2. User picks camera or PDF
3. Camera: `CameraScreen` → captures photo → `ResumeAnalysisScreen`
4. PDF: File picker → `ResumeAnalysisScreen`
5. `ResumeAnalysisViewModel` calls repository methods
6. Repository:
   - Calls backend API (`/extract` or `/extract_pdf` then `/analyze`)
   - Saves result to Firestore under `users/{userId}/resume_analyses/{analysisId}`
   - Returns result to UI

### What We Can Build Dashboard From

**Available from Firestore** (`users/{userId}/resume_analyses`):
- ✅ Full list of analyses per user
- ✅ Each analysis contains:
  - `analysisId` (unique ID)
  - `score` (0-100 integer)
  - `summary` (text description)
  - `suggestions` (list of objects with category/issue/recommendation)
  - `suggestionCount` (integer)
  - `createdAt` (Firestore Timestamp)
  - `source` (optional: "pdf" or null for camera)
  - `resumeText` (the extracted text)

**Computable Metrics:**
- **Total Resume Count:** `analyses.size`
- **Total Suggestions:** `analyses.sumOf { it.suggestionCount }`
- **Average Score:** `analyses.map { it.score }.average()`
- **Resume History List:** Sorted by `createdAt` descending
- **Graph Data:** Various options (see Target Behavior below)

---

## 2. Target Dashboard Behavior

### Header (Already Functional ✅)
- **Title:** "Dashboard"
- **Left Icon:** Hamburger menu → opens navigation drawer
- **Right Icon:** Plus (+) button → navigates to `PolishResumeScreen`

### Total Resume Edits Section
**Metric:** Total number of resume analyses submitted by this user

**Calculation:**
```kotlin
totalEdits = analyses.size
```

**Display:**
- Label: "Total Resume Edits"
- Value: Large number (e.g., "5" if user has 5 analyses)
- If zero: Show "0"

---

### Graph Section
**Purpose:** Visual representation of how much the user has used the app

**Design Option (Recommended):**
- Bar chart showing the last 7-10 analyses
- Each bar height = score of that analysis
- Bars ordered chronologically (oldest to newest, left to right)
- If user has < 7 analyses, show only those bars
- If user has 0 analyses, show empty/placeholder state or all bars at 0

**Calculation:**
```kotlin
// Take last 7 analyses, sorted by createdAt
val recentAnalyses = analyses
    .sortedBy { it.createdAt }
    .takeLast(7)
    
val graphBars = recentAnalyses.map { it.score }

// If less than 7, pad with zeros (optional)
val paddedBars = graphBars + List(7 - graphBars.size) { 0 }
```

**Alternative Options:**
- Option B: Bars = number of suggestions per analysis (last 7)
- Option C: Bars = grouped analyses by week/month (more complex)

---

### Statistics Cards

#### Card 1: Resume Corrections
**Metric:** Total number of suggestions/corrections across all analyses

**Calculation:**
```kotlin
totalCorrections = analyses.sumOf { it.suggestionCount }
```

**Display:**
- Title: "Resume Corrections"
- Value: Integer (e.g., "42")
- Icon: `resume_icon`

#### Card 2: AI Checker
**Metric:** Average score across all analyses (as percentage)

**Calculation:**
```kotlin
aiCheckerPercent = if (analyses.isNotEmpty()) {
    analyses.map { it.score }.average().roundToInt()
} else {
    0
}
```

**Display:**
- Title: "AI Checker"
- Value: Percentage (e.g., "85%")
- Icon: `resume_icon`

---

### Resume History Section

**Data Source:** All analyses for the current user, sorted by `createdAt` descending (newest first)

**Each Item Displays:**
- **Icon:** Resume icon (already exists)
- **Title:** `Resume_Version_{N}` where N is the reverse chronological index
  - Example: If user has 5 analyses, newest = "Resume_Version_5", oldest = "Resume_Version_1"
- **Subtitle:** `"{suggestionCount} Corrections, {suggestionCount} Suggestions"`
  - Note: Current data structure uses `suggestionCount` for both. If backend differentiates corrections vs suggestions in the future, update this.

**Calculation:**
```kotlin
val sortedAnalyses = analyses.sortedByDescending { it.createdAt }
val historyItems = sortedAnalyses.mapIndexed { index, analysis ->
    HistoryItem(
        analysisId = analysis.analysisId,
        versionNumber = sortedAnalyses.size - index, // reverse chronological numbering
        correctionsCount = analysis.suggestionCount,
        suggestionsCount = analysis.suggestionCount,
        score = analysis.score
    )
}
```

**Tap Behavior:**
When user taps a history item:
1. Navigate to `ResumeAnalysisScreen`
2. Pass `analysisId` as route parameter
3. `ResumeAnalysisScreen` should load that specific analysis from Firestore
4. Display the stored score, summary, and suggestions

**Current Gap:**
- `ResumeAnalysisScreen` currently only loads new analyses from camera/PDF URIs
- Need to add capability to load existing analysis from Firestore by `analysisId`

---

### Empty State Handling

**If user has 0 analyses:**
- Total Resume Edits: "0"
- Graph: Show 7 bars all at height 0, or placeholder message
- Resume Corrections: "0"
- AI Checker: "0%" or "N/A"
- Resume History: Show empty state message:
  - "No resumes analyzed yet"
  - "Tap the + button to get started!"

---

## 3. Data Plan

### Firestore Data Schema (Current)

**Collection Path:** `users/{userId}/resume_analyses/{analysisId}`

**Document Fields:**
| Field Name | Type | Description | Source |
|------------|------|-------------|--------|
| `analysisId` | String | Unique ID (timestamp) | Repository |
| `source` | String? | "pdf" or null (camera) | Repository |
| `score` | Int | 0-100 score | Backend LLM |
| `summary` | String | Analysis summary text | Backend LLM |
| `resumeText` | String | Extracted text from resume | Backend OCR |
| `suggestions` | List<Map> | Array of {category, issue, recommendation} | Backend LLM |
| `suggestionCount` | Int | Size of suggestions array | Repository |
| `createdAt` | Timestamp | When analysis was created | Repository |

### Dashboard UI State Mapping

**Target Data Model:**
```kotlin
data class DashboardUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalEdits: Int = 0,
    val totalCorrections: Int = 0,
    val aiCheckerPercent: Int = 0,
    val graphBars: List<Int> = emptyList(),
    val historyItems: List<HistoryItem> = emptyList()
)

data class HistoryItem(
    val analysisId: String,
    val versionLabel: String,      // e.g., "Resume_Version_3"
    val correctionsCount: Int,
    val suggestionsCount: Int,
    val score: Int
)
```

**Computation Logic:**
```kotlin
// From raw Firestore data
val rawAnalyses: List<Map<String, Any>> = /* from FirestoreRepository */

// Parse into structured objects
val analyses = rawAnalyses.mapNotNull { data ->
    try {
        AnalysisData(
            analysisId = data["analysisId"] as String,
            score = (data["score"] as Long).toInt(),
            suggestionCount = (data["suggestionCount"] as Long?)?.toInt() ?: 0,
            createdAt = data["createdAt"] as Timestamp
        )
    } catch (e: Exception) {
        null // Skip malformed entries
    }
}

// Compute metrics
val totalEdits = analyses.size

val totalCorrections = analyses.sumOf { it.suggestionCount }

val aiCheckerPercent = if (analyses.isNotEmpty()) {
    analyses.map { it.score }.average().roundToInt()
} else {
    0
}

// Graph: last 7 analyses by score
val graphBars = analyses
    .sortedBy { it.createdAt }
    .takeLast(7)
    .map { it.score }

// History: newest first with version numbers
val sortedAnalyses = analyses.sortedByDescending { it.createdAt }
val historyItems = sortedAnalyses.mapIndexed { index, analysis ->
    HistoryItem(
        analysisId = analysis.analysisId,
        versionLabel = "Resume_Version_${sortedAnalyses.size - index}",
        correctionsCount = analysis.suggestionCount,
        suggestionsCount = analysis.suggestionCount,
        score = analysis.score
    )
}
```

---

## 4. ViewModel & State Plan

### Approach: Create Dedicated `DashboardViewModel`

**Rationale:**
- `UserViewModel` is focused on user profile data (name, email, username)
- Dashboard has distinct data needs (analyses, metrics, history)
- Separation of concerns: profile vs. dashboard statistics
- `DashboardViewModel` will fetch analyses and compute all necessary metrics

### New File: `app/src/main/java/com/cs407/resumelens/data/DashboardViewModel.kt`

**Structure:**
```kotlin
package com.cs407.resumelens.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.resumelens.auth.AuthRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HistoryItem(
    val analysisId: String,
    val versionLabel: String,
    val correctionsCount: Int,
    val suggestionsCount: Int,
    val score: Int
)

data class DashboardUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalEdits: Int = 0,
    val totalCorrections: Int = 0,
    val aiCheckerPercent: Int = 0,
    val graphBars: List<Int> = emptyList(),
    val historyItems: List<HistoryItem> = emptyList()
)

data class AnalysisData(
    val analysisId: String,
    val score: Int,
    val suggestionCount: Int,
    val createdAt: Timestamp
)

class DashboardViewModel(
    private val firestoreRepo: FirestoreRepository = FirestoreRepository(),
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        val userId = authRepo.currentUser?.uid ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            firestoreRepo.getResumeAnalyses(userId)
                .onSuccess { rawAnalyses ->
                    val analyses = parseAnalyses(rawAnalyses)
                    val uiState = computeDashboardState(analyses)
                    _state.value = uiState.copy(isLoading = false)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load dashboard data"
                    )
                }
        }
    }

    private fun parseAnalyses(rawData: List<Map<String, Any>>): List<AnalysisData> {
        return rawData.mapNotNull { data ->
            try {
                AnalysisData(
                    analysisId = data["analysisId"] as String,
                    score = (data["score"] as Long).toInt(),
                    suggestionCount = (data["suggestionCount"] as Long?)?.toInt() ?: 0,
                    createdAt = data["createdAt"] as Timestamp
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun computeDashboardState(analyses: List<AnalysisData>): DashboardUiState {
        val totalEdits = analyses.size

        val totalCorrections = analyses.sumOf { it.suggestionCount }

        val aiCheckerPercent = if (analyses.isNotEmpty()) {
            analyses.map { it.score }.average().toInt()
        } else {
            0
        }

        val graphBars = analyses
            .sortedBy { it.createdAt }
            .takeLast(7)
            .map { it.score }

        val sortedAnalyses = analyses.sortedByDescending { it.createdAt }
        val historyItems = sortedAnalyses.mapIndexed { index, analysis ->
            HistoryItem(
                analysisId = analysis.analysisId,
                versionLabel = "Resume_Version_${sortedAnalyses.size - index}",
                correctionsCount = analysis.suggestionCount,
                suggestionsCount = analysis.suggestionCount,
                score = analysis.score
            )
        }

        return DashboardUiState(
            totalEdits = totalEdits,
            totalCorrections = totalCorrections,
            aiCheckerPercent = aiCheckerPercent,
            graphBars = graphBars,
            historyItems = historyItems
        )
    }

    fun refresh() {
        loadDashboardData()
    }
}
```

### Integration with DashboardScreen

**Modifications to `DashboardScreen.kt`:**
1. Add `DashboardViewModel` parameter alongside `UserViewModel`
2. Collect `DashboardUiState` from the ViewModel
3. Replace hardcoded values with state values
4. Handle loading and error states
5. Wire up history item click to navigation with `analysisId`

---

## 5. Phased Implementation Plan

### Phase 1: Create DashboardViewModel & Data Loading

**Goal:** Set up the ViewModel and data fetching logic

**Files to Create:**
- ✅ `/app/src/main/java/com/cs407/resumelens/data/DashboardViewModel.kt`
  - Create `DashboardViewModel` class
  - Define `DashboardUiState` and `HistoryItem` data classes
  - Define `AnalysisData` helper class
  - Implement `loadDashboardData()` method
  - Implement `parseAnalyses()` helper
  - Implement `computeDashboardState()` helper

**Dependencies:**
- Requires existing `FirestoreRepository` (already exists ✅)
- Requires existing `AuthRepository` (already exists ✅)

**Testing:**
- Manually test by logging the fetched analyses in ViewModel
- Verify data parsing handles Firestore field types correctly

---

### Phase 2: Wire DashboardViewModel into DashboardScreen

**Goal:** Connect ViewModel to UI without changing layout yet

**Files to Modify:**
1. **`/app/src/main/java/com/cs407/resumelens/ui/screens/DashboardScreen.kt`**
   - Add `DashboardViewModel` parameter (with default `viewModel()`)
   - Add state collection: `val dashboardState by dashboardViewModel.state.collectAsStateWithLifecycle()`
   - Keep existing UI unchanged for now

2. **`/app/src/main/java/com/cs407/resumelens/ui/screens/ResumeLensApp.kt`**
   - In `Dashboard` composable block (line 91-110):
     - Create `val dashboardVm: DashboardViewModel = viewModel()`
     - Add `LaunchedEffect(Unit) { dashboardVm.loadDashboardData() }`
     - Pass `dashboardViewModel = dashboardVm` to `DashboardScreen`

**Testing:**
- Verify app compiles
- Verify ViewModel loads data (check logs)
- UI still shows hardcoded values (expected at this stage)

---

### Phase 3: Replace Hardcoded Dashboard Metrics with Real Data

**Goal:** Bind Total Edits, Corrections, AI Checker, and Graph to ViewModel state

**Files to Modify:**
1. **`/app/src/main/java/com/cs407/resumelens/ui/screens/DashboardScreen.kt`**
   
   **Change 1: Total Resume Edits (line 122-123)**
   ```kotlin
   // OLD:
   Text("Total Resume Edits", fontSize = 16.sp, color = Color.Gray)
   Text("432", fontSize = 40.sp, fontWeight = FontWeight.Bold)
   
   // NEW:
   Text("Total Resume Edits", fontSize = 16.sp, color = Color.Gray)
   Text(
       text = "${dashboardState.totalEdits}",
       fontSize = 40.sp,
       fontWeight = FontWeight.Bold
   )
   ```
   
   **Change 2: Graph Bars (line 138)**
   ```kotlin
   // OLD:
   val heights = listOf(40, 80, 60, 100, 90, 70, 50)
   
   // NEW:
   val heights = if (dashboardState.graphBars.isNotEmpty()) {
       dashboardState.graphBars
   } else {
       listOf(0, 0, 0, 0, 0, 0, 0) // Empty state
   }
   ```
   
   **Change 3: Resume Corrections Card (line 156-161)**
   ```kotlin
   // OLD:
   StatCard(
       title = "Resume Corrections",
       value = "30",
       icon = R.drawable.resume_icon,
       modifier = Modifier.weight(1f)
   )
   
   // NEW:
   StatCard(
       title = "Resume Corrections",
       value = "${dashboardState.totalCorrections}",
       icon = R.drawable.resume_icon,
       modifier = Modifier.weight(1f)
   )
   ```
   
   **Change 4: AI Checker Card (line 162-167)**
   ```kotlin
   // OLD:
   StatCard(
       title = "AI Checker",
       value = "80%",
       icon = R.drawable.resume_icon,
       modifier = Modifier.weight(1f)
   )
   
   // NEW:
   StatCard(
       title = "AI Checker",
       value = "${dashboardState.aiCheckerPercent}%",
       icon = R.drawable.resume_icon,
       modifier = Modifier.weight(1f)
   )
   ```

**Testing:**
- Run app with existing user who has no analyses → should show all zeros
- Upload a resume via camera/PDF → return to Dashboard → verify metrics update
- Create multiple analyses → verify numbers accumulate correctly

---

### Phase 4: Implement Dynamic Resume History List

**Goal:** Replace hardcoded history items with real data from ViewModel

**Files to Modify:**
1. **`/app/src/main/java/com/cs407/resumelens/ui/screens/DashboardScreen.kt`**
   
   **Change: Resume History LazyColumn (lines 176-188)**
   ```kotlin
   // OLD:
   LazyColumn {
       items(3) { index ->
           ResumeHistoryItem(
               title = "Resume_Version_${3 - index}",
               corrections = listOf(5, 2, 7)[index],
               suggestions = listOf(6, 7, 10)[index],
               onClick = {
                   onNavigateToResumeAnalysis("resume_${index + 1}")
               }
           )
       }
   }
   
   // NEW:
   LazyColumn {
       if (dashboardState.historyItems.isEmpty()) {
           item {
               Column(
                   modifier = Modifier
                       .fillMaxWidth()
                       .padding(vertical = 32.dp),
                   horizontalAlignment = Alignment.CenterHorizontally
               ) {
                   Text(
                       text = "No resumes analyzed yet",
                       color = Color.Gray,
                       fontSize = 16.sp
                   )
                   Spacer(Modifier.height(8.dp))
                   Text(
                       text = "Tap the + button to get started!",
                       color = Color.Gray,
                       fontSize = 14.sp
                   )
               }
           }
       } else {
           items(dashboardState.historyItems) { historyItem ->
               ResumeHistoryItem(
                   title = historyItem.versionLabel,
                   corrections = historyItem.correctionsCount,
                   suggestions = historyItem.suggestionsCount,
                   onClick = {
                       onNavigateToResumeAnalysis(historyItem.analysisId)
                   }
               )
           }
       }
   }
   ```

**Testing:**
- Empty state: New user should see "No resumes analyzed yet" message
- Populated state: User with analyses should see correct version numbers
- Click handling: Tap should call `onNavigateToResumeAnalysis` with real `analysisId`

---

### Phase 5: Enable ResumeAnalysisScreen to Load Existing Analyses

**Goal:** Allow navigation to a specific past analysis from Dashboard history

**Current Problem:**
- `ResumeAnalysisScreen` only handles new analyses from camera/PDF URIs
- Navigation from Dashboard passes `analysisId`, but screen doesn't load it

**Solution:**
Add capability to load existing analysis from Firestore by ID

**Files to Modify:**

1. **`/app/src/main/java/com/cs407/resumelens/analysis/ResumeAnalysisRepository.kt`**
   - Add new method:
   ```kotlin
   suspend fun getAnalysisById(userId: String, analysisId: String): Result<AnalysisResponseDto> =
       withContext(Dispatchers.IO) {
           try {
               val data = firestoreRepo.getResumeAnalysisById(userId, analysisId)
                   .getOrThrow()
               
               // Parse Firestore data back into AnalysisResponseDto
               val suggestions = (data["suggestions"] as List<Map<String, Any>>).map { s ->
                   SuggestionDto(
                       category = s["category"] as String,
                       issue = s["issue"] as String,
                       recommendation = s["recommendation"] as String
                   )
               }
               
               val response = AnalysisResponseDto(
                   score = (data["score"] as Long).toInt(),
                   summary = data["summary"] as String,
                   suggestions = suggestions
               )
               
               Result.success(response)
           } catch (e: Exception) {
               Result.failure(e)
           }
       }
   ```

2. **`/app/src/main/java/com/cs407/resumelens/data/FirestoreRepository.kt`**
   - Add new method:
   ```kotlin
   suspend fun getResumeAnalysisById(
       userId: String,
       analysisId: String
   ): Result<Map<String, Any>> {
       return try {
           val document = db.collection("users")
               .document(userId)
               .collection("resume_analyses")
               .document(analysisId)
               .get()
               .await()
           
           if (document.exists()) {
               Result.success(document.data!!)
           } else {
               Result.failure(Exception("Analysis not found"))
           }
       } catch (e: FirebaseFirestoreException) {
           Result.failure(e)
       }
   }
   ```

3. **`/app/src/main/java/com/cs407/resumelens/analysis/ResumeAnalysisViewModel.kt`**
   - Add new method:
   ```kotlin
   fun loadAnalysisById(analysisId: String) {
       viewModelScope.launch {
           _state.value = _state.value.copy(loading = true, error = null)
           
           repository.getAnalysisById(analysisId)
               .onSuccess { resp ->
                   _state.value = _state.value.copy(
                       loading = false,
                       score = resp.score,
                       summary = resp.summary,
                       suggestions = resp.suggestions
                   )
               }
               .onFailure { e ->
                   _state.value = _state.value.copy(
                       loading = false,
                       error = e.message ?: "Failed to load analysis"
                   )
               }
       }
   }
   ```

4. **`/app/src/main/java/com/cs407/resumelens/ui/screens/ResumeLensApp.kt`**
   - Update navigation setup to pass `analysisId`:
   ```kotlin
   // Change route definition for ResumeAnalysis (line 25)
   data object ResumeAnalysis : Screen("resume_analysis/{analysisId}")
   
   // Update composable (line 141-152)
   composable(
       route = "resume_analysis/{analysisId}?analysisId={analysisId}",
       arguments = listOf(
           navArgument("analysisId") { 
               type = NavType.StringType
               nullable = true
               defaultValue = null
           }
       )
   ) { backStackEntry ->
       val analysisId = backStackEntry.arguments?.getString("analysisId")
       
       ResumeAnalysisScreen(
           viewModel = analysisVm,
           analysisId = analysisId,
           onBack = { nav.popBackStack() },
           onImproveScore = {
               nav.navigate(Screen.PolishResume.route) {
                   popUpTo(Screen.Dashboard.route) { inclusive = false }
               }
           }
       )
   }
   ```

5. **`/app/src/main/java/com/cs407/resumelens/ui/screens/ResumeAnalysisScreen.kt`**
   - Add `analysisId` parameter:
   ```kotlin
   @Composable
   fun ResumeAnalysisScreen(
       viewModel: ResumeAnalysisViewModel,
       analysisId: String? = null, // NEW
       onBack: () -> Unit = {},
       onImproveScore: () -> Unit = {}
   ) {
       val uiState by viewModel.state.collectAsStateWithLifecycle()
       val context = LocalContext.current

       LaunchedEffect(Unit) {
           // NEW: Check if loading existing analysis
           if (analysisId != null) {
               viewModel.loadAnalysisById(analysisId)
               return@LaunchedEffect
           }
           
           // EXISTING: Handle new analysis from camera/PDF
           val imgUri = viewModel.consumePendingImageUri()
           if (imgUri != null) {
               val bytes = context.contentResolver.openInputStream(imgUri)?.readBytes()
               if (bytes != null) viewModel.analyzeImageBytes(bytes)
               return@LaunchedEffect
           }

           val pdfUri = viewModel.consumePendingPdfUri()
           if (pdfUri != null) {
               val bytes = context.contentResolver.openInputStream(pdfUri)?.readBytes()
               if (bytes != null) viewModel.analyzePdfBytes(bytes)
           }
       }
       
       // Rest of UI remains the same
   }
   ```

**Testing:**
- Navigate to Dashboard → tap a history item → should load that specific analysis
- Verify correct score, summary, and suggestions display
- Back button should return to Dashboard
- New analyses from camera/PDF should still work as before

---

### Phase 6: Add Loading & Error States

**Goal:** Improve UX with loading indicators and error handling

**Files to Modify:**
1. **`/app/src/main/java/com/cs407/resumelens/ui/screens/DashboardScreen.kt`**
   
   **Add loading overlay:**
   ```kotlin
   // Inside Column, after padding:
   if (dashboardState.isLoading) {
       Box(
           modifier = Modifier.fillMaxSize(),
           contentAlignment = Alignment.Center
       ) {
           CircularProgressIndicator()
       }
   } else {
       // Existing dashboard content
   }
   ```
   
   **Add error snackbar:**
   ```kotlin
   // Add SnackbarHost if error exists
   val snackbarHostState = remember { SnackbarHostState() }
   
   LaunchedEffect(dashboardState.errorMessage) {
       dashboardState.errorMessage?.let { error ->
           snackbarHostState.showSnackbar(
               message = error,
               duration = SnackbarDuration.Short
           )
       }
   }
   ```

**Testing:**
- Simulate slow network → should show loading spinner
- Force error by breaking Firestore connection → should show error message

---

### Phase 7: Add Pull-to-Refresh (Optional Enhancement)

**Goal:** Allow users to manually refresh dashboard data

**Files to Modify:**
1. **`/app/src/main/java/com/cs407/resumelens/ui/screens/DashboardScreen.kt`**
   - Wrap main content in `PullRefreshIndicator`
   - Add refresh state and callback:
   ```kotlin
   val refreshing by remember { derivedStateOf { dashboardState.isLoading } }
   val pullRefreshState = rememberPullRefreshState(
       refreshing = refreshing,
       onRefresh = { dashboardViewModel.refresh() }
   )
   
   Box(Modifier.pullRefresh(pullRefreshState)) {
       // Existing Column content
       
       PullRefreshIndicator(
           refreshing = refreshing,
           state = pullRefreshState,
           modifier = Modifier.align(Alignment.TopCenter)
       )
   }
   ```

**Testing:**
- Pull down on Dashboard → should trigger data refresh
- New analyses should appear after pull-to-refresh

---

### Phase 8: Add Real-time Updates (Optional Enhancement)

**Goal:** Dashboard automatically updates when new analyses are added

**Files to Modify:**
1. **`/app/src/main/java/com/cs407/resumelens/data/DashboardViewModel.kt`**
   - Replace one-time fetch with real-time listener:
   ```kotlin
   init {
       setupRealtimeListener()
   }
   
   private fun setupRealtimeListener() {
       val userId = authRepo.currentUser?.uid ?: return
       
       firestoreRepo.observeResumeAnalyses(
           userId = userId,
           onUpdate = { rawAnalyses ->
               val analyses = parseAnalyses(rawAnalyses)
               val uiState = computeDashboardState(analyses)
               _state.value = uiState
           },
           onError = { e ->
               _state.value = _state.value.copy(
                   errorMessage = e.message ?: "Failed to load data"
               )
           }
       )
   }
   ```

**Testing:**
- Upload new resume → navigate to Dashboard → should auto-update without manual refresh
- Verify no memory leaks (listener cleanup in `onCleared()`)

---

## 6. Testing Checklist

### Unit Tests (Future Consideration)
- [ ] `DashboardViewModel.parseAnalyses()` handles malformed data
- [ ] `computeDashboardState()` correctly calculates metrics
- [ ] Empty analyses list returns zeros for all metrics

### Integration Tests
- [ ] New user with 0 analyses sees empty state
- [ ] User with 1 analysis sees correct data
- [ ] User with 10+ analyses sees correct graph (last 7)
- [ ] History items sorted newest first
- [ ] Version numbers assigned correctly
- [ ] Clicking history item navigates to correct analysis

### End-to-End Flow
1. [ ] Fresh user signs up → Dashboard shows zeros
2. [ ] User uploads camera photo → analysis runs → Dashboard updates
3. [ ] User uploads PDF → analysis runs → Dashboard updates
4. [ ] User taps history item → sees correct analysis
5. [ ] User taps back → returns to Dashboard
6. [ ] User signs out and back in → Dashboard persists data

---

## 7. Edge Cases & Considerations

### Data Consistency
- **Problem:** User navigates away during analysis upload
- **Solution:** Repository already saves to Firestore asynchronously; Dashboard will load next time

### Large Data Sets
- **Problem:** User has 100+ analyses
- **Solution:** 
  - Graph only shows last 7 (already limited)
  - History list uses LazyColumn (already implemented)
  - Consider pagination if needed (future enhancement)

### Firestore Field Type Mismatches
- **Problem:** Firestore returns `Long` for numbers, not `Int`
- **Solution:** Explicitly cast in `parseAnalyses()`: `(data["score"] as Long).toInt()`

### Null/Missing Fields
- **Problem:** Old analyses might not have `suggestionCount` field
- **Solution:** Use safe defaults: `?.toInt() ?: 0`

### Score Range
- **Problem:** Backend might return scores outside 0-100
- **Solution:** Add validation in parsing: `score.coerceIn(0, 100)`

### Graph Empty State
- **Problem:** User with 0 analyses has empty graph
- **Solution:** Show placeholder bars at height 0, or display message "Complete your first analysis to see activity"

---

## 8. Dependencies & Prerequisites

### Already Satisfied ✅
- Firebase Firestore SDK integrated
- `FirestoreRepository` implemented
- `ResumeAnalysisRepository` saves analyses
- `UserViewModel` pattern established
- Navigation structure exists

### New Dependencies
- None required (all functionality uses existing libraries)

### Build Configuration
- No changes to `build.gradle.kts` needed

---

## 9. Rollout Strategy

### Development Approach
1. **Phase 1-2:** Set up data layer (no UI changes)
2. **Phase 3-4:** Replace hardcoded values (user-facing changes)
3. **Phase 5:** Enable historical analysis loading (new feature)
4. **Phase 6-8:** Polish and enhancements (iterative)

### Testing Strategy
- Test each phase independently before moving to next
- Use a test Firebase account with controlled data
- Manually create 0, 1, 5, and 10+ analyses to test all states

### Rollback Plan
- If issues arise, phases 1-2 don't affect UI (can rollback easily)
- Phase 3+ changes are non-destructive (data stays in Firestore)
- Keep hardcoded values in comments during development for quick revert

---

## 10. Future Enhancements (Post-MVP)

### Analytics Tracking
- Track which history items users click most
- Monitor average time spent on Dashboard

### Advanced Metrics
- Compare current resume score to previous versions
- Show improvement trends over time
- Add "Best Score" and "Average Score" cards

### Filtering & Sorting
- Filter history by date range
- Sort by score, corrections, or date
- Search history by resume text content

### Achievements System
- Unlock achievements for milestones (5 resumes, 90+ score, etc.)
- Display progress in Profile Sidebar

### Export History
- Download CSV of all analyses
- Share resume analysis as PDF report

---

## Summary

This plan transforms the Dashboard from static mockup to fully functional data-driven screen in 8 phases:

1. ✅ **Phase 1:** Create `DashboardViewModel` with data fetching
2. ✅ **Phase 2:** Wire ViewModel into UI (no visual changes)
3. ✅ **Phase 3:** Replace hardcoded metrics with real data
4. ✅ **Phase 4:** Implement dynamic history list
5. ✅ **Phase 5:** Enable loading past analyses from history
6. ✅ **Phase 6:** Add loading/error states
7. ✅ **Phase 7:** Add pull-to-refresh (optional)
8. ✅ **Phase 8:** Add real-time updates (optional)

Each phase is independent, testable, and builds on previous work. The implementation leverages existing infrastructure (Firestore, repositories, auth) and requires no new dependencies.

**Estimated Time:**
- Phases 1-5 (core functionality): 4-6 hours
- Phases 6-8 (polish): 2-3 hours
- **Total:** ~6-9 hours of development

**Key Benefits:**
- Dashboard reflects real user activity
- No data is lost or hardcoded
- Users can review past analyses
- Scales from 0 to 1000+ analyses seamlessly

