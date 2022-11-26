import scrapy
from scrapy_playwright.page import PageMethod

class TechnoparkSpider(scrapy.Spider):
    name = 'technopark'
    allowed_domains = ['www.technopark.org']
    start_urls = [
        'https://www.technopark.org/company-a-z-listing'
    ]
    base_url = 'https://www.technopark.org'

    def start_requests(self):
        meta = dict(
            playwright = True,
            playwright_page_methods = [
                PageMethod('evaluate', "window.scrollBy(0, document.body.scrollHeight)"),
                PageMethod("wait_for_selector", "div.cmpny-detail"),
            ],
            playwright_include_page = True,
            errback=self.errback
        )
        yield scrapy.Request(self.start_urls[0], meta=meta)


    #Rules
    async def parse(self, response):
        page = response.meta['playwright_page']
        nEntry = 4
        maxEntry = 1000
        for i in range(1, maxEntry // nEntry):
            x = i * nEntry
            await page.evaluate("window.scrollBy(0, document.body.scrollHeight)")
            await page.wait_for_selector("div.cmpny-detail:nth-child({})".format(x))
        
        html = await page.content()
        await page.close()
        s = scrapy.Selector(text=html)

        all_companies = s.xpath("//div[contains(@class,'cmpny-detail')]//a[@href][1]/@href")
        for company in all_companies:
            print(company)
            yield scrapy.Request(self.base_url + company.get(), callback=self.parse_company)
        
    def parse_company(self, response):
        if response.status != 200:
            yield
        # print(response.url)

    async def errback(self, failure):
        page = failure.request.meta["playwright_page"]
        await page.close()