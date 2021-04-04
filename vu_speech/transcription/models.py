from django.db import models
from django.utils import timezone


# Create your models here.


class SpeechUsage(models.Model):
    class Meta:
        verbose_name_plural = 'speech usage'

    speech_datetime = models.DateTimeField(default=timezone.now)
    speech_duration = models.DurationField()


class SpeechStability(models.Model):
    class Meta:
        verbose_name_plural = 'speech stability'

    phrase = models.CharField(max_length=150, unique=True)
    average_stability = models.FloatField(default=0.0)
    count = models.IntegerField(default=0)
