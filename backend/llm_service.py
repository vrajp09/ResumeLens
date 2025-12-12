# Gemini LLM endpoints go here
import google.generativeai as genai
from fastapi import APIRouter, HTTPException, Request
from dotenv import load_dotenv
from pydantic import BaseModel, Field, ValidationError
from typing import List, Optional
import json
import re
import os

load_dotenv()

gemini_router = APIRouter()

class Suggestion(BaseModel):
    category: str
    issue: str
    recommendation: str

class AnalysisRequest(BaseModel):
    resume_text: str = Field(..., min_length=50, max_length=10000)
    target_role: Optional[str] = Field(
        default="Software Engineer",
        description="Target role for tailored suggestions. Defaults to Software Engineer."
    )

class AnalysisResponse(BaseModel):
    score: int = Field(..., ge=0, le=100)
    summary: str
    suggestions: List[Suggestion] = Field(..., min_items=1, max_items=15)

GEMINI_MODEL = os.getenv("GEMINI_MODEL", "gemini-2.5-flash-lite")
SYSTEM_PROMPT = (
    "You are a strict, experienced ATS resume reviewer. Be critical and detailed in your assessment.\n\n"
    
    "SCORING GUIDELINES (be strict):\n"
    "- 90-100: Exceptional resume. Near-perfect ATS optimization, compelling content, pristine formatting. Very rare.\n"
    "- 80-89: Strong resume. Good ATS compatibility, solid experience presentation, minor improvements needed.\n"
    "- 70-79: Decent resume. Acceptable but has notable gaps in ATS optimization or content quality.\n"
    "- 60-69: Needs improvement. Missing key ATS elements, weak descriptions, or formatting issues.\n"
    "- 50-59: Poor resume. Major ATS failures, vague content, unprofessional presentation.\n"
    "- 40-49: Severely flawed. Missing major sections, minimal content, or very poor quality.\n"
    "- Below 40: Broken/Incomplete. Text cuts off mid-sentence, severely corrupted, or only fragments present.\n\n"
    
    "CRITICAL - DETECTING BROKEN RESUMES:\n"
    "Before scoring, check if the resume is fundamentally broken or incomplete. Score BELOW 40 if you see:\n"
    "- Text that cuts off mid-sentence or mid-word (e.g., ends with 'cliening I, ComputerSy')\n"
    "- Severe corruption or garbled text that makes content unintelligible\n"
    "- Only shows a tiny fragment (like just a corner or header)\n"
    "- Missing 2+ major sections entirely (Education AND Experience, or Experience AND Skills)\n"
    "- Suspiciously short content (< 100 words for what claims to be a full resume)\n"
    "If ANY of these apply, score 30-40 and clearly state the resume is incomplete/corrupted.\n\n"
    
    "OCR ARTIFACTS: The text may contain minor OCR scanning artifacts (concatenated words, spacing issues). "
    "These have been partially cleaned but may still exist. DO NOT penalize for minor OCR artifacts - focus on "
    "content quality, ATS optimization, and the substance of the resume. Only flag formatting if it's a pattern "
    "that affects readability or ATS parsing, not isolated OCR errors.\n\n"
    
    "SUGGESTION RULES (based on score):\n"
    "- Score 0-50: Provide 10-12 suggestions (critical issues)\n"
    "- Score 51-70: Provide 7-10 suggestions (significant improvements needed)\n"
    "- Score 71-79: Provide 3-6 suggestions (moderate improvements)\n"
    "- Score 80-89: Provide 2-4 suggestions (minor refinements - only high-impact changes)\n"
    "- Score 90-100: Provide 1-2 suggestions (very minor polish - only if truly necessary)\n\n"
    
    "Each suggestion must have:\n"
    "- category: Area of concern (e.g., 'ATS Keywords', 'Content Quality', 'Experience Description', 'Structure')\n"
    "- issue: Specific problem identified (focus on substance, not OCR artifacts)\n"
    "- recommendation: Concrete, actionable fix\n\n"
    
    "FOCUS AREAS (prioritize high-impact suggestions):\n"
    "- Missing critical ATS keywords for the target role (not generic buzzwords)\n"
    "- Weak or vague achievement descriptions that lack impact\n"
    "- Missing quantifiable metrics where they would add value\n"
    "- Missing important sections (e.g., summary for experienced candidates)\n"
    "- Content that doesn't align well with the target role\n\n"
    
    "AVOID (especially for scores 80+):\n"
    "- Minor formatting preferences or style suggestions\n"
    "- Over-optimization (e.g., 'mention X in every bullet point')\n"
    "- Restructuring suggestions when current structure is functional\n"
    "- Nitpicky details that don't materially improve ATS performance\n"
    "- Redundant suggestions (e.g., listing skills already in the skills section)\n\n"
    
    "CRITICAL OUTPUT REQUIREMENT:\n"
    "Your ENTIRE response must be ONLY valid JSON. No explanations before or after.\n"
    "Start your response with { and end with }. Nothing else.\n"
    "Required structure:\n"
    '{"score": <number>, "summary": "<2-3 sentences>", "suggestions": [...]}\n\n'
    "If the resume is broken/incomplete, still return valid JSON with a low score (30-45) and note the issues in the summary.\n\n"
    "Be honest, thorough, and constructively critical about content - not OCR errors."
)

def configure_gemini():
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        raise RuntimeError("GEMINI_API_KEY is not configured in the environment.")
    genai.configure(api_key=api_key)

# cleans common OCR artifacts before analysis 
def clean_ocr_text(text: str) -> str:
    
    text = re.sub(r'([a-z])([A-Z])', r'\1 \2', text)
    text = re.sub(r'([.!?])([A-Z])', r'\1 \2', text)
    text = re.sub(r'(\d+[+%]?)([a-z])', r'\1 \2', text)
    text = re.sub(r'\b(in|to|for|from|with|and|or|of|at)\b([A-Z])', r'\1 \2', text)
    text = re.sub(r'\s+', ' ', text)
    text = re.sub(r'\n\s+\n', '\n\n', text)
    
    return text.strip()

def build_prompt(payload: AnalysisRequest) -> str:
    
    # Clean OCR artifacts before analysis
    original_text = payload.resume_text.strip()
    cleaned_text = clean_ocr_text(payload.resume_text)
    
    # General checks for broken resumes
    is_very_short = len(original_text) < 400
    word_count = len(original_text.split())
    is_minimal_content = word_count < 80
    
    text_looks_broken = is_very_short or is_minimal_content 
    
    base = [
        SYSTEM_PROMPT,
        "",
    ]
    
    if text_looks_broken:
        base.extend([
            "WARNING: This resume appears unusually short or may be incomplete.",
            "Carefully verify the resume ends properly and contains complete sections.",
            "",
        ])
    
    base.extend([
        "Analyze the following resume text.",
        f"Target role: {payload.target_role}",
        "",
        "Resume text:",
        cleaned_text,
        "",
        "Provide your analysis following the scoring guidelines and suggestion rules above.",
    ])
    
    return "\n".join(base)

# escapes all control characters in our input to ensure valid JSON string values
def control_char_clean(json_str: str) -> str:
    final_string = []
    quotation_detected, escape_next = False, False
    i = 0
    
    while i < len(json_str):
        char = json_str[i]
        
        if escape_next:
            final_string.append(char)
            escape_next = False
            i += 1
            continue
        # handle any backslashes
        if char == '\\':
            final_string.append(char)
            escape_next = True
            i += 1
            continue
        # handle any quotation marks
        if char == '"':
            quotation_detected = not quotation_detected
            final_string.append(char)
            i += 1
            continue
        
        # we're now inside a string (not brackets or colon)
        if quotation_detected:
            # escape any control characters we find
            if char == '\n':
                final_string.append('\\n')
            elif char == '\r':
                final_string.append('\\r')
            elif char == '\t':
                final_string.append('\\t')
            else:
                final_string.append(char)
        else:
            final_string.append(char)
        
        i += 1
    
    return ''.join(final_string)

# cleans up Gemini output and extracts JSON
def parse_response(response) -> dict:
    try:
        llm_output = response.text
    except Exception as e:
        # Return fallback response instead of 502 error
        return {
            "score": 40,
            "summary": "Unable to process LLM response. Please try again or re-upload your resume.",
            "suggestions": [
                {
                    "category": "System Error",
                    "issue": "The analysis service encountered an error processing your resume.",
                    "recommendation": "Please try uploading your resume again. If the issue persists, ensure the resume is in a clear, readable format."
                }
            ]
        }

    # remove all whitespace and backticks
    llm_output = llm_output.strip()
    if llm_output.startswith("```json"):
        llm_output = llm_output[7:]  
    if llm_output.startswith("```"):
        llm_output = llm_output[3:] 
    if llm_output.endswith("```"):
        llm_output = llm_output[:-3]  
    llm_output = llm_output.strip()

    # ensure that we're getting standard JSON
    try:
        parsed = json.loads(llm_output)
    except json.JSONDecodeError as e:
        # LLM returned invalid JSON - return fallback instead of 502
        return {
            "score": 40,
            "summary": "The resume analysis encountered a formatting issue. The resume may be incomplete or corrupted.",
            "suggestions": [
                {
                    "category": "Resume Quality",
                    "issue": "Unable to fully analyze the resume due to formatting or completeness issues.",
                    "recommendation": "Please ensure your resume is complete and properly formatted. Try re-uploading a clear photo or PDF of your full resume."
                },
                {
                    "category": "Content Completeness",
                    "issue": "The resume appears to be incomplete or cut off.",
                    "recommendation": "Verify that all sections (Education, Experience, Skills, Projects) are fully captured in the upload."
                }
            ]
        }
    return parsed

@gemini_router.get("/openai_health_check")
def openai_health_check():
    return {"Message": "Gemini LLM endpoint is healthy."}

@gemini_router.post("/analyze", response_model=AnalysisResponse)
async def analyze_resume(payload: AnalysisRequest):
    try:
        configure_gemini()
    except RuntimeError:
        # Return user-friendly error for configuration issues
        raise HTTPException(
            status_code=500,
            detail="The analysis service is temporarily unavailable. Please try again later."
        )
    
    # build a generative model
    try:
        model = genai.GenerativeModel(GEMINI_MODEL)
        prompt = build_prompt(payload)
        response = model.generate_content(prompt)
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail="Unable to analyze your resume at this time. Please try again in a few moments."
        )

    llm_response = parse_response(response)

    try:
        return AnalysisResponse(**llm_response)
    except ValidationError as eexc:
        raise HTTPException(
            status_code=500,
            detail="Unable to process the resume analysis. The resume may be incomplete or corrupted. Please try uploading a clear, complete resume."
        )
