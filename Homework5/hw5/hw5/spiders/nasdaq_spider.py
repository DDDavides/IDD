import scrapy
import requests

class NasdaqSpider(scrapy.Spider):
    name = 'nasdaq'
    allowed_domains = ['www.nasdaq.com']
    start_urls = ['https://www.nasdaq.com/market-activity/stocks/screener']

    def parse(self, response):
        print("Parsing on response", response)
        all_companies = response.xpath("//th/a[@href]/@href")
        print(all_companies)
        for company in all_companies:
            print(company.get())
            yield scrapy.Request(company.get(), callback=self.parse_company)

    def parse_company(self, response):
        if response.status != requests.codes.ok:
            return
        name = response.xpath("//*[@class='symbol-page-header__name']/text()")
        if name == None:
            print(response.url)
        yield {
            'company_name': name
        }