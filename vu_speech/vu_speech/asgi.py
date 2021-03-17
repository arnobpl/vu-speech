"""
ASGI config for vu_speech project.
It exposes the ASGI callable as a module-level variable named ``application``.
For more information on this file, see
https://docs.djangoproject.com/en/3.1/howto/deployment/asgi/
"""

import os

from django.core.asgi import get_asgi_application
from channels.asgi import get_channel_layer()

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'vu_speech.settings')

# application = get_asgi_application()
application = get_channel_layer()