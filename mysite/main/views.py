from django.shortcuts import render, HttpResponse
from .models import SearchResult

# Create your views here.

def index(request):
    class resultItem:
        def __init__(self, score, title, url, keywords=None, parentLinks=None, childLinks=None):
            self.score = score
            self.title = title
            self.url = url
            self.keywords = keywords
            self.parentLinks = parentLinks
            self.childLinks = childLinks

    result1 = resultItem("Test page", "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm")
    result2 = resultItem("CSE department of HKUST", "https://www.cse.ust.hk/~kwtleung/COMP4321/ust_cse.htm")
    return render(request, "index.html", {"results": [result1, result2]})
