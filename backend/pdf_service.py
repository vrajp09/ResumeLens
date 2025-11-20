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
        
        extracted = []
        
        with pdfplumber.open(io.BytesIO(file_content)) as pdf:
            all_pages = pdf.pages

            for page in all_pages:
                page_text = page.extract_text() or None
                if page_text:
                    extracted.append(page_text.strip())
        
        full_combined_text = "\n\n".join(extracted).strip()
        
        if not full_combined_text:
            raise HTTPException(
                status_code=400, 
                detail="No text could be extracted from the PDF."
            )
        
        return {"extracted_text": full_combined_text}
    
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500, 
            detail=f"PDF extraction failed: {str(e)}"
        )

@pdf_router.get("/pdf_health_check")
def pdf_health_check():
    return {"Message": "PDF extraction endpoint is healthy."}