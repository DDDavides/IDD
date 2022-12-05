import time
import os
spiders = ["financial", "companiesmarketcap", "cbinsight", "teamblind"]

rtimes=[]
for spider in spiders:
  print(f"evaluating {spider} crawling time:")

  start = time.time()
  os.system(f"scrapy crawl {spider} -s LOG_ENABLED=0")
  rtime = time.time() - start
  print(f"tot time: {rtime}s")
  rtimes.append(rtime)

s=sum(rtimes)
mean = s / len(rtimes)
print("summary:")
print(f"tot time:  {s}s")
print(f"mean time: {mean}s")
