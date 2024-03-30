import ssl
import sqlite3
import uuid
import queue
import requests

from ContentExtractor import ContentExtractor
from StopStem import StopStem
from Indexer import Indexer

CURSOR_UP = "\033[1A"
CLEAR = "\x1b[2K"
CLEAR_LINE = CURSOR_UP + CLEAR

class Spider:
    def __init__(self, startUrl: str, n: int):
        self.startUrl: str = startUrl
        self.n: int = n
        self.extractor: ContentExtractor = ContentExtractor()
        self.stemmer: StopStem = StopStem()
        self.indexer: Indexer = Indexer()
        self.link_queue: queue.Queue = queue.Queue()
        self.crawled_pages: list[str] = []
        self.child_pages: dict = {}
        self.con = None # for SQLite
        self.cur = None # for SQLite

        ssl._create_default_https_context = ssl._create_unverified_context
        self.prepareSQL()


    def prepareSQL(self) -> None:
        self.con = sqlite3.connect('project.db')
        self.cur = self.con.cursor()

        # create table 'url2id' when necessary
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='url2id'").fetchone() is None:
            self.cur.execute("CREATE TABLE url2id(url, urlId)")

        # create table 'id2url' when necessary
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='id2url'").fetchone() is None:
            self.cur.execute("CREATE TABLE id2url(urlId, url)")

        # create table 'ModDate' when necessary
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='ModDate'").fetchone() is None:
            self.cur.execute("CREATE TABLE ModDate(urlId, modDate)")

        # create table 'Title' when necessary
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='Title'").fetchone() is None:
            self.cur.execute("CREATE TABLE Title(urlId, title)")

        # create table 'PageSize' when necessary
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='PageSize'").fetchone() is None:
            self.cur.execute("CREATE TABLE PageSize(urlId, size)")

        # create table 'CrawledPage' when necessary
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='CrawledPage'").fetchone() is None:
            self.cur.execute("CREATE TABLE CrawledPage(url, urlId)")

        # create table 'ParentUrl' when necessary
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='ParentUrl'").fetchone() is None:
            self.cur.execute("CREATE TABLE ParentUrl(urlId, value)") # value is in the form "parentURLId1 parentURLId2 ..."

        # create table 'ChildUrl' when necessary
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='ChildUrl'").fetchone() is None:
            self.cur.execute("CREATE TABLE ChildUrl(urlId, value)") # value is in the form "childtURLId1 childtURLId2 ..."

        self.con.commit()


    def crawlPages(self) -> None:
        print("Start Crawling...")
        # initialize the queue
        self.link_queue.put(self.startUrl)

        # start BFS page crawling
        while (not self.link_queue.empty()) and len(self.crawled_pages) < self.n:
            url = self.link_queue.get()

            # create ID for the url
            urlId: str = str(uuid.uuid4().int)
            # add to conversion tables url <=> urlId
            self.cur.execute("INSERT INTO url2id VALUES(?, ?)", (url, urlId))
            self.cur.execute(f"INSERT INTO id2url VALUES(?, ?)", (urlId, url))
            self.con.commit()


            # crawl the page
            childLinks: list[str] = self.crawlSinglePage(url, urlId)

            # add uncrawled child pages to link_queue
            for childUrl in childLinks:
                if childUrl not in self.crawled_pages:
                    self.link_queue.put(childUrl)
                    self.child_pages[urlId] = self.child_pages.get(urlId, []) + [childUrl]

            # add url to crawled_page
            self.crawled_pages.append(url)
            # add the page to the CrawledPage database
            self.cur.execute(f"INSERT INTO CrawledPage VALUES(?, ?)", (url, urlId))
            self.con.commit()

        # end of BFS
        # first provide ID to pages that are inside link_queue but not being crawled
        while not self.link_queue.empty():
            url = self.link_queue.get()

            # create ID for the url
            urlId: str = str(uuid.uuid4().int)
            # add to conversion tables url <=> urlId
            self.cur.execute("INSERT INTO url2id VALUES(?, ?)", (url, urlId))
            self.cur.execute(f"INSERT INTO id2url VALUES(?, ?)", (urlId, url))
            self.con.commit()

        # then update childURL database
        for parentUrlId, childUrls in self.child_pages.items():
            childUrlIds: list[str] = []
            for childUrl in childUrls:
                childUrlIds.append(self.cur.execute("SELECT urlId FROM url2id WHERE url=?", (childUrl,)).fetchone()[0])
            value = ' '.join(childUrlIds)
            self.cur.execute("INSERT INTO ChildUrl VALUES(?, ?)", (parentUrlId, value))
            self.con.commit()


        print("\nDone!")

        # return self.crawled_pages

    def crawlSinglePage(self, url: str, urlId: str) -> list[str]:
        print("\r", end="")
        print(f"Progress: {len(self.crawled_pages) + 1}/{self.n}", end="")
        # read html page
        page_data = requests.post(url).text
        # print(page_data)

        # get title
        title: str = self.extractor.getTitle(page_data)
        processedTitle: list[str] = self.stemmer.process(self.extractor.splitWords(title))
        # save title to database
        self.cur.execute(f"INSERT INTO Title VALUES(?, ?)", (urlId, title))
        self.con.commit()
        # save title to ForwardIndex
        self.indexer.addNewKeyword(processedTitle)
        self.indexer.forwardIndex(processedTitle, urlId)
        # save title to TitleInvertedIndex
        self.indexer.titleInvertedIndex(processedTitle, urlId)

        # get body text
        bodyText: str = self.extractor.getBodyText(page_data)
        processedBodyText: list[str] = self.stemmer.process(self.extractor.splitWords(bodyText))
        # save body text to ForwardIndex
        self.indexer.addNewKeyword(processedBodyText)
        self.indexer.forwardIndex(processedBodyText, urlId)
        # save body text to BodyInvertedIndex
        self.indexer.bodyInvertedIndex(processedBodyText, urlId)

        # get last modification date
        modDate: str = self.extractor.getLastModDate(url)
        # save last modification date to database
        self.cur.execute(f"INSERT INTO ModDate VALUES(?, ?)", (urlId, modDate))
        self.con.commit()

        # get page size
        pageSize: str = str(self.extractor.getPagesize(url, bodyText))
        # save page size to database
        self.cur.execute(f"INSERT INTO PageSize VALUES(?, ?)", (urlId, pageSize))
        self.con.commit()

        # get child links
        childLinks: list[str] = self.extractor.getLinks(url, page_data)
        return childLinks


if __name__ == '__main__':
    spider = Spider("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm", 30)
    pages = spider.crawlPages()
    # print("Num of pages:", len(pages))
    # print(pages)

    # links = queue.Queue()
    # i = 0
    # while (i < 1000):
    #     links.put(i)
    #     i += 1
    #     print(links.qsize())
    #
    # while(not links.empty()):
    #     print(links.get(), links.qsize())
    # links.put('hello')
    # links.put('world')
    # print(links.get())
    # links.put('!')
    # print(links.empty(), links.get())
    # print(links.empty(), links.get())
    # print(links.empty())
    # print(links.get() is None)

#    [test, page, thi, test, page, crawler, befor, get, admis, cse, depart, hkust, you, read, intern, new, book, here,
#     movi, list, new]
