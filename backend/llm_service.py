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
        None,
        description="Optional role or industry focus to tailor suggestions."
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
    "- Below 50: Severely flawed. Incomplete, illegible, or fundamentally broken resume.\n\n"
    
    "IMPORTANT: If the resume is incomplete, illegible, or only shows a fragment (like just a corner), "
    "score it below 40 and note the incompleteness.\n\n"
    
    "OCR ARTIFACTS: The text may contain minor OCR scanning artifacts (concatenated words, spacing issues). "
    "These have been partially cleaned but may still exist. DO NOT penalize for minor OCR artifacts - focus on "
    "content quality, ATS optimization, and the substance of the resume. Only flag formatting if it's a pattern "
    "that affects readability or ATS parsing, not isolated OCR errors.\n\n"
    
    "SUGGESTION RULES (based on score):\n"
    "- Score 0-50: Provide 8-12 suggestions (critical issues)\n"
    "- Score 51-70: Provide 5-8 suggestions (significant improvements needed)\n"
    "- Score 71-85: Provide 3-5 suggestions (moderate improvements)\n"
    "- Score 86-100: Provide 1-3 suggestions (minor refinements)\n\n"
    
    "Each suggestion must have:\n"
    "- category: Area of concern (e.g., 'ATS Keywords', 'Content Quality', 'Experience Description', 'Structure')\n"
    "- issue: Specific problem identified (focus on substance, not OCR artifacts)\n"
    "- recommendation: Concrete, actionable fix\n\n"
    
    "AVOID: Do not create suggestions about minor typos, spacing issues, or OCR artifacts. Focus on:\n"
    "- Missing ATS keywords for the target role\n"
    "- Weak or vague achievement descriptions\n"
    "- Lack of quantifiable metrics\n"
    "- Missing important sections (summary, skills grouping)\n"
    "- Poor action verb usage\n"
    "- Content that doesn't align with the target role\n\n"
    
    "Return ONLY valid JSON with this exact structure:\n"
    '{"score": <number>, "summary": "<2-3 sentences>", "suggestions": [...]}\n\n'
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
    cleaned_text = clean_ocr_text(payload.resume_text)
    
    base = [
        SYSTEM_PROMPT,
        "",
        "Analyze the following resume text.",
        f"Target role: {payload.target_role}" if payload.target_role else "Target role: General/Any position",
        "",
        "Resume text:",
        cleaned_text,
        "",
        "Provide your analysis following the scoring guidelines and suggestion rules above.",
    ]
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
        raise HTTPException(
            status_code=502,
            detail=f"Incorrect Gemini response format: {e}"
        )

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
        raise HTTPException(
            status_code=502,
            detail=f"Invalid JSON: {e}. Truncated repsonse: {llm_output[:150]}"
        )
    return parsed

@gemini_router.get("/openai_health_check")
def openai_health_check():
    return {"Message": "Gemini LLM endpoint is healthy."}


@gemini_router.post("/analyze", response_model=AnalysisResponse)
async def analyze_resume(payload: AnalysisRequest):
    configure_gemini()
    
    # build a generative model
    try:
        model = genai.GenerativeModel(GEMINI_MODEL)
        prompt = build_prompt(payload)
        response = model.generate_content(prompt)
    except Exception as e:
        raise HTTPException(
            status_code=502,
            detail=f"Gemini request failed: {e}"
        )

    llm_response = parse_response(response)

    try:
        return AnalysisResponse(**llm_response)
    except ValidationError as eexc:
        raise HTTPException(
            status_code=502,
            detail=f"Gemini response failed validation: {e}"
        )
