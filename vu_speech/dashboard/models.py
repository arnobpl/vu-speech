from django.contrib.auth import get_user_model
from django.db import models


# Create your models here.


class Diagnostics(models.Model):
    class Meta:
        verbose_name_plural = "Diagnostics"

    user = models.ForeignKey(get_user_model(), on_delete=models.CASCADE)
    speech_datetime = models.DateTimeField('Call Date and Time Initiated')
    speech_duration = models.DurationField('Call Duration')
    language = models.CharField('Language', max_length=50)
    accent = models.CharField('Accent', max_length=50)
    stability = models.DecimalField('Call Confidence', max_digits=2, decimal_places=2)
