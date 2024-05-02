# import requests
# from requests import get
# from bs4 import BeautifulSoup
# # import numpy as np
# import time
# import random
#
# pages = list(range(1, 51, 1))
#
# book_title = []
# star_rating = []
# product_price = []
#
# start = time.time()
# for page in pages:
#     time.sleep(random.randint(1, 10))
#
#     url = 'http://books.toscrape.com/catalogue/page-' + str(page) + '.html'
#     results = requests.get(url)
#     soup = BeautifulSoup(results.text, 'html.parser')
#
#     book_div = soup.find_all('li', class_='col-xs-6 col-sm-4 col-md-3 col-lg-3')
#
#     for container in book_div:
#         title = container.article.h3.a['title']
#         book_title.append(title)
#
#         price = container.article.find('div', class_='product_price').p.text
#         product_price.append(price)
#
#         rating = container.article.p['class'][-1]
#         star_rating.append(rating)
#
# end = time.time()
# print('It took', (end - start), 'seconds')

import requests
from bs4 import BeautifulSoup
import time
import random
# import numpy as np
from multiprocessing import Pool

url_list = []

pages = list(range(1, 51, 1))


def generate_urls():
    for page in pages:
        url = 'http://books.toscrape.com/catalogue/page-' + str(page) + '.html'
        url_list.append(url)


def scrape_url(url):
    book_title = []
    star_rating = []
    product_price = []

    time.sleep(random.randint(1, 10))
    results = requests.get(url)
    soup = BeautifulSoup(results.text, 'html.parser')

    book_div = soup.find_all('li', class_='col-xs-6 col-sm-4 col-md-3 col-lg-3')

    for container in book_div:
        title = container.article.h3.a['title']
        book_title.append(title)

        price = container.article.find('div', class_='product_price').p.text
        product_price.append(price)

        rating = container.article.p['class'][-1]
        star_rating.append(rating)

    return (book_title, product_price, star_rating)

if __name__ == '__main__':
    generate_urls()

    start = time.time()

    p = Pool(10)
    book_list = p.map(scrape_url, url_list)
    p.terminate()
    p.join()

    end = time.time()
    print('It took', (end - start), 'seconds')