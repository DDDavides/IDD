import scrapy
import requests
from scrapy_playwright.page import PageMethod
from hw5.items import NasdaqItem
# import logging

# logging.basicConfig(filename='test.log', encoding='utf-8', level=print)

class NasdaqSpider(scrapy.Spider):
    name = 'nasdaq'
    base_url = 'https://www.nasdaq.com'

    def start_requests(self):
        url = 'https://www.nasdaq.com/market-activity/stocks/screener'
        yield scrapy.Request(url, meta=dict(
            playwright = True,
            # To use PageMethod (for clicking on page elements)
            # and access the Playwright Page object and define 
            # any callbacks as a coroutine function in order to
            # await the provided Page object 
            # [Vedi https://scrapeops.io/python-scrapy-playbook/scrapy-playwright/#how-to-install-scrapy-playwright]
            playwright_include_page = True,
            # tell what to do on the start page
            playwright_page_methods =[
                    PageMethod('wait_for_timeout', 3000),
                ],
            # when using prev setting, recommanded to use following setting
            # to make sure pages are closed even if a request fails
            errback = self.errback,
        ))

    async def parse(self, response):
        all_companies = []
        page = response.meta["playwright_page"]
        print("Current page:", page)
        
        tot_companies = 1000
        # Vedere quanti tr ci sono e parametrizzare questa variabile (prima bisogna accetare i cookie)
        company_per_page = 25
        first_page = True
        for _ in range(tot_companies // company_per_page):
            # Se accedo al sito la prima volta, allora accetto i cookie
            if first_page:
                # Salvo uno screenshot di debug prima del click
                # await page.screenshot(path="nasdaq.png", full_page=True)
                # print("Trying to accept cookies")
                # cerco il bottone "I Accept" e clicco per far scomparire il pop up
                await page.locator("xpath=//button[@id='onetrust-accept-btn-handler']").click()
                # Salvo uno screenshot di debug quando ho cliccato
                # await page.screenshot(path="nasdaq_after_decline.png", full_page=True)
                first_page = False
        
            content = await page.content()
            s = scrapy.Selector(text=content)
            all_companies.extend(s.xpath("//tbody[@class='nasdaq-screener__table-body']/tr/th/a/@href"))
            # print("Extended companies [ len =", len(all_companies), "]")
            await page.locator("xpath=//button[contains(@class, 'pagination__page--active')]/following-sibling::button[1]").click()
            # await page.evaluate("document.evaluate(\"//button[contains(@class, 'pagination__page--active')]/following-sibling::button[1]\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.click()")
            # print("Finished evaulation")

            # loc = ("xpath=//button[contains(@class, 'pagination__page--active')]/following-sibling::button[1]")
            # await page.wait_for_selector("//button[contains(@class, 'pagination__page--active')]/following-sibling::button[1]")
            # await page.wait_for_timeout(500)
            # await page.screenshot(path="nasdaq_last.png", full_page=True)
            # await page.wait_for_load_state('domcontentloaded')

        await page.close()
        
        # print(len(all_companies), "companies got")
        # i = 1
        for company in all_companies:
            # print("[", i, "] managing request for", company.get())
            # i += 1
            yield scrapy.Request(self.base_url+company.get(), callback=self.parse_company)
        

    async def errback(self, failure):
        print("FAIL")
        page = failure.request.meta["playwright_page"]
        await page.close()

    def parse_company(self, response):
        if response.status != requests.codes.ok:
            # print("Error in link", response.url)
            return
        
        name = response.xpath("//*[@class='symbol-page-header__name']/text()").get()
        # print("Company name:", name)
        if name == None:
            print("Company name empty ", response.url)
        
        nsqItm = NasdaqItem()
        nsqItm['name'] = name
        yield nsqItm