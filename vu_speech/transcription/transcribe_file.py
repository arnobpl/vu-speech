import io

from rev_ai.streamingclient import RevAiStreamingClient

from .config import *
from .utilities import *


def stream_audio(filename):
    config = get_media_config(filename)
    # config = MediaConfig("audio/x-raw", "interleaved", 16000, "S16LE", 1)
    streamclient = RevAiStreamingClient(ACCESS_KEY, config)

    with io.open(filename, 'rb') as stream:
        media_generator = [stream.read()]

    response_generator = streamclient.start(media_generator)

    text = []
    for resp in response_generator:
        text.append(resp)

    # print(text)
    streamclient.end()
    return text
