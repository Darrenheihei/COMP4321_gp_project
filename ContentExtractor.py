from bs4 import BeautifulSoup
import re
import requests
from urllib.parse import urljoin

class ContentExtractor:
    def __init__(self):
        pass

    def getTitle(self, page: str) -> str:
        soup = BeautifulSoup(page, 'html.parser')
        return soup.title.string

    def getBodyText(self, page: str) -> str:
        soup = BeautifulSoup(page, 'html.parser')
        # print(soup.find('body').get_text(separator=' '))
        return soup.find('body').get_text(separator=' ')

    def splitWords(self, words: str) -> list[str]:
        splittedWords = re.split(r"[^a-zA-Z0-9'\-]+", words)
        return [i.strip('-').strip(' ') for i in splittedWords if i not in ['', '-']]

    def getLastModDate(self, url: str) -> str:
        res = requests.head(url).headers

        if 'Last-Modified' in res:
            return res['Last-Modified']
        else:
            return res['Date']

    def getLinks(self, baseUrl: str, page: str) -> list[str]:
        soup = BeautifulSoup(page, 'html.parser')
        links = []
        for link in soup.find_all('a'): # get all links on the page
            links.append(urljoin(baseUrl, link.get('href')))
        return links

    def getPagesize(self, url: str, bodyText: str) -> int:
        res = requests.head(url).headers
        if 'Content-Length' in res:
            return int(res['Content-Length'])
        else:
            return len(bodyText)

if __name__ == '__main__':
    extractor = ContentExtractor()
    print(extractor.splitWords("Hello-World! How are you?"))