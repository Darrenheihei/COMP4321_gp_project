from django.shortcuts import render, HttpResponse, HttpResponseRedirect
from .forms import UserQuery
from .models import SearchResult, EachUserQueryHistory, UserQuery
import uuid
from django.contrib.auth import get_user_model
from django.views.decorators.csrf import csrf_protect
import json
from retrieval_function import retrieval_function

# Create your views here.\

@csrf_protect
def index(request):
    class resultItem:
        def __init__(self, score, title, url, date, size, keywords, parentLinks, childLinks, top5Words):
            self.score = score
            self.title = title
            self.url = url
            self.date = date
            self.size = size
            self.keywords = '; '.join(f"{key} {value}" for key, value in keywords.items())
            self.top5Words = top5Words
            if parentLinks != []:
                self.parentLinks = parentLinks
            else:
                self.parentLinks = ["This page has no parent links"]

            if childLinks != []:
                self.childLinks = childLinks
            else:
                self.childLinks = ["This page has no child links"]

    
    submitted = False
    result1 = resultItem(0, "Test page", "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm" , "1-1-1111", 100, {'a': 10, 'b': 20}, ["parent 1", "parent 2"], ["child 1"],"happy sad excited")
    result2 = resultItem(0, "CSE department of HKUST", "https://www.cse.ust.hk/~kwtleung/COMP4321/ust_cse.htm", "1-1-1111", 100, {'c': 30, 'd': 40}, [], [],"darren alex sunny kiki amy")
    results = [result1,result2]


    if request.method == 'GET':
        cookie_id = request.COOKIES.get('user_cookie_id')
        
        # first time using the browser to view the webpage
        if not cookie_id:
            cookie_id = uuid.uuid4()
            
            # ensure each user has a query history
            new_user_query_history, created = EachUserQueryHistory.objects.get_or_create(cookie_id=str(cookie_id))

            #form = UserQuery()
            
            #response = render(request, "index.html", {"message": "First time using search engine, thus a cookie will be set.", "results": results, "form":form, "submitted": submitted})
            #response.set_cookie('user_cookie_id', str(cookie_id), max_age=31536000)  # max_age unit is second, thus here is 1 year to be expired
            response = render(request, "index.html", {"message": "First time using search engine, thus a cookie will be set.", "submitted": submitted})
            response.set_cookie('user_cookie_id', str(cookie_id), max_age=31536000)  # max_age unit is second, thus here is 1 year to be expired
            return response
        
        # not the first time using the browser to view the webpage( i.e have cookie already)
        else:
            user_query_history = EachUserQueryHistory.objects.get(cookie_id=cookie_id)
            user_queries = UserQuery.objects.filter(history=user_query_history).order_by('-timestamp')[:20]

            #form = UserQuery()

            return render(request, "index.html", {"queries": user_queries, "submitted": submitted})

    # query recevied 
    elif request.method == 'POST':

        #form = UserQuery(request.POST) #darren
        #query = form.cleaned_data["query"] #darren this gives you the query the user submitted

        cookie_id = request.COOKIES.get('user_cookie_id')

        #if cookie_id and form.is_valid(): #darren
        if cookie_id:
            submitted = True #darren

            if request.content_type == 'application/json': # check is it from get similar page button
                data = json.loads(request.body)
                top5Words = data.get('top5Words', '')
                query_text = top5Words if top5Words else data.get('query', '')
            else:
                query_text = request.POST.get('query', '')

            user_query_history = EachUserQueryHistory.objects.get(cookie_id=cookie_id)

            # TODO: uncomment the line below and call the ranking function
            rf = retrieval_function()
            results = rf.get_AllResult(query_text)

            # check the query exist or not already in database or the query is just emprt
            existing_query = UserQuery.objects.filter(history=user_query_history, query=query_text).first()

            if not existing_query and bool(query_text.strip()):

                new_query = UserQuery(query=query_text, history=user_query_history)
                new_query.save()

                # we only save the 10 latest query for each user
                if UserQuery.objects.filter(history=user_query_history).count() > 5:
                    UserQuery.objects.filter(pk__in=UserQuery.objects.filter(history=user_query_history).order_by('-timestamp').values_list('pk', flat=True)[5:]).delete()

            user_queries = UserQuery.objects.filter(history=user_query_history).order_by('-timestamp')[:20]
            return render(request, "index.html", {"results": results, "submitted": submitted, "queries": user_queries}) #darren        
        else:
            return HttpResponse("No cookie found. Need to enable cookies", status=400)

