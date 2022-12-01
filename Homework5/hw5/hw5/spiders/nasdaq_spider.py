import scrapy
import requests
from scrapy_playwright.page import PageMethod
from hw5.items import NasdaqItem
from bs4 import BeautifulSoup
import re

class NasdaqSpider(scrapy.Spider):
    name = 'nasdaq'
    base_url = 'https://www.nasdaq.com'
    # i = 0

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
            # playwright_page_methods =[
            #         PageMethod('wait_for_timeout', 1000),
            #     ],
            # when using prev setting, recommanded to use following setting
            # to make sure pages are closed even if a request fails
            errback = self.errback,
        ))

    async def parse(self, response):
        all_companies = []
        page = response.meta["playwright_page"]
        # print("Current page:", page)
        
        tot_companies = 100
        company_per_page = 25
        first_page = True
        for _ in range(tot_companies // company_per_page):
            # Se accedo al sito la prima volta, allora accetto i cookie
            if first_page:
                # Salvo uno screenshot di debug prima del click
                # await page.screenshot(path="nasdaq.png", full_page=True)
                # cerco il bottone "I Accept" e clicco per far scomparire il pop up
                await page.locator("xpath=//button[@id='onetrust-accept-btn-handler']").click()
                # Salvo uno screenshot di debug quando ho cliccato
                # await page.screenshot(path="nasdaq_after_decline.png", full_page=True)
                first_page = False
        
            content = await page.content()
            s = scrapy.Selector(text=content)
            all_companies.extend(s.xpath("//tbody[@class='nasdaq-screener__table-body']/tr/th/a/@href"))
            await page.locator("xpath=//button[contains(@class, 'pagination__page--active')]/following-sibling::button[1]").click()
            # await page.evaluate("document.evaluate(\"//button[contains(@class, 'pagination__page--active')]/following-sibling::button[1]\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.click()")

            # await page.wait_for_selector("//button[contains(@class, 'pagination__page--active')]/following-sibling::button[1]")
            # await page.wait_for_timeout(500)
            # await page.screenshot(path="nasdaq_last.png", full_page=True)
            # await page.wait_for_load_state('domcontentloaded')
        await page.close()
        # j = 0
        for company in all_companies:
            # print("["+str(self.i)+"]"+"COMPAGNIE:",all_companies[j].get())
            # self.i += 1
            # j += 1
            yield scrapy.Request(
                self.base_url+company.get(), 
                meta=dict(
                        playwright = True,
                        playwright_include_page = True,
                        playwright_page_methods =[
                            PageMethod('evaluate', "window.scrollTo(0, document.body.scrollHeight)"),
                            PageMethod('wait_for_timeout', 250),
                            PageMethod('evaluate', "window.scrollTo(document.body.scrollHeight, document.body.scrollHeight/8)"),
                            PageMethod('evaluate', "window.scrollTo(0, document.body.scrollHeight)"),
                            PageMethod('wait_for_timeout', 500),
                            PageMethod('evaluate', "window.scrollTo(document.body.scrollHeight, document.body.scrollHeight/8)"),
                        ],
                        errback = self.errback,
                    ),
                callback = self.parse_company
                )

    async def parse_company(self, response):
        page = response.meta["playwright_page"]
        # print("Current page company", response.url)

        soup = BeautifulSoup(response.text, 'lxml')
        if response.status != requests.codes.ok:
            print("Error code in", response.url)
            return
        
        name = response.xpath("//*[@class='symbol-page-header__name']/text()").get()
        # print("Name:", name)
        await page.screenshot(path="./screenshots/nasdaq_"+name+".png")
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

        # company_info = soup.find_all("td", {'class':'summary-data__cell'})
        # print(len(company_info), "attributes got")
        # print("INFO GOT", company_info)
        # for i in company_info:
        #     if i == None:
        #         print("Attribute", i, "empty for", response.url)
        
        # nsqItm = NasdaqItem()
        # nsqItm['name'] = name
        # # nsqItm['analysisDate'] = analysisDate  
        # nsqItm['exchange'] = company_info[0].get_text()
        # nsqItm['sector'] = company_info[1].get_text()
        # nsqItm['industry'] = company_info[2].get_text()
        # nsqItm['oneYearTarget'] = company_info[3].get_text()
        # nsqItm['shareVolume'] = company_info[5].get_text()
        # nsqItm['averageVolume'] = company_info[6].get_text()
        # nsqItm['marketCap'] = company_info[9].get_text()
        yield nsqItm

    async def errback(self, failure):
        print("FAIL")
        page = failure.request.meta["playwright_page"]
        await page.close()