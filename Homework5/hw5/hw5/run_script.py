import scrapy
from scrapy.crawler import CrawlerProcess
from scrapy.utils.project import get_project_settings

process = CrawlerProcess(get_project_settings())
process


process.crawl('cbinsight')
process.crawl('financial')
process.crawl('technopark_spider')
process.crawl('companiesmarketcap')
process.start() # the script will block here until the crawling is finished