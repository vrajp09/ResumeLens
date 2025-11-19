from fastapi import APIRouter, UploadFile, File, HTTPException
import pdfplumber
import io

pdf_router = APIRouter()

# extracts text from PDF f
@pdf_router.post("/extract_pdf")
async def extract_pdf_text(file: UploadFile = File(...)):
    try:
        file_content = await file.read()
        
        return {"extracted_text": "test text"}
    
    except Exception as e:
        raise HTTPException(
            status_code=500, 
            detail=f"PDF extraction failed: {str(e)}"
        )