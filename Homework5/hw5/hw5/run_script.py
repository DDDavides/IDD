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
# process.crawl('technopark')
process.crawl('teamblind')
process.start() # the script will block here until the crawling is finished

#Prova 2
# def start_sequentially(process: CrawlerProcess, crawlers: list):
#     print('start crawler {}'.format(crawlers[0].__name__))
#     deferred = process.crawl(crawlers[0])
#     if len(crawlers) > 1:
#         deferred.addCallback(lambda _: start_sequentially(process, crawlers[1:]))

# def main():
#     crawlers = ['cbinsight', 'companiesmarketcap','financial']
#     process = CrawlerProcess(settings=get_project_settings())
#     start_sequentially(process, crawlers)
#     process.start()

#Prova 3
# settings = get_project_settings()
# configure_logging(settings)
# runner = CrawlerRunner(settings)

# @defer.inlineCallbacks
# def crawl():
#     yield runner.crawl('cbinsight')
#     yield runner.crawl('companiesmarketcap')
#     yield runner.crawl('financial')
#     yield runner.crawl('technopark')
#     yield runner.crawl('teamblind')
#     reactor.stop()

# crawl()
# reactor.run()