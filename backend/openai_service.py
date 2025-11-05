# OpenAI endpoints go here
from openai import OpenAI
from fastapi import APIRouter
from dotenv import load_dotenv
import os

load_dotenv()

openai_router = APIRouter()

@openai_router.get("/openai_health_check")
def openai_health_check():
    return {"Message": "OpenAI endpoint is healthy."}




