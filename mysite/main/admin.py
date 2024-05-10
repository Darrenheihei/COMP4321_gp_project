from django.contrib import admin
from .models import SearchResult, EachUserQueryHistory, UserQuery

# Register your models here.
admin.site.register(SearchResult)
admin.site.register(EachUserQueryHistory)
admin.site.register(UserQuery)