import asyncio
import time
import requests

async def fetch_data(delay):
    print("Fetching data...")
    await asyncio.sleep(delay)
    print("Data fetched")
    return {"data": "some data"}

async def main():
    start = time.perf_counter()
    print("Start of main coroutine")
    task = asyncio.create_task(fetch_data(2))
    task2 = asyncio.create_task(fetch_data(2))
    result = await task
    result2 = await task2
    print(f"Received result: {result}")
    print(f"Received result: {result2}")
    print("End of main coroutine")
    end = time.perf_counter()
    print(f"Time spend: {end - start}")

asyncio.run(main())



"""
links = ['https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/1.html',
'https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/2.html',
'https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/3.html',
'https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/4.html',
'https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/5.html',
'https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/6.html',
'https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/7.html',
'https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/8.html',
'https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/9.html',
'https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/10.html']

def noAsync(links):
    start = time.perf_counter()
    _ = requests.post('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/1.html').text
    _ = requests.post('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/2.html').text
    _ = requests.post('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/3.html').text
    _ = requests.post('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/4.html').text
    _ = requests.post('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/5.html').text
    _ = requests.post('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/1.html').text
    _ = requests.post('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/2.html').text
    _ = requests.post('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/3.html').text
    _ = requests.post('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/4.html').text
    _ = requests.post('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/5.html').text
    end = time.perf_counter()
    print(f"No Async: {end - start}s")


async def crawl(url):
    return requests.post(url).text

async def withAsync(links):
    start = time.perf_counter()
    results = []
    tasks = []
    # for link in links:
    #     tasks.append(asyncio.create_task(crawl(link)))
    # for task in tasks:
    #     results.append(await task)
    task1 = asyncio.create_task(crawl('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/1.html'))
    task2 = asyncio.create_task(crawl('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/2.html'))
    task3 = asyncio.create_task(crawl('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/3.html'))
    task4 = asyncio.create_task(crawl('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/4.html'))
    task5 = asyncio.create_task(crawl('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/5.html'))
    task10 = asyncio.create_task(crawl('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/1.html'))
    task20 = asyncio.create_task(crawl('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/2.html'))
    task30 = asyncio.create_task(crawl('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/3.html'))
    task40 = asyncio.create_task(crawl('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/4.html'))
    task50 = asyncio.create_task(crawl('https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/5.html'))
    _ = await task1
    _ = await task2
    _ = await task3
    _ = await task4
    _ = await task5
    _ = await task10
    _ = await task20
    _ = await task30
    _ = await task40
    _ = await task50

    end = time.perf_counter()
    print(f"With Async: {end - start}s")

noAsync(links)
asyncio.run(withAsync(links))
"""