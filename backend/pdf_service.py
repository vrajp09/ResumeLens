from fastapi import APIRouter, UploadFile, File, HTTPException
import pdfplumber
import io

pdf_router = APIRouter()

# extracts text from PDF files
@pdf_router.post("/extract_pdf")
async def extract_pdf_text(file: UploadFile = File(...)):
    try:
        file_content = await file.read()
        
        # file validation
        if not file.filename.lower().endswith('.pdf'):
            raise HTTPException(
                status_code=400, 
                detail="Invalid file type. Please upload a PDF file."
            )
        
        return {"extracted_text": "test text"}
    
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500, 
            detail=f"PDF extraction failed: {str(e)}"
        )