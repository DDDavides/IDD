import scrapy
from bs4 import BeautifulSoup

class NasdaqSpider(scrapy.Spider):
    name = 'wsj'
    allowed_domain = 'www.wsj.com'
    start_urls = ['https://www.wsj.com/market-data/quotes/company-list/sector/software']

    async def parse(self, response):
        print("Accessing to site")
        all_companies = []
        page = response.meta["playwright_page"]
        print("Got page")
        bs = BeautifulSoup(page, 'lxml')
        for row in bs.find_all('tr'):
            print(row)
            # all_companies.extend(row.td.a.href)
            # //tr/td[1]/a/@href
        
        # for company in all_companies:
        #     yield scrapy.Request(company, callback=self.parse_company)
        self.parse_company
        

    async def errback(self, failure):
        print("FAIL")
        page = failure.request.meta["playwright_page"]
        await page.close()

    async def parse_company(self, response):
        # if response.status != requests.codes.ok:
        #     return
        # name = response.xpath("//*[@class='symbol-page-header__name']/text()")
        # if name == None:
        #     print(response.url)
        # yield {
        #     'company_name': name
        # }
        print(response.url)