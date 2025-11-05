# ResumeLens

A mobile-first AI-powered tool that scans, evaluates, and optimizes resumes for ATS systems. Upload a PDF or resume photo and receive instant feedback and actionable improvement recommendations.

Built with **Kotlin + Jetpack Compose** and **Python + FastAPI**, featuring a fullstack architecture using **Google Cloud Vision OCR** and **OpenAI** for intelligent resume analysis.

## Key Features

- **Camera-Based Resume Capture**  
  Snap a photo of any resume directly from your phone. No need for manual uploads or file transfers.

- **Intelligent OCR Extraction**  
  Uses Google Cloud Vision API to accurately extract text from resume images, handling various fonts, layouts, and formatting styles.

- **AI-Powered Resume Analysis**  
  Leverages OpenAI to identify weak phrasing, detect missing keywords, and highlight areas for improvement.

- **ATS Optimization Scoring**  
  Evaluates how well a resume aligns with Applicant Tracking System requirements, providing a quantitative score and specific recommendations.

- **Modern Android UI**  
  Clean, responsive interface built with Jetpack Compose and Material Design 3 principles for an intuitive user experience.

## How It Works

### 1. Capture & Upload

- User opens the app and navigates to the camera screen
- Takes a photo of their resume or selects from gallery
- Image is uploaded to the backend via multipart form-data

### 2. Text Extraction

**Endpoint:**

```http
POST /extract
```

**Request:**

- Multipart file upload containing resume image

**Process:**

- Google Cloud Vision API performs optical character recognition
- Extracts full text while preserving structure and layout context
- Returns clean, machine-readable text

**Response:**

```json
{
  "extracted_text": "John Doe\nSoftware Engineer\n..."
}
```

### 3. AI Analysis & Scoring

**Endpoint:**

```http
POST /analyze
```

**Request:**

```json
{
  "resume_text": "extracted resume content",
  "target_role": "Software Engineer (optional)"
}
```

**Process:**

- OpenAI analyzes resume content for:
  - Weak or passive language
  - Missing action verbs
  - ATS keyword optimization
  - Formatting and structure issues
- Generates improvement suggestions with specific examples
- Calculates overall ATS compatibility score

**Response:**

```json
{
  "score": 78,
  "suggestions": [
    {
      "category": "Action Verbs",
      "issue": "Use of passive language in experience section",
      "recommendation": "Replace 'Responsible for' with 'Led', 'Developed', or 'Managed'"
    }
  ]
}
```

### 4. Display Results

- Android app receives analysis results
- Displays score with visual indicators (color-coded)
- Shows categorized suggestions in an expandable list
- Allows users to save results or re-analyze

## Current Project Architecture

```
.
├── README.md
├── app
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src
│       └── main
│           ├── AndroidManifest.xml
│           ├── java
│           │   └── com
│           │       └── cs407
│           │           ├── ResumeLens.kt
│           │           └── resumelens
│           │               ├── MainActivity.kt
│           │               └── ui
│           │                   ├── components
│           │                   │   └── ProfileMenu.kt
│           │                   ├── screens
│           │                   │   ├── LogInScreen.kt
│           │                   │   ├── ProfileScreen.kt
│           │                   │   ├── ResumeLensApp.kt
│           │                   │   ├── SignUpScreen.kt
│           │                   │   └── WelcomeScreen.kt
│           │                   └── theme
│           │                       ├── Color.kt
│           │                       ├── Theme.kt
│           │                       └── Type.kt
│           └── res
│               ├── drawable
│               ├── mipmap-anydpi
│               ├── mipmap-hdpi
│               ├── mipmap-mdpi
│               ├── mipmap-xhdpi
│               ├── mipmap-xxhdpi
│               ├── mipmap-xxxhdpi
│               ├── values
│               │   ├── colors.xml
│               │   ├── strings.xml
│               │   └── themes.xml
│               └── xml
├── backend
│   ├── credentials
│   ├── main.py
│   ├── ocr_service.py
│   ├── openai_service.py
│   └── requirements.txt
├── build.gradle.kts
├── gradle
│   ├── libs.versions.toml
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
├── local.properties
└── settings.gradle.kts
```

## Local Setup

### Backend Setup

#### 1. Install dependencies

```bash
cd backend
pip install -r requirements.txt
```

#### 2. Configure Environment Variables

Create a `.env` file in the `backend/` directory:

```bash
OPENAI_API_KEY=your_openai_api_key_here
GOOGLE_APPLICATION_CREDENTIALS=./credentials/google_service_account.json # create on Google Cloud Console
```

#### 3. Start Development Server

```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000 # available at localhost:8000
```

### Android Setup

##### TBD

## Team Roles (Rotating Weekly)

| Name                | Week 1 Role            | Email                 |
| ------------------- | ---------------------- | --------------------- |
| Mahesh Ramakrishnan | Project Coordinator    | mramakrishn3@wisc.edu |
| Akash Mohan         | Backend Lead           | amohan29@wisc.edu     |
| Vraj Patel          | Frontend Lead          | vpatel36@wisc.edu     |
| Manan Chand         | Observer/Documentation | mtchand@wisc.edu      |

## Timeline

### Milestone 1 (Week 1)

- [x] Establish team roles and communication plan
- [x] UI screens created
- [x] Screen navigation wired in
- [x] Firebase Auth setup
- [x] Backend structure and environment setup

### Milestone 2 (Weeks 2-3)

- [x] Integrate Google Cloud Vision OCR
- [ ] Implement camera capture and image upload flow on frontend
- [ ] Build OpenAI resume analysis logic
- [ ] Create results display screen

### Milestones 3 & 4 (Weeks 4-5)

- [ ] Error handling and edge cases
- [ ] UI/UX refinements
- [ ] Performance optimization
- [ ] End-to-end testing
