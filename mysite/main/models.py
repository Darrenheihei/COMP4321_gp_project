from django.db import models

# Create your models here.
class SearchResult(models.Model):
    title = models.CharField(max_length=200)
    url = models.URLField()
    modDate = models.CharField(max_length=200)
    size = models.IntegerField()
    keywords = models.CharField(max_length=200)

