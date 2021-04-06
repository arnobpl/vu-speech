from channels.generic.websocket import AsyncWebsocketConsumer

from .transcribe import *
from .utilities import *

from rev_ai.streamingclient import RevAiStreamingClient
from .config import *
from .utilities import *
import asyncio

class VuConsumer(AsyncWebsocketConsumer):
    groupname = ''

    async def connect(self):
        print(self.scope)
        self.groupname = 'dashboard'
        await self.channel_layer.group_add(
            self.groupname,
            self.channel_name
        )
        await self.channel_layer.group_send(
            self.groupname,
            {
                'type': 'deprocessing',
                'value': 'connected'
            }
        )
        await self.accept()

    async def disconnect(self, code):
        await self.channel_layer.group_discard(
            self.groupname,
            self.channel_name
        )
        pass

    async def receive(self, text_data=None, bytes_data=None):
        print('>>>>>>>', text_data)
        print('>>>>>', bytes_data)
        # x = parse_data(text_data)
        y = json.loads(text_data)
        bytes_array = []
        # bytes_array = bytes(y["stream"], 'utf-8')
        # bytes_array = bytearray(y["stream"], 'utf-8')
        for b in y["stream"]:
            bytes_array.append(bytes(y["stream"], 'utf-8'))
        # print(bytes_array)
        config = get_media_config(y["type"])
        streamclient = RevAiStreamingClient(ACCESS_KEY, config)
        response_generator = streamclient.start(bytes_array)
        print('Generating response...')
        for resp in response_generator:
            print(resp)
            json_obj = json.loads(resp)
            if json_obj['type'] == 'final':
                elements = json_obj['elements']
                for ele in elements:
                    partial_text = ele['value']
                    loop = asyncio.get_event_loop()
                    task1 = loop.create_task(self.send(partial_text))
                    # task1 = asyncio.create_task(
                    #     self.send(partial_text))
                    await task1
        print('Closing connection')
        streamclient.end()
        # self.disconnect(200)
        await self.close()

        return 'END'

    async def deprocessing(self, event):
        print("Even called!")
        val_other = event['value']
        await self.send(text_data=json.dumps({'type': 'text', 'value': val_other}))

