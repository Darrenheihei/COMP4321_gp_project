from django.shortcuts import render, HttpResponse, HttpResponseRedirect
from .forms import UserQuery
import sys
import os
current_dir = os.path.dirname(os.path.realpath(__file__))
parent_dir = os.path.dirname(current_dir)
parent_dir = os.path.dirname(parent_dir)

print("cur: ",current_dir)
print("par: ",parent_dir)
sys.path.append(parent_dir)
from retrieval_function import retrieval_function

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
        rf = retrieval_function()
        results = rf.get_AllResult(query)
    return render(response, "index.html", {"results": results, "form":form, "submitted": submitted})
