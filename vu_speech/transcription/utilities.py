import ast
import json

from rev_ai.models import MediaConfig


def get_media_config(text):
    config = None
    if text == 'raw':
        config = MediaConfig('audio/x-raw', 'interleaved', 16000, 'S16LE', 1)
    elif text == 'mp3' or text == 'mp4':
        config = MediaConfig('audio/x-mpeg')
    elif text == 'flac':
        config = MediaConfig('audio/x-flac')
    elif text == 'wav':
        config = MediaConfig('audio/x-wav')
    return config


def get_transcription_text(json_obj):
    elements = json_obj['elements']
    text = ''
    for ele in elements:
        text += ele['value']
        text += ' '
    return text


def parse_data(text_data):
    y = json.loads(text_data)
    x = ast.literal_eval(y)
    # print(x['type'])
    # print(x['stream'])
    return x
