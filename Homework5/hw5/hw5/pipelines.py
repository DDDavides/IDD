# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://docs.scrapy.org/en/latest/topics/item-pipeline.html


# useful for handling different item types with a single interface
from itemadapter import ItemAdapter
import pandas as pd
import os

class HW5Pipeline:

    data = pd.DataFrame()

    def process_item(self, item, spider):
        self.data = pd.concat([self.data, pd.DataFrame(ItemAdapter(item).asdict(), index=[0], dtype=str)], sort=False, ignore_index=True)
        return item
    
    def close_spider(self, spider):
        if not os.path.exists("./dataset"):
            os.mkdir("./dataset")
            
        self.data.to_csv(f"./dataset/{spider.name}.csv")