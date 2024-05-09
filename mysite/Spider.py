import ssl
import sqlite3
import uuid
import queue
import requests
import asyncio
import time
import warnings

from ContentExtractor import ContentExtractor
from StopStem import StopStem
from Indexer import Indexer

warnings.filterwarnings("ignore")


class Spider:
    def __init__(self, startUrl: str, n: int):
        self.startUrl: str = startUrl
        self.n: int = n
        self.extractor: ContentExtractor = ContentExtractor()
        self.stemmer: StopStem = StopStem()
        self.indexer: Indexer = Indexer()
        self.link_queue: list[str] = []
        self.crawled_pages: list[(str, str)] = []
        self.id2url: dict = {}
        self.url2id: dict = {}
        self.child_pages: dict = {}
        self.parent_pages: dict = {}
        self.Title: dict = {}
        self.PageSize: dict = {}
        self.ModDate: dict = {}
        self.con = None # for SQLite
        self.cur = None # for SQLite

        ssl._create_default_https_context = ssl._create_unverified_context
        self.prepareSQL()


    def prepareSQL(self) -> None:
        self.con = sqlite3.connect('project.db')
        self.cur = self.con.cursor()

        # create table 'url2id' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='url2id'").fetchone() is None:
            self.cur.execute("CREATE TABLE url2id(url, urlId)")
        else: # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM url2id").fetchall()
            self.url2id = {i[0]: i[1] for i in result}


        # create table 'id2url' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='id2url'").fetchone() is None:
            self.cur.execute("CREATE TABLE id2url(urlId, url)")
        else: # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM id2url").fetchall()
            self.id2url = {i[0]: i[1] for i in result}

        # create table 'ModDate' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='ModDate'").fetchone() is None:
            self.cur.execute("CREATE TABLE ModDate(urlId, modDate)")
        else: # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM ModDate").fetchall()
            self.ModDate = {i[0]: i[1] for i in result}

        # create table 'Title' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='Title'").fetchone() is None:
            self.cur.execute("CREATE TABLE Title(urlId, title)")
        else: # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM Title").fetchall()
            self.Title = {i[0]: i[1] for i in result}

        # create table 'PageSize' if not exist, extract previous data if already exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='PageSize'").fetchone() is None:
            self.cur.execute("CREATE TABLE PageSize(urlId, size)")
        else: # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM PageSize").fetchall()
            self.PageSize = {i[0]: i[1] for i in result}

        # create table 'CrawledPage' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='CrawledPage'").fetchone() is None:
            self.cur.execute("CREATE TABLE CrawledPage(url, urlId)")

        # create table 'ParentUrl' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='ParentUrl'").fetchone() is None:
            self.cur.execute("CREATE TABLE ParentUrl(urlId, value)") # value is in the form "parentURLId1 parentURLId2 ..."
        else:  # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM ParentUrl").fetchall()
            self.parent_pages = {i[0]: i[1].split(' ') for i in result}

        # create table 'ChildUrl' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='ChildUrl'").fetchone() is None:
            self.cur.execute("CREATE TABLE ChildUrl(urlId, value)") # value is in the form "childtURLId1 childtURLId2 ..."
        else: # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM ChildUrl").fetchall()
            self.child_pages = {i[0]: i[1].split(' ') for i in result}

        self.con.commit()

    def updateDatabase(self): # store all the data into database
        # update id2url database
        # clear the id2url table
        self.cur.execute("DROP TABLE id2url")
        self.cur.execute("CREATE TABLE id2url(urlId, url)")
        # update the entries inside the table
        self.cur.executemany("INSERT INTO id2url VALUES(?, ?)", list(self.id2url.items()))
        self.con.commit()

        # update url2id database
        # clear the url2id table
        self.cur.execute("DROP TABLE url2id")
        self.cur.execute("CREATE TABLE url2id(url, urlId)")
        # update the entries inside the table
        self.cur.executemany("INSERT INTO url2id VALUES(?, ?)", list(self.url2id.items()))
        self.con.commit()

        # update Title database
        # clear the Title table
        self.cur.execute("DROP TABLE Title")
        self.cur.execute("CREATE TABLE Title(urlId, title)")
        # update the entries inside the table
        self.cur.executemany("INSERT INTO Title VALUES(?, ?)", list(self.Title.items()))
        self.con.commit()

        # update ModDate database
        # clear the ModDate table
        self.cur.execute("DROP TABLE ModDate")
        self.cur.execute("CREATE TABLE ModDate(urlId, modDate)")
        # update the entries inside the table
        self.cur.executemany("INSERT INTO ModDate VALUES(?, ?)", list(self.ModDate.items()))
        self.con.commit()

        # update PageSize database
        # clear the PageSize table
        self.cur.execute("DROP TABLE PageSize")
        self.cur.execute("CREATE TABLE PageSize(urlId, size)")
        # update the entries inside the table
        self.cur.executemany("INSERT INTO PageSize VALUES(?, ?)", list(self.PageSize.items()))
        self.con.commit()

        # update CrawledPage database
        # clear the CrawledPage table
        self.cur.execute("DROP TABLE CrawledPage")
        self.cur.execute("CREATE TABLE CrawledPage(url, urlId)")
        # update the entries inside the table
        self.cur.executemany("INSERT INTO CrawledPage VALUES(?, ?)", self.crawled_pages)
        self.con.commit()

        # update ParentUrl database
        # clear the ParentUrl table
        self.cur.execute("DROP TABLE ParentUrl")
        self.cur.execute("CREATE TABLE ParentUrl(urlId, value)")
        # construct the value into the specified format
        key_values = ((key, ' '.join(value)) for key, value in self.parent_pages.items())
        # update the entries inside the table
        self.cur.executemany("INSERT INTO ParentUrl VALUES(?, ?)", key_values)
        self.con.commit()

        # update ChildUrl database
        # clear the ChildUrl table
        self.cur.execute("DROP TABLE ChildUrl")
        self.cur.execute("CREATE TABLE ChildUrl(urlId, value)")
        # construct the value into the specified format
        key_values = ((key, ' '.join(value)) for key, value in self.child_pages.items())
        # update the entries inside the table
        self.cur.executemany("INSERT INTO ChildUrl VALUES(?, ?)", key_values)
        self.con.commit()


        # update the databases used in the indexer
        self.indexer.updateDatabase()


    def doCrawl(self, modDate: str, urlId: str) -> bool:
        # get the stored last modification date of the page
        storedModDate: str = self.ModDate.get(urlId, None)

        # compare the mod date, return True if modDate is later than storedModDate
        # the idea here is that, a modification on a page cannot be revert (I think, not 100% sure),
        # therefore if the value of modDate != storedModDate, that means the page must have been changed
        return storedModDate is None or modDate != storedModDate

    async def crawlSinglePage(self, url: str, urlId: str) -> list[str]:
        '''
        crawl a page, and return the child links if the page should be crawled
        '''

        # get the actual modification date from the webpage
        res = await asyncio.to_thread(requests.get, url, verify=False)
        head = res.headers
        modDate: str = self.extractor.getLastModDate(head)

        if not self.doCrawl(modDate, urlId): # we do not need to crawl this page
            return None

        ### start crawling the page ###

        # get page data
        page_data: str = res.text

        # get title
        title: str = self.extractor.getTitle(page_data)
        processedTitle: list[str] = self.stemmer.process(self.extractor.splitWords(title))
        # save title
        self.Title[urlId] = title

        # save title to ForwardIndex
        self.indexer.addNewKeyword(processedTitle)
        self.indexer.forwardIndex(processedTitle, urlId, clearOldContent=True)
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

        # save last modification
        self.ModDate[urlId] = modDate

        # get page size
        pageSize: str = str(self.extractor.getPagesize(head, bodyText))
        # save page size
        self.PageSize[urlId] = pageSize

        # get child links
        childLinks: list[str] = self.extractor.getLinks(url, page_data)

        return childLinks

    def addLinkRelation(self, curUrl, childUrl, ids, paths):
        if childUrl not in ids: # childUrl is not in the queue to be fetched yet
            return True

        if paths[curUrl] & ids[childUrl] > 0:  # cycle detected
            return False

        # no cycle detected, update childUrl's path and return True
        paths[childUrl] = paths[childUrl] | ids[curUrl] | paths[curUrl]
        return True


    async def crawlPages(self) -> None:
        print("Start Crawling...")
        tasks = []
        temp = []

        cycleDetectionId = 1  # for the use of cycle detection
        cycle_ids = {}  # for the use of cycle detection
        cycle_paths = {}  # for the user of cycle detection

        ### initialize the crawling asyncio algorithm ###
        curUrlId: str = self.url2id.get(self.startUrl, None)

        if curUrlId is None: # the page is a new page
            curUrlId = str(uuid.uuid4().int)
            self.url2id[self.startUrl] = curUrlId
            self.id2url[curUrlId] = self.startUrl

        task = asyncio.create_task(self.crawlSinglePage(self.startUrl, curUrlId))
        tasks.append((self.startUrl, task))
        self.link_queue.append(self.startUrl)
        # for cycle detection
        cycle_ids[self.startUrl] = cycleDetectionId
        cycleDetectionId *= 2
        cycle_paths[self.startUrl] = 0


        while tasks != [] and len(self.crawled_pages) < self.n:
            temp = []
            for curUrl, task in tasks[:self.n - len(self.crawled_pages)]: # the slicing is to control the number of crawled pages
                curUrlId = self.url2id[curUrl]
                childLinks = await task

                if childLinks is None: # do not need to crawl this page
                    continue

                # first remove current page from the parent page list of all the old child pages
                oldChildLinks: list[str] = self.child_pages.get(curUrlId, [])
                for link in oldChildLinks:
                    childUrlId: str = self.url2id.get(link)
                    # remove the current page from the parent page list
                    newParentList = self.parent_pages[childUrlId]
                    newParentList.remove(curUrlId)
                    self.parent_pages[childUrlId] = newParentList

                # clear the child page list of the current page
                if oldChildLinks != []:
                    self.child_pages[curUrlId] = []


                # perform operations on each new child link
                for link in childLinks:
                    # get id of the page
                    childUrlId: str = self.url2id.get(link, None)

                    if childUrlId is None:  # the page is a new page
                        childUrlId = str(uuid.uuid4().int)
                        self.url2id[link] = childUrlId
                        self.id2url[childUrlId] = link


                    if link not in self.crawled_pages + self.link_queue: # the page is a new page
                        task = asyncio.create_task(self.crawlSinglePage(link, childUrlId))
                        temp.append((link, task))
                        self.link_queue.append(link)

                        cycle_ids[link] = cycleDetectionId
                        cycleDetectionId *= 2
                        cycle_paths[link] = cycle_paths[curUrl] | cycle_ids[curUrl] # update path for the child page

                    # check whether set current page as parent URL of the child page
                    if self.addLinkRelation(curUrl, link, cycle_ids, cycle_paths):
                        # print(childUrlId)
                        self.child_pages[curUrlId] = self.child_pages.get(curUrlId, []) + [childUrlId]
                        self.parent_pages[childUrlId] = self.parent_pages.get(childUrlId, []) + [curUrlId]

                    # print(self.child_pages[curUrlId])
                self.crawled_pages.append((curUrl, curUrlId))

                print("\r", end="")
                print(f"Progress: {len(self.crawled_pages)}/{self.n}", end="")

            tasks = temp


        ### finish crawling, perform some post-crawling task ###
        self.updateDatabase()
        print("\r", end="")
        print(f"Progress: {len(self.crawled_pages)}/{self.n}", end="")
        print("\nDone!")


if __name__ == '__main__':
    spider = Spider("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm", 300)
    start = time.perf_counter()
    asyncio.run(spider.crawlPages())
    end = time.perf_counter()
    print(f"Time spent: {end - start:.2f}s")