from django.contrib.auth.decorators import login_required
from django.shortcuts import render

from vu_speech.settings import INTERNAL_IPS


# Create your views here.

@login_required
def index(request):
    return render(request, 'transcription/index.html', {'ip_address': INTERNAL_IPS[0]})
