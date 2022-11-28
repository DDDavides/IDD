# Define here the models for your scraped items
#
# See documentation in:
# https://docs.scrapy.org/en/latest/topics/items.html

import scrapy

class CompaniesmarketcapCompanyItem(scrapy.Item):
    name = scrapy.Field()
    rank = scrapy.Field()
    marketcap = scrapy.Field()
    country = scrapy.Field()
    share_price = scrapy.Field()
    change1d = scrapy.Field()
    change1y = scrapy.Field()
    categories = scrapy.Field()
