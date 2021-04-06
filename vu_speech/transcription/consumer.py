import asyncio
import datetime

from channels.generic.websocket import AsyncWebsocketConsumer
from django.core.exceptions import ValidationError
from django.core.validators import validate_email
from django.db import transaction
from rev_ai.streamingclient import RevAiStreamingClient

from . import models
from .config import *
from .utilities import *


class VuConsumer(AsyncWebsocketConsumer):
    group_name = 'transcription'

    async def connect(self):
        # print(self.scope)
        await self.channel_layer.group_add(
            self.group_name,
            self.channel_name
        )
        await self.channel_layer.group_send(
            self.group_name,
            {
                'type': 'notify',
                'value': 'connected'
            }
        )
        await self.accept()

    async def disconnect(self, code):
        await self.channel_layer.group_discard(
            self.group_name,
            self.channel_name
        )

    async def receive(self, text_data=None, bytes_data=None):
        if text_data is not None:
            x = parse_data(text_data)
            bytes_array = [x['stream']]
            config = get_media_config(x['type'])
        else:
            bytes_array = bytes_data
            config = get_media_config('raw')

        stream_client = RevAiStreamingClient(ACCESS_KEY, config)
        response_generator = stream_client.start(bytes_array)

        loop = asyncio.get_event_loop()

        print('Generating response...')
        for resp in response_generator:
            json_obj = json.loads(resp)
            if json_obj['type'] == 'final':
                # print(resp)
                loop.create_task(diagnostics_data_push(json_obj))
                elements = json_obj['elements']
                for ele in elements:
                    partial_text = ele['value']
                    send_task = loop.create_task(self.send(partial_text))
                    await send_task

        print('Closing connection')
        stream_client.end()
        await self.close()

    async def notify(self, event):
        # print('Notify event called!')
        value = event['value']
        await self.send(text_data=json.dumps({'type': 'text', 'value': value}))


async def diagnostics_data_push(json_obj):
    def _data_push():
        time_start = json_obj['ts']
        time_end = json_obj['end_ts']

        duration = datetime.timedelta(seconds=time_end - time_start)
        sp_usage = models.SpeechUsage(speech_duration=duration)
        sp_usage.save()

        elements = json_obj['elements']
        for ele in elements:
            if ele['type'] != 'text':
                continue

            phrase = ele['value']
            if has_private_data(phrase):
                continue

            phrase = phrase.lower()
            stability = ele['confidence']

            with transaction.atomic():
                sp_stability, _ = (models.SpeechStability.objects.select_for_update()
                                   .get_or_create(phrase=phrase)
                                   )
                sp_stability.average_stability = (sp_stability.average_stability
                                                  + ((stability - sp_stability.average_stability)
                                                     / (sp_stability.count + 1))
                                                  )
                sp_stability.count += 1
                sp_stability.save()

    await asyncio.get_event_loop().run_in_executor(None, func=_data_push)


def has_private_data(data):
    # number
    if any(ch.isdigit() for ch in data):
        return True

    # acronym
    if len(data) > 1 and data.isupper():
        return True

    # email
    try:
        validate_email(data)
        return True
    except ValidationError:
        pass

    return False
