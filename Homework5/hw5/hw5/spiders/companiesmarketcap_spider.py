import scrapy
import requests
from bs4 import BeautifulSoup
from hw5.items import CompaniesmarketcapCompanyItem

class CompaniesmarketcapSpider(scrapy.Spider):
    name = 'companiesmarketcap'
    allowed_domains = ['companiesmarketcap.com']
    base_url = 'https://companiesmarketcap.com'

    start_urls = ["https://companiesmarketcap.com/page/" + str(i) for i in range(1, 50)]

    #Rules
    def parse(self, response):
        all_companies = response.xpath("//*[contains(@class, 'company-name')]/ancestor::a[1]/@href")

        for company in all_companies:
            yield scrapy.Request(self.base_url + company.get(), callback=self.parse_company)
    

    def parse_company(self, response):
        if response.status != requests.codes.ok:
            return
        
        soup = BeautifulSoup(response.text, 'lxml')

        fields = ["Rank", "Marketcap", "Country", "Share price", "Change (1 day)", "Change (1 year)", "Categories"]
        company_info = []
        for field in fields:
            div = soup.find(text=field, class_="line2")
            result = None
            if div and div.parent:
                result = div.parent.find("div", class_="line1")

            company_info.append(result)


        company = CompaniesmarketcapCompanyItem()
        company['name'] = soup.find("div", class_='company-name').text
        company['rank'] = company_info[0].text.split("#")[1]
        company['marketcap'] = company_info[1].text
        company['country'] = company_info[2].a['href'].split("/")[1] if company_info[2] and company_info[2].a else None
        company['share_price'] = company_info[3].text
        company['change1d'] = company_info[4].span.text if company_info[4] and company_info[4].span else company_info[4]
        company['change1y'] = company_info[5].span.text if company_info[5] and company_info[5].span else company_info[5]
        
        categories = []
        categ_str = ""
        if company_info[6]:
            categories = company_info[6].find_all("a")

        for category in categories:
            categ_str += "{} ".format(category['href'].split("/")[1])
        
        
        company['categories'] = categ_str

        yield company

        
