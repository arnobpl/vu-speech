from rev_ai.models import MediaConfig
from rev_ai.streamingclient import RevAiStreamingClient
import io
import pyaudio
from six.moves import queue

class MicrophoneStream(object):
    def __init__(self, rate, chunk):
        self.rate = rate
        self._chunk = chunk
        self._buff = queue.Queue()
        self.closed = True

    def __enter__(self):
        self._audio_interface = pyaudio.PyAudio()
        self._audio_stream = self._audio_interface.open(
            format=pyaudio.paInt16,
            channels=1,
            rate=self.rate,
            input=True,
            frames_per_buffer=self._chunk,
            stream_callback=self._fill_buffer,
        )
        self.closed=False
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self._audio_stream.stop_stream()
        self._audio_stream.close()
        self.closed=True
        self._buff.put(None)
        self._audio_interface.terminate()

    def _fill_buffer(self, in_data, frame_count, time_info, status_flags):
        self._buff.put(in_data)
        return None, pyaudio.paContinue

    def generator(self):
        while not self.closed:
            chunk = self._buff.get()
            if chunk is None:
                return
            data = [chunk]

            while True:
                try:
                    chunk=self._buff.get(block=False)
                    if chunk is None:
                        return
                    data.append(chunk)
                except queue.Empty:
                    break
            yield b''.join(data)

rate = 44100
chunk = int(rate / 10)

#TODO: add access token from rev.ai
access_token = "02QlwoZ46LJpb0meCW3Q7FEgVmY8ogZeKZflvmGHc-9JcUDi53qrtYOscMUbVDm6_Pon1gH5vJIAaORsCY2XdVP3iM6ds"

config = MediaConfig("audio/x-raw", "interleaved", 16000, "S16LE", 1)

streamclient = RevAiStreamingClient(access_token, config)

with MicrophoneStream(rate, chunk) as stream:
    try:
        response_gen = streamclient.start(stream.generator())
        for resp in response_gen:
            print(resp)

    except KeyboardInterrupt:
        streamclient.end()
        pass