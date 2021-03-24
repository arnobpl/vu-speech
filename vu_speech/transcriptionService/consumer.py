from channels.generic.websocket import AsyncWebsocketConsumer
from rev_ai.streamingclient import RevAiStreamingClient
import io
import json

from transcriptionService.config import *
from transcriptionService.transcribe_file import *
from transcriptionService.utilities import *

class VuConsumer(AsyncWebsocketConsumer):

    async def connect(self):
        print(self.scope)
        self.groupname='dashboard'
        await self.channel_layer.group_add(
            self.groupname,
            self.channel_name
        )
        await self.accept()

    async def disconnect(self, code):
        # await self.disconnect()
        pass

    async def receive(self, text_data=None, bytes_data=None):
        print('>>>>>',text_data)
        ##TODO: find out if its a file or stream
        text = stream_audio(text_data)

        ##TODO: split the json object and extract final text
        text = get_final_transcription(text)
        print("Final Transcription recieved:")
        print(text)
        val = text
        await self.channel_layer.group_send(
            self.groupname,
            {
                'type': 'deprocessing',
                'value': val
            }
        )

        return text

    async def deprocessing(self, event):
        valOther = event['value']
        await self.send(text_data=json.dumps({'type':'text', 'value': valOther}))