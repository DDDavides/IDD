import scrapy
# from twisted.internet import reactor, defer
from scrapy.crawler import CrawlerProcess
from scrapy.crawler import CrawlerRunner
from scrapy.utils.log import configure_logging
from scrapy.utils.project import get_project_settings

#Prova 1
process = CrawlerProcess(get_project_settings())

process.crawl('cbinsight')
process.crawl('companiesmarketcap')
process.crawl('financial')
#process.crawl('technopark')
process.crawl('teamblind')
process.start() # the script will block here until the crawling is finished
