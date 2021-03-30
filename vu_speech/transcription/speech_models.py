from django.db import models


class Speech(models.Model):
    type = models.CharField(max_length=20)
    id = models.CharField(max_length=10)

    def __str__(self):
        return self.id
