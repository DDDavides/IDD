# Define here the models for your scraped items
#
# See documentation in:
# https://docs.scrapy.org/en/latest/topics/items.html

import scrapy

not_available = "None"

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

class NasdaqItem(scrapy.Item):
    name = scrapy.Field()
    analysisDate = scrapy.Field()
    exchange = scrapy.Field()
    sector = scrapy.Field()
    industry = scrapy.Field()
    oneYearTarget = scrapy.Field()
    shareVolume = scrapy.Field()
    averageVolume = scrapy.Field()
    marketCap = scrapy.Field()
    # Altro? guarda sito per decidere

class FinancialItem(scrapy.Item):
    name = scrapy.Field()
    country = scrapy.Field()
    sector = scrapy.Field()
    absolute_growth_rate_pct = scrapy.Field()
    compound_annual_growth_rate_cagr_pct = scrapy.Field()
    revenue_2020_euro = scrapy.Field()
    revenue_2017_euro = scrapy.Field()
    number_of_employees_2020 = scrapy.Field()
    number_of_employees_2017 = scrapy.Field()
    founding_year = scrapy.Field()

class BlindItem(scrapy.Item):
    name = scrapy.Field()
    website = scrapy.Field()
    locations = scrapy.Field()
    size = scrapy.Field()
    industry = scrapy.Field()
    founded = scrapy.Field()