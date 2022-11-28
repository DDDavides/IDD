import scrapy
import requests
from scrapy_playwright.page import PageMethod
from bs4 import BeautifulSoup
from hw5.items import TechnoparkCompanyItem

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
        
        for company in all_companies:
            yield scrapy.Request(self.base_url + company.get(), callback=self.parse_company)
    

    def parse_company(self, response):
        if response.status != requests.codes.ok:
            return
        soup = BeautifulSoup(response.text, 'lxml')
        company_info = [li.text.split("\n")[2].strip() for li in soup.find("ul", class_="list-sx").find_all("li")]

        company = TechnoparkCompanyItem()
        company['location'] = company_info[0]
        company['name'] = company_info[1]
        company['address'] = company_info[2]
        company['pin'] = company_info[3]
        company['phone'] = company_info[4]
        company['email'] = company_info[5] if len(company_info) >= 6 else None
        company['site'] = company_info[6] if len(company_info) >= 7 else None
        
        yield company




    async def errback(self, failure):
        page = failure.request.meta["playwright_page"]
        await page.close()