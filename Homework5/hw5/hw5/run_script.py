import scrapy
from scrapy.crawler import CrawlerProcess
from scrapy.utils.project import get_project_settings

process = CrawlerProcess(get_project_settings())

process.crawl('cbinsight')
process.crawl('companiesmarketcap')
# process.crawl('financial')
process.crawl('technopark')
process.crawl('teamblind')
process.start() # the script will block here until the crawling is finished