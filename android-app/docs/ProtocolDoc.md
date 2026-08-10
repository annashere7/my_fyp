# Robot Vision: Android-to-Backend Protocol

This document defines the WebSocket communication protocol between the Android Robot Vision app and the Python MiDaS backend.

## Connection
- **Protocol**: WebSocket (WS/WSS)
- **Endpoint**: `ws://<server-ip>:<port>/stream`

## Message Flow
The streaming client sends frames to the server in pairs. For every frame captured by the camera, two WebSocket frames are sent sequentially:

### 1. Metadata Frame (Text)
A JSON string containing the metadata for the frame.

```json
{
  "frameId": 12345,
  "timestamp": 1691234567890,
  "width": 1280,
  "height": 720,
  "rotation": 90
}
```
- `frameId`: Monotonically increasing counter for sequence tracking.
- `timestamp`: Unix timestamp (milliseconds) of when the frame was captured.
- `width`/`height`: Dimensions of the image buffer.
- `rotation`: Clockwise rotation degrees (0, 90, 180, 270) required to orient the image upright.

### 2. Image Data Frame (Binary)
Immediately following the metadata text frame, the client sends a binary frame containing the compressed JPEG bytes of the image.

## Server Expectations
1. The server must accept the WebSocket connection on the `/stream` path.
2. The server should loop, receiving one Text frame, parsing the JSON metadata, and then receiving the next Binary frame containing the JPEG bytes.
3. The server can optionally send back a JSON response (ACK) containing the `frameId` to allow the Android client to calculate round-trip latency.

### Example Server ACK (Optional but Recommended)
```json
{
  "ackFrameId": 12345,
  "status": "processed"
}
```
