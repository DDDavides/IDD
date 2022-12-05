import time
import os
spiders = ["financial", "companiesmarketcap", "cbinsight", "teamblind"]

rtimes=[]
for spider in spiders:
  print("evaluating $spider crawling time:")

  start = time.time()
  os.system(f"scrapy crawl {spider} -s LOG_ENABLED=0")
  rtime = time.time() - start
  print(f"tot time: {rtime}s")
  rtimes.append(rtime)

s=sum(rtimes)

mean = s / len(rtimes)
print("summary:")
print("tot time:  ${sum}s")
print("mean time: ${mean}s")
