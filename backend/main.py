# FastAPI entrypoint
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv
from llm_service import gemini_router 
from ocr_service import ocr_router

load_dotenv()
app = FastAPI()

app.include_router(gemini_router)
app.include_router(ocr_router)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/root_health_check")
def root_health_check():
    return {"Message": "Backend is healthy."}

