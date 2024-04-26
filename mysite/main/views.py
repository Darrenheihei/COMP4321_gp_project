from django.shortcuts import render, HttpResponse, HttpResponseRedirect
from .forms import UserQuery

# Create your views here.
def index(response):

    class resultItem:
        def __init__(self, score, title, url, date, size, keywords, parentLinks, childLinks):
            self.score = score
            self.title = title
            self.url = url
            self.date = date
            self.size = size
            self.keywords = '; '.join(f"{key} {value}" for key, value in keywords.items())
            if parentLinks != []:
                self.parentLinks = parentLinks
            else:
                self.parentLinks = ["This page has no parent links"]

            if childLinks != []:
                self.childLinks = childLinks
            else:
                self.childLinks = ["This page has no child links"]


    submitted = False
    # result1 = resultItem(0, "Test page", "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm" , "1-1-1111", 100, {'a': 10, 'b': 20}, ["parent 1", "parent 2"], ["child 1"])
    # result2 = resultItem(0, "CSE department of HKUST", "https://www.cse.ust.hk/~kwtleung/COMP4321/ust_cse.htm", "1-1-1111", 100, {'c': 30, 'd': 40}, [], [])
    results = []

    if response.method == "POST":
        form = UserQuery(response.POST)
    else:
        form = UserQuery()

    if form.is_valid():
        submitted = True
        query = form.cleaned_data["query"] # this gives you the query the user submitted
        # TODO: uncomment the line below and call the ranking function
        # results = <ranking_function>()
    return render(response, "index.html", {"results": results, "form":form, "submitted": submitted})
