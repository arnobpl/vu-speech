from django.db import models


# Create your models here.


class Elements(models.Model):
    class Meta:
        managed = False

    type = models.CharField(max_length=20)
    value = models.CharField(max_length=250)
    ts = models.IntegerField()
    end_ts = models.IntegerField()
    confidence = models.IntegerField()
