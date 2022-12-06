import pandas as pd
import time, yaml, os

spiders = ["financial", "companiesmarketcap", "cbinsight", "teamblind"]
output_file = "../performance.txt"
num_instances = [100, 200, 400, 800, 1000]

with open('../config.yaml') as f:
  config = yaml.load(f, Loader=yaml.FullLoader)

with open(output_file,'w') as out:
  out.write("******************************************\n")
  # print("******************************************")
  for x in num_instances:
    config['ntopick'] = x
    with open('../config.yaml', "w") as f:
      yaml.dump(config, stream=f, default_flow_style=False, sort_keys=False)
    
    # print(f"max companies number: {x}")
    out.write(f"max companies number: {x}\n")
    times = []
    loss = []
    for spider in spiders:
      # print(f"evaluating {spider} crawling time:")
      out.write(f"evaluating {spider} crawling time:\n")

      start = time.time()
      os.system(f"scrapy crawl {spider} -s LOG_ENABLED=0")
      
      times.append(time.time() - start)
      loss.append(x - len(pd.read_csv(f"./dataset/{spider}.csv").index))

      # print(f"run time: {times[-1]}s")
      out.write(f"run time: {times[-1]}s\n")
      # print(f"data loss: {loss[-1]}")
      out.write(f"data loss: {loss[-1]}\n")
      # print(f"loss (perncentage):  {loss[-1] / x}")
      out.write(f"loss (perncentage):  {loss[-1] / x}\n")
    
    tot_t = sum(times)
    mean = tot_t / len(times)
    
    tot_l = sum(loss)
    tot_lp = tot_l / (x * len(spiders))

    out.write("\nsummary:\n")
    # print("\nsummary:")
    out.write(f"\ttotal full time:  {tot_t}s\n")
    # print(f"\ttotal full time:  {tot_t}s")
    out.write(f"\tmean time: {mean}s\n")
    # print(f"\tmean time: {mean}s")
    out.write(f"\ttotal loss:  {tot_l}\n")
    # print(f"\ttotal loss:  {tot_l}")
    out.write(f"\ttotal loss (perncentage):  {tot_lp}\n")
    # print(f"\ttotal loss (perncentage):  {tot_lp}")
    out.write("******************************************\n")
    # print("******************************************")
