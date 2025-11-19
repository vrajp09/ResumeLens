# Gemini LLM endpoints go here
import google.generativeai as genai
from fastapi import APIRouter, HTTPException
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
    resume_text: str = Field(..., min_length=50, max_length=6000)
    target_role: Optional[str] = Field(
        None,
        description="Optional role or industry focus to tailor suggestions."
    )


class AnalysisResponse(BaseModel):
    score: int = Field(..., ge=0, le=100)
    summary: str
    suggestions: List[Suggestion]

GEMINI_MODEL = os.getenv("GEMINI_MODEL", "gemini-1.5-flash")
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
    """Build the complete prompt for Gemini including system instructions."""
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


@gemini_router.get("/openai_health_check")
def openai_health_check():
    return {"Message": "Gemini LLM endpoint is healthy."}


@gemini_router.post("/analyze", response_model=AnalysisResponse)
async def analyze_resume(payload: AnalysisRequest):
    configure_gemini()
    
    try:
        model = genai.GenerativeModel(GEMINI_MODEL)
        prompt = build_prompt(payload)
        response = model.generate_content(prompt)
    except Exception as exc:
        raise HTTPException(
            status_code=502,
            detail=f"Gemini request failed: {exc}"
        )

    try:
        return AnalysisResponse(**response)
    except ValidationError as exc:
        raise HTTPException(
            status_code=502,
            detail=f"Gemini response failed validation: {exc}"
        )
