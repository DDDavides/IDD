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

class TechnoparkCompanyItem(scrapy.Item):
    location = scrapy.Field()
    name = scrapy.Field()
    address = scrapy.Field()
    pin = scrapy.Field()
    phone = scrapy.Field()
    email = scrapy.Field()
    site = scrapy.Field()
    ceo_and_mananging_director = scrapy.Field()

class CbinsightItem(scrapy.Item):
    name = scrapy.Field()
    valuation = scrapy.Field()
    dateJoined = scrapy.Field()
    country = scrapy.Field()
    city = scrapy.Field()
    industry = scrapy.Field()
    investors = scrapy.Field()
    founded = scrapy.Field()
    stage = scrapy.Field()
    totalRaised = scrapy.Field()

class BlindItem(scrapy.Item):
    name = scrapy.Field()
    website = scrapy.Field()
    location = scrapy.Field()
    size = scrapy.Field()
    industry = scrapy.Field()
    founded = scrapy.Field()
    salary = scrapy.Field()
