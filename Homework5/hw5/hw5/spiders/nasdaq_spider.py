import scrapy
import requests
from scrapy_playwright.page import PageMethod
from hw5.items import NasdaqItem
from bs4 import BeautifulSoup
import re
# from playwright.async_api import async_playwright

# for logging
import logging
from scrapy.utils.log import configure_logging 
    
class NasdaqSpider(scrapy.Spider):
    configure_logging(install_root_handler=False)
    logging.basicConfig(
        filename='log.txt',
        level=logging.DEBUG
    )
    name = 'nasdaq'
    allowed_domains = ['nasdaq.com']
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
                    # PageMethod('wait_for_load_state', 'domcontentloaded'),
                    PageMethod('wait_for_timeout', 2000),
                ],
            # when using prev setting, recommanded to use following setting
            # to make sure pages are closed even if a request fails
            errback = self.errback,
        ))

    async def parse(self, response):
        all_companies = []
        page = response.meta["playwright_page"]
        
        tot_companies = 25
        company_per_page = 25
        first_page = True
        iters = tot_companies // company_per_page
        for i in range(iters):
            # Se accedo al sito la prima volta, allora accetto i cookie
            if first_page:
                # Salvo uno screenshot di debug prima del click
                # await page.screenshot(path="nasdaq.png", full_page=True)
                # cerco il bottone "I Accept" e clicco per far scomparire il pop up
                logging.debug("STO CLICCANDO IL BOTTONE")
                await page.locator("xpath=//button[@id='onetrust-accept-btn-handler']").click()
                logging.debug("HO CLICCATO IL BOTTONE")
                # Salvo uno screenshot di debug quando ho cliccato
                # await page.screenshot(path="nasdaq_after_decline.png", full_page=True)
                first_page = False
        
            content = await page.content()
            s = scrapy.Selector(text=content)
            links = s.xpath("//tbody[@class='nasdaq-screener__table-body']/tr/th/a/@href")[:10]
            all_companies.extend(links)
            # all_companies.extend(s.xpath("(//tbody[@class='nasdaq-screener__table-body']/tr/th/a/@href)[1]"))
            if i != iters-1:
                await page.locator("xpath=//button[contains(@class, 'pagination__page--active')]/following-sibling::button[1]").click()
                await page.wait_for_load_state('domcontentloaded')
        await page.close()

        # with open("./link/sites.txt",'w') as f:
        #     for company in all_companies:
        #         f.write(f"{self.base_url+company.get()}\n")

        for company in all_companies:
            print("Company URL:", self.base_url+company.get())
            yield scrapy.Request(
                self.base_url+company.get(), 
                meta=dict(
                        playwright = True,
                        playwright_include_page = True,
                        playwright_page_methods =[
                            # PageMethod('wait_for_selector', "//*[@class='symbol-page-header__name']/text()"),
                            PageMethod('wait_for_timeout', 500),
                            PageMethod('evaluate', "window.scrollTo(0, document.body.scrollHeight/16)"),
                            PageMethod('wait_for_timeout', 500),
                            PageMethod('evaluate', "window.scrollTo(document.body.scrollHeight/16, document.body.scrollHeight/8)"),
                            PageMethod('wait_for_timeout', 500),
                            PageMethod('evaluate', "window.scrollTo(document.body.scrollHeight/16, document.body.scrollHeight/4)"),
                            # PageMethod('evaluate', "window.scrollTo(0, document.body.scrollHeight)"),
                            # PageMethod('evaluate', "window.scrollTo(0, document.body.scrollHeight)"),
                            # PageMethod('evaluate', "window.scrollTo(document.body.scrollHeight, document.body.scrollHeight/8)"),
                        ],
                        errback = self.errback,
                    ),
                callback = self.parse_company
                )
        

    def parse_company(self, response):
        print("CURRENT PAGE COMPANY:\n", response.url)

        soup = BeautifulSoup(response.text, 'lxml')
        if response.status != requests.codes.ok:
            print("\n\nError code in\n\n", response.url)
            return
        
        name = response.xpath("//*[@class='symbol-page-header__name']/text()").get()
        # print("Name:", name)
        # analysisDate = response.xpath("//*[contains(@class,'symbol-page-header__timestamp')]/span/text()").get()
        # print("Date:", analysisDate)
        
        # cerchiamo questi attributi, ottennuti con il seguente mapping dal sito
        # " ", "'s " -> "_"  
        # tutto il resto lo lasciamo
        # rendiamo tutto lower case
        attrs = {
            'exchange': None,
            'sector': None,
            'industry': None,
            '1_year_target': None,
            'today_high/low': None,
            'share_volume': None,
            'average_volume': None,
            'previous_close': None,
            '52_week_high/low': None,
            'market_cap': None,
            'p/e_ratio': None,
            'forward_p/e_1_yr.': None,
            'earnings_per_share(eps)': None,
            'annualized_dividend': None,
            'ex_dividend_date': None,
            'current_yield': None
        }
        rows = soup.find_all("tr", class_='summary-data__row')
        # print("All rows found:", rows)
        for row in rows:
            attr_name = re.sub(" ", "_", re.sub("'s ", " ", row.find('td').get_text().strip().lower()))
            # print("Searching", attr_name, "in dictionary...")
            if attr_name in attrs:
                # print("Found")
                attrs[attr_name] = row.find_all('td')[1].get_text()
                # print("Got value of", attr_name, ":", row.td[2].text)

        nsqItm = NasdaqItem()
        nsqItm['name'] = name
        # nsqItm['analysisDate'] = analysisDate  
        nsqItm['exchange'] = attrs['exchange']
        nsqItm['sector'] = attrs['sector']
        nsqItm['industry'] = attrs['industry']
        nsqItm['oneYearTarget'] = attrs['1_year_target']
        nsqItm['shareVolume'] = attrs['share_volume']
        nsqItm['averageVolume'] = attrs['average_volume']
        nsqItm['marketCap'] = attrs['market_cap']
        yield nsqItm

    # async def start_trace(self):
    #     async with async_playwright() as p:
    #         self.browser = await p.chromium.launch()
    #         self.context = await self.browser.new_context()
    #         await self.context.tracing.start(screenshots=True, snapshots=True, sources=True)

    # async def stop_tracing(self):
    #     await self.context.tracing.stop(path = "./tracer/trace.zip")

    async def errback(self, failure):
        print("FAIL")
        page = failure.request.meta["playwright_page"]
        await page.close()