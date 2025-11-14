# OpenAI endpoints go here
from openai import OpenAI
from fastapi import APIRouter, HTTPException
from dotenv import load_dotenv
from pydantic import BaseModel, Field, ValidationError
from typing import List, Optional
import json
import os

load_dotenv()

openai_router = APIRouter()


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

OPENAI_MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
MAX_SUGGESTIONS = 6
SYSTEM_PROMPT = (
    "You are a meticulous resume reviewer focused on ATS alignment. "
    "Return JSON that includes a numeric score (0-100), a 2-3 sentence summary, "
    "and up to six actionable suggestions. Each suggestion must have a "
    "category, issue, and recommendation. Be specific, avoid fluff."
)

def get_openai_client() -> OpenAI:
    api_key = os.getenv("OPENAI_API_KEY")
    if not api_key:
        raise RuntimeError("OPENAI_API_KEY is not configured in the environment.")
    return OpenAI(api_key=api_key)


def build_user_prompt(payload: AnalysisRequest) -> str:
    base = [
        "Analyze the following resume text.",
        f"Target role: {payload.target_role}" if payload.target_role else "General role."
    ]
    base.append("Resume:")
    base.append(payload.resume_text.strip())
    base.append(f"Limit suggestions to at most {MAX_SUGGESTIONS}.")
    return "\n\n".join(base)


def parse_response(response) -> dict:
    try:
        content = response.output[0].content[0].text
    except (AttributeError, IndexError) as exc:
        raise HTTPException(
            status_code=502,
            detail=f"Unexpected OpenAI response format: {exc}"
        )

    try:
        parsed = json.loads(content)
    except json.JSONDecodeError:
        raise HTTPException(
            status_code=502,
            detail="OpenAI returned invalid JSON."
        )
    return parsed


@openai_router.get("/openai_health_check")
def openai_health_check():
    return {"Message": "OpenAI endpoint is healthy."}


@openai_router.post("/analyze", response_model=AnalysisResponse)
async def analyze_resume(payload: AnalysisRequest):
    client = get_openai_client()

    try:
        response = client.responses.create(
            model=OPENAI_MODEL,
            input=[
                {"role": "system", "content": [{"type": "text", "text": SYSTEM_PROMPT}]},
                {"role": "user", "content": [{"type": "text", "text": build_user_prompt(payload)}]},
            ],
            response_format={"type": "json_object"}
        )
    except Exception as exc:
        raise HTTPException(
            status_code=502,
            detail=f"OpenAI request failed: {exc}"
        )

    data = parse_response(response)

    try:
        return AnalysisResponse(**data)
    except ValidationError as exc:
        raise HTTPException(
            status_code=502,
            detail=f"OpenAI response failed validation: {exc}"
        )
