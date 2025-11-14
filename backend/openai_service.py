# OpenAI endpoints go here
from openai import OpenAI
from fastapi import APIRouter, HTTPException
from dotenv import load_dotenv
from pydantic import BaseModel, Field
from typing import List, Optional
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


def get_openai_client() -> OpenAI:
    api_key = os.getenv("OPENAI_API_KEY")
    if not api_key:
        raise RuntimeError("OPENAI_API_KEY is not configured in the environment.")
    return OpenAI(api_key=api_key)


@openai_router.get("/openai_health_check")
def openai_health_check():
    return {"Message": "OpenAI endpoint is healthy."}


@openai_router.post("/analyze", response_model=AnalysisResponse)
async def analyze_resume(_: AnalysisRequest):
    raise HTTPException(
        status_code=501,
        detail="Resume analysis is not implemented yet."
    )
