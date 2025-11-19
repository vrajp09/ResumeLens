# Gemini LLM endpoints go here
import google.generativeai as genai
from fastapi import APIRouter, HTTPException, Request
from dotenv import load_dotenv
from pydantic import BaseModel, Field, ValidationError
from typing import List, Optional
import json
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
    suggestions: List[Suggestion]

GEMINI_MODEL = os.getenv("GEMINI_MODEL", "gemini-2.5-flash-lite")
MAX_SUGGESTIONS = 6
SYSTEM_PROMPT = (
    "You are a meticulous resume reviewer focused on ATS alignment. "
    "Return JSON that includes a numeric score (0-100), a 2-3 sentence summary, "
    "and up to six actionable suggestions. Each suggestion must have a "
    "category, issue, and recommendation. Be specific, avoid fluff. "
    "Return ONLY valid JSON, no additional text."
)

def configure_gemini():
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        raise RuntimeError("GEMINI_API_KEY is not configured in the environment.")
    genai.configure(api_key=api_key)


def build_prompt(payload: AnalysisRequest) -> str:
    base = [
        SYSTEM_PROMPT,
        "",
        "Analyze the following resume text.",
        f"Target role: {payload.target_role}" if payload.target_role else "General role.",
        "",
        "Resume:",
        payload.resume_text.strip(),
        "",
        f"Limit suggestions to at most {MAX_SUGGESTIONS}.",
        "",
        "Return your response as a JSON object with the following structure:",
        '{"score": <number>, "summary": "<string>", "suggestions": [{"category": "<string>", "issue": "<string>", "recommendation": "<string>"}]}'
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
async def analyze_resume(request: Request):
    try:
        model = genai.GenerativeModel(GEMINI_MODEL)
        prompt = build_prompt(request)
        response = model.generate_content(prompt)
    except Exception as exc:
        raise HTTPException(
            status_code=502,
            detail=f"Gemini request failed: {exc}"
        )

    response_data = parse_response(response)

    try:
        return AnalysisResponse(**response_data)
    except ValidationError as exc:
        raise HTTPException(
            status_code=502,
            detail=f"Gemini response failed validation: {exc}"
        )
