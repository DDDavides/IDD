import scrapy
import requests
from bs4 import BeautifulSoup
from hw5.items import CompaniesmarketcapCompanyItem

class CompaniesmarketcapSpider(scrapy.Spider):
    name = 'companiesmarketcap'
    allowed_domains = ['companiesmarketcap.com']
    base_url = 'https://companiesmarketcap.com'

    start_urls = ["https://companiesmarketcap.com/page/" + str(i) for i in range(1, 11)]
    comapnies = []

    #Rules
    def parse(self, response):
        all_companies = response.xpath("//*[contains(@class, 'company-name')]/ancestor::a[1]/@href")

        for company in all_companies:
            yield scrapy.Request(self.base_url + company.get(), callback=self.parse_company)
    

    def parse_company(self, response):
        if response.status != requests.codes.ok:
            return
        
        soup = BeautifulSoup(response.text, 'lxml')
        company_info = soup.find_all("div", class_='line1')

        print(response.url)
        company = CompaniesmarketcapCompanyItem()
        company['name'] = soup.find("div", class_='company-name').text
        company['rank'] = company_info[0].text.split("#")[1]
        company['marketcap'] = company_info[1].text
        company['country'] = company_info[2].a['href'].split("/")[1] if company_info[2].a else "None"
        company['share_price'] = company_info[3].text
        company['change1d'] = company_info[4].span.text
        company['change1y'] = company_info[5].span.text if company_info[5].span else "None",
        company['categories'] = str([ category['href'].split("/")[1] for category in company_info[6].find_all("a") ]) if len(company_info) >= 7 and company_info[6] else "None",

        yield company

        
