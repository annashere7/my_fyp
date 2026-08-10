import json
import logging
from fastapi import FastAPI, WebSocket, WebSocketDisconnect

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()

@app.websocket("/stream")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    logger.info("Client connected to /stream")
    try:
        while True:
            # 1. Wait for Metadata Frame (Text)
            metadata_text = await websocket.receive_text()
            try:
                metadata = json.loads(metadata_text)
                frame_id = metadata.get("frameId")
                logger.info(f"Received metadata for frame: {frame_id}")
            except json.JSONDecodeError:
                logger.error("Invalid JSON metadata received")
                continue

            # 2. Wait for Image Data Frame (Binary)
            image_data = await websocket.receive_bytes()
            logger.info(f"Received image data for frame: {frame_id}, size: {len(image_data)} bytes")

            # 3. Send ACK
            ack = {
                "ackFrameId": frame_id,
                "status": "processed"
            }
            await websocket.send_text(json.dumps(ack))

    except WebSocketDisconnect:
        logger.info("Client disconnected")
    except Exception as e:
        logger.error(f"Error in websocket connection: {e}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
