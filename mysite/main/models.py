from django.db import models

# Create your models here.
class SearchResult(models.Model):
    title = models.CharField(max_length=200)
    url = models.URLField()
    modDate = models.CharField(max_length=200)
    size = models.IntegerField()
    keywords = models.CharField(max_length=200)

class EachUserQueryHistory(models.Model):
    cookie_id = models.CharField(max_length=255, unique=True, primary_key=True)
    request = models.BooleanField(default=False) # true if user request reload, so that user can get results in GET from views.py
    request_query = models.CharField(max_length=200) # the request query fot the reload 

class UserQuery(models.Model):
    history = models.ForeignKey(
        EachUserQueryHistory, 
        on_delete=models.CASCADE, 
        related_name="queries"
    )
    query = models.CharField(max_length=200, default='')
    timestamp = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return self.query
