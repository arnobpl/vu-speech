import websocket, json

socket = "wss://api.rev.ai/speechtotext/v1/stream?access_token=02QlwoZ46LJpb0meCW3Q7FEgVmY8ogZeKZflvmGHc-9JcUDi53qrtYOscMUbVDm6_Pon1gH5vJIAaORsCY2XdVP3iM6ds&content_type=audio/x-raw;layout=interleaved;rate=16000;format=S16LE;channels=1"

def on_open(ws):
    print("opened")

    filename = "C:/Users/Vinita/Documents/cse611/Git/vu-speech/vu_speech/audio_test/english_test.raw"
    with io.open(filename, 'rb') as stream:
        print("sending data:")
        ws.send(stream.read())

def on_message(ws, message):
    print("Recieved a message:")
    print(message)
print("Trying to connect")
ws = websocket.WebSocketApp(socket, on_open=on_open, on_message=on_message)
ws.run_forever()