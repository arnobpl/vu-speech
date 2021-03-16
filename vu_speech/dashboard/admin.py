from django.contrib import admin

from .models import Diagnostics


# Register your models here.

class DiagnosticAdmin(admin.ModelAdmin):
    fields = [
        'user_id',
        'speech_datetime',
        'speech_duration',
        'language',
        'accent',
        'stability'
    ]


admin.site.register(Diagnostics, DiagnosticAdmin)
