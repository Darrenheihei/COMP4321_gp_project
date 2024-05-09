from django.shortcuts import render, HttpResponse, HttpResponseRedirect
from .forms import UserQuery
from .models import SearchResult, EachUserQueryHistory, UserQuery
import uuid
from django.contrib.auth import get_user_model
from django.views.decorators.csrf import csrf_protect
import json
from retrieval_function import retrieval_function
from django.urls import reverse
from django.shortcuts import redirect

# Create your views here.\

@csrf_protect
def index(request):
    submitted = False
    results = []

    if request.method == 'GET':
        cookie_id = request.COOKIES.get('user_cookie_id')
        
        # first time using the browser to view the webpage
        if not cookie_id:
            cookie_id = uuid.uuid4()
            
            # ensure each user has a query history
            new_user_query_history, created = EachUserQueryHistory.objects.get_or_create(cookie_id=str(cookie_id))

            #form = UserQuery()

            response = render(request, "index.html", {"message": "First time using search engine, thus a cookie will be set.", "submitted": submitted})
            response.set_cookie('user_cookie_id', str(cookie_id), max_age=31536000)  # max_age unit is second, thus here is 1 year to be expired
            return response
        
        # not the first time using the browser to view the webpage( i.e have cookie already)
        else:
            user_query_history = EachUserQueryHistory.objects.get(cookie_id=cookie_id)
            user_queries = UserQuery.objects.filter(history=user_query_history).order_by('-timestamp')[:20]

            return render(request, "index.html", {"queries": user_queries, "submitted": submitted})

    # query recevied 
    elif request.method == 'POST':

        #form = UserQuery(request.POST) #darren
        #query = form.cleaned_data["query"] #darren this gives you the query the user submitted
        
        cookie_id = request.COOKIES.get('user_cookie_id')

        #if cookie_id and form.is_valid(): #darren
        if cookie_id:
            submitted = True #darren

            user_query_history = EachUserQueryHistory.objects.get(cookie_id=cookie_id)

            if user_query_history.request:
                submitted = True

                query_text = user_query_history.request_query

                rf = retrieval_function()
                results = rf.get_AllResult(query_text)

                # check the query exist or not already in database or the query is just emprt
                existing_query = UserQuery.objects.filter(history=user_query_history, query=query_text).first()

                if not existing_query and bool(query_text.strip()):

                    new_query = UserQuery(query=query_text, history=user_query_history)
                    new_query.save()

                    # we only save the 5 latest query for each user
                    if UserQuery.objects.filter(history=user_query_history).count() > 5:
                        UserQuery.objects.filter(pk__in=UserQuery.objects.filter(history=user_query_history).order_by('-timestamp').values_list('pk', flat=True)[5:]).delete()

                user_queries = UserQuery.objects.filter(history=user_query_history).order_by('-timestamp')[:20]

                user_query_history.request = False
                user_query_history.request_query = ""
                user_query_history.save()

                return render(request, "index.html", {"results": results, "submitted": submitted, "queries": user_queries}) 

            if request.content_type == 'application/json': # check is it from get similar page button
                data = json.loads(request.body)
                top5Words = data.get('top5Words', '')
                query_text = top5Words if top5Words else data.get('query', '')

                user_query_history = EachUserQueryHistory.objects.get(cookie_id=cookie_id)
                user_query_history.request = True
                user_query_history.request_query = query_text
                user_query_history.save()

                return render(request, "index.html", {"results": results, "submitted": submitted})  

            query_text = request.POST.get('query', '')

            rf = retrieval_function()
            results = rf.get_AllResult(query_text)

            # check the query exist or not already in database or the query is just emprt
            existing_query = UserQuery.objects.filter(history=user_query_history, query=query_text).first()

            if not existing_query and bool(query_text.strip()):

                new_query = UserQuery(query=query_text, history=user_query_history)
                new_query.save()

                # we only save the 5 latest query for each user
                if UserQuery.objects.filter(history=user_query_history).count() > 5:
                    UserQuery.objects.filter(pk__in=UserQuery.objects.filter(history=user_query_history).order_by('-timestamp').values_list('pk', flat=True)[5:]).delete()

            user_queries = UserQuery.objects.filter(history=user_query_history).order_by('-timestamp')[:20]

            return render(request, "index.html", {"results": results, "submitted": submitted, "queries": user_queries}) #darren      
        else:
            return HttpResponse("No cookie found. Need to enable cookies", status=400)

