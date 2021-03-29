import json

from rev_ai.models import MediaConfig


# TODO: get the file type: raw/mp3/mp4/flav
def get_media_config(filename):
    text = filename.split('/')
    text = text[len(text) - 1]
    text = text.split('.')
    text = text[len(text) - 1]

    config = None
    if text == 'raw':
        print("*********its raw y'all")
        config = MediaConfig("audio/x-raw", "interleaved", 16000, "S16LE", 1)
    elif text == 'mp3' or text == 'mp4':
        config = MediaConfig("audio/x-mpeg")
    elif text == 'flac':
        config = MediaConfig("audio/x-flac")
    elif text == 'wav':
        config = MediaConfig("audio/x-wav")
    return config


def get_final_transcription(text):
    json_obj = None
    # json_obj = json.loads(text)
    for t in text:
        json_obj = json.loads(t)
        # print()
        if json_obj['type'] == 'final':
            final_obj = json_obj

    elements = json_obj['elements']
    text = ''
    for ele in elements:
        text += ele['value']
        # print(ele['value'])
    # print(text)
    return text
