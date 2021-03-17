from channels.routing import ProtocolTypeRouter, URLRouter
from channels.auth import AuthMiddlewareStack
from django.conf.urls import url

from django.urls import path
from channels.security.websocket import AllowedHostsOriginValidator
from transcriptionService import consumer

websocket_urlPattern=[
    path('ws/transcriptData/',consumer.VuConsumer.as_asgi()),
]
application=ProtocolTypeRouter({
     'websocket':AllowedHostsOriginValidator(AuthMiddlewareStack(URLRouter(websocket_urlPattern)))
})