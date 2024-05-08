import sqlite3
import time
if __name__ == '__main__':
    con = sqlite3.connect('project.db')
    cur = con.cursor()
    # result = cur.execute(f"SELECT * FROM ModDate").fetchall()
    # print(result)
    print(cur.execute("SELECT name FROM sqlite_master").fetchall())
    for curUrl, childUrls in cur.execute("SELECT * FROM ChildUrl").fetchall():
        print("Parent Page:")
        print(cur.execute("SELECT url FROM id2url WHERE urlId=?", (curUrl,)).fetchone())


        print("Child pages:", len(childUrls.split(' ')))
        for childUrl in childUrls.split(' '):
            print("ID:", childUrl)
            print("link:", cur.execute("SELECT url FROM id2url WHERE urlId=?", (childUrl,)).fetchone())
            print()

    # start = time.perf_counter()
    # for _ in range(1000):
    #     cur.execute("INSERT INTO test VALUES(?, ?)", ('0', '0'))
    # end = time.perf_counter()
    # print(f'{end - start}s')
