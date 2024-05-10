import sqlite3
import time
if __name__ == '__main__':
    con = sqlite3.connect('project.db')
    cur = con.cursor()
    # result = cur.execute(f"SELECT * FROM ModDate").fetchall()
    # print(result)
    print(cur.execute("SELECT * FROM ModDate").fetchall())
    print(cur.execute("SELECT * FROM url2id").fetchall())

    # start = time.perf_counter()
    # for _ in range(1000):
    #     cur.execute("INSERT INTO test VALUES(?, ?)", ('0', '0'))
    # end = time.perf_counter()
    # print(f'{end - start}s')
