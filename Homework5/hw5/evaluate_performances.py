import time, yaml, os
import pandas as pd

spiders = ["financial", "companiesmarketcap", "cbinsight", "teamblind"]

with open('../config.yaml') as f:
  config = yaml.load(f, Loader=yaml.FullLoader)

print("******************************************")
for x in [100, 200, 400, 800, 1000]:
  config['ntopick'] = x
  with open('../config.yaml', "w") as f:
    yaml.dump(config, stream=f, default_flow_style=False, sort_keys=False)
  
  print(f"max companies number: {x}")
  times = []
  loss = []
  for spider in spiders:
    print(f"evaluating {spider} crawling time:")

    start = time.time()
    os.system(f"scrapy crawl {spider} -s LOG_ENABLED=0")
    
    times.append(time.time() - start)
    loss.append(x - len(pd.read_csv(f"./dataset/{spider}.csv").index))

    print(f"run time: {times[-1]}s")
    print(f"data loss: {loss[-1]}")
    print(f"loss (percentage):  {loss[-1] / x}")
  
  tot_t = sum(times)
  mean = tot_t / len(times)
  
  tot_l = sum(loss)
  tot_lp = tot_l / (x * len(spiders))

  print("\nsummary:")
  print(f"\ttotal full time:  {tot_t}s")
  print(f"\tmean time: {mean}s")
  print(f"\ttotal loss:  {tot_l}")
  print(f"\ttotal loss (percentage):  {tot_lp}")
  print("******************************************")
