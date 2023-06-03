import pandas as pd
import time, yaml, os

spiders = ["financial", "companiesmarketcap", "cbinsight", "teamblind"]
config_file = "../config.yaml"
out_path = "./csv_stats/performances"
num_instances = [100000]

with open(config_file) as f:
  config = yaml.load(f, Loader=yaml.FullLoader)

to_eval = ["run_time", "data_loss", "data_loss_(pct)"]
performances = { spider: {x: {field: 0 for field in to_eval} for x in num_instances}  for spider in spiders}
for x in num_instances:

  config['ntopick'] = x
  with open(config_file, "w") as f:
    yaml.dump(config, stream=f, default_flow_style=False, sort_keys=False)
  
  for spider in spiders:
    start = time.time()
    print("Eseguendo:", spider)
    os.system(f"scrapy crawl {spider} -s LOG_ENABLED=0")

    run_time = time.time() - start
    data_loss = x - len(pd.read_csv(f"./dataset/{spider}.csv").index)
    data_loss_pct = data_loss / x

    performances[spider][x]["run_time"] = run_time
    performances[spider][x]["data_loss"] = data_loss
    performances[spider][x]["data_loss_pct"] = data_loss_pct


if not os.path.exists(out_path):
  os.mkdir(out_path)

for spider in performances:
  df = pd.DataFrame()
  
  for x in performances[spider]:
    tmp = pd.DataFrame(performances[spider][x], index=[x])
    df = pd.concat([df,  tmp])

  df.to_csv(f"./csv_stats/performances/{spider}.csv")