from channels.generic.websocket import AsyncWebsocketConsumer
import json

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
        pass