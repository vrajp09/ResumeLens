from fastapi import APIRouter, UploadFile, File, HTTPException
from google.cloud import vision
from dotenv import load_dotenv

ocr_router = APIRouter()

@ocr_router.get("/")
def ocr_health_check():
    return {"Message": "OCR endpoint is healthy."}