from rev_ai.models import MediaConfig


def get_media_config(text):
    config = None
    if text == 'raw':
        config = MediaConfig('audio/x-raw', 'interleaved', 44100, 'S16LE', 1)
    elif text == 'mp3' or text == 'mp4':
        config = MediaConfig('audio/x-mpeg')
    elif text == 'flac':
        config = MediaConfig('audio/x-flac')
    elif text == 'wav':
        config = MediaConfig('audio/x-wav')
    return config

