from fastapi import APIRouter, UploadFile, File, HTTPException
from google.cloud import vision
from dotenv import load_dotenv
import io


ocr_router = APIRouter()

# grabs text from resume image
@ocr_router.post("/extract")
async def extract_resume_text(file: UploadFile = File(...)):
    try:
        contents = await file.read()
        client = vision.ImageAnnotatorClient()
        image = vision.Image(content = contents)
        res = client.text_detection(image = image)
        
        if res.error.message:
            raise HTTPException(status_code=500, detail=res.error.message)
        
        extracted_text = res.full_text_annotation.text.strip()
        if not extracted_text:
            raise HTTPException(status_code=400, detail="No text could be extracted from the image. Please try again.")
        
        return {"extracted_text": extracted_text}
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"OCR extraction failed: {str(e)}")
    
# endpoint health check
@ocr_router.get("/ocr_health_check")
def ocr_health_check():
    return {"Message": "OCR endpoint is healthy."}

