from django.db import models

class elements(models.Model):
    type=models.CharField(max_length=20)
    value=models.CharField(max_length=250)
    ts=models.IntegerField()
    end_ts=models.IntegerField()
    confidence=models.IntegerField()

