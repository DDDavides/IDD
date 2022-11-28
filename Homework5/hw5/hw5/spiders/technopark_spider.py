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
                PageMethod("wait_for_selector", "div.cmpny-detail"),
            ],
            playwright_include_page = True,
            errback=self.errback
        )
        yield scrapy.Request(self.start_urls[0], meta=meta)


    #Rules
    async def parse(self, response):
        page = response.meta['playwright_page']
        loadedEntry = 1
        maxEntry = 1000
        for _ in range(maxEntry // loadedEntry):
            await page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
            await page.wait_for_selector("div.cmpny-detail")
        
        html = await page.content()
        await page.close()
        s = scrapy.Selector(text=html)

        all_companies = s.xpath("//div[contains(@class,'cmpny-detail')]//a[@href][1]/@href")
        
        print(len(all_companies))
        for company in all_companies:
            yield scrapy.Request(self.base_url + company.get(), callback=self.parse_company)
    
    # TODO: Estrarre i dati dalle varie pagine
    def parse_company(self, response):
        print(response.url)

    async def errback(self, failure):
        page = failure.request.meta["playwright_page"]
        await page.close()