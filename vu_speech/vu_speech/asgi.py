"""
ASGI config for vu_speech project.
It exposes the ASGI callable as a module-level variable named ``application``.
For more information on this file, see
https://docs.djangoproject.com/en/3.1/howto/deployment/asgi/
"""

import os

# from django.core.asgi import get_asgi_application
from channels.auth import AuthMiddlewareStack
from channels.routing import ProtocolTypeRouter, URLRouter
from channels.security.websocket import AllowedHostsOriginValidator
from django.urls import path

from transcription import consumer

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'vu_speech.settings')

websocket_urlPattern = [
    path('ws/transcriptData/', consumer.VuConsumer.as_asgi()),
]

# application = get_asgi_application()
application = ProtocolTypeRouter({
    'websocket': AllowedHostsOriginValidator(AuthMiddlewareStack(URLRouter(websocket_urlPattern)))
})

