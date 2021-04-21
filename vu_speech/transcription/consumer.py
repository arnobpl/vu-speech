import asyncio
import datetime
import time

from channels.generic.websocket import AsyncWebsocketConsumer
from django.core.exceptions import ValidationError
from django.core.validators import validate_email
from django.db import transaction
from rev_ai.streamingclient import RevAiStreamingClient

from . import models
from .config import *
from .utilities import *
import wave

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

        self.scope['stream_client'] = None
        self.scope['response_generator'] = None

        await self.accept()

    async def disconnect(self, code):
        await self.channel_layer.group_discard(
            self.group_name,
            self.channel_name
        )

    async def receive(self, text_data=None, bytes_data=None):
        if bytes_data is None:
            print('text_data: ' + text_data[:20])
            print(len(text_data))
        else:
            print('bytes_data: ' + str(bytes_data)[:20])
            print(len(bytes_data))

        if self.scope['stream_client'] is None:
            config = get_media_config('raw')
            self.scope['stream_client'] = RevAiStreamingClient(ACCESS_KEY, config)
            self.scope['stream_queue'] = [bytes_data]
        else:
            self.scope['stream_queue'].append(bytes_data)

        if self.scope['response_generator'] is None:
            ## Save bytes data to a wav file.
            ## For testing purpose only
            # sampleRate = 44100.0  # hertz
            # duration = 1.0  # seconds
            # frequency = 440.0  # hertz
            # obj = wave.open('sound.wav', 'w')
            # obj.setnchannels(1)  # mono
            # obj.setsampwidth(2)
            # obj.setframerate(sampleRate)
            # obj.writeframesraw(bytes_data)
            # obj.close()
            self.scope['response_generator'] = self.scope['stream_client'].start(self.stream_generator())
            asyncio.get_event_loop().create_task(self.generate_response())
        else:
            print('response_generator not NONE')

        return 'END'

    def stream_generator(self):
        qu = self.scope['stream_queue']

        while True:
            print('generator loop')

            retry = 3
            while len(qu) == 0:
                retry -= 1
                if retry == 0:
                    print('generator terminated')
                    return
                print('generator retry')
                time.sleep(1)

            print('generator yield')
            val = qu.pop(0)
            if val is None:
                print('val is none')
            else:
                print(str(val)[:20])
            yield val

    async def generate_response(self):
        loop = asyncio.get_event_loop()
        response_generator = self.scope['response_generator']
        for resp in response_generator:
            print('Generating response...')
            print(resp)
            json_obj = json.loads(resp)
            if json_obj['type'] == 'final':
                loop.create_task(diagnostics_data_push(json_obj))
                elements = json_obj['elements']
                for ele in elements:
                    partial_text = ele['value']
                    send_task = loop.create_task(self.send(partial_text))
                    await send_task

        print('Closing connection')
        self.scope['stream_client'].end()
        self.scope['stream_client'] = None
        self.scope['response_generator'] = None
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
