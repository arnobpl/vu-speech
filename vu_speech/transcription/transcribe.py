from rev_ai.streamingclient import RevAiStreamingClient

from .config import *
from .utilities import *


def stream_audio(types, bytes_data):
    config = get_media_config(types)
    # config = MediaConfig("audio/x-raw", "interleaved", 16000, "S16LE", 1)
    print('config',config)
    streamclient = RevAiStreamingClient(ACCESS_KEY, config)
    print('Starting streaming....')
    # print(type(media_generator))
    # print(type(bytes_data))
    bytes_array = [bytes_data]
    response_generator = streamclient.start(bytes_array)
    text = []
    print('Generating response...')
    for resp in response_generator:
        text.append(resp)
        print(resp)
    # print(text)
    streamclient.end()
    return text

# def stream(bytes_data):