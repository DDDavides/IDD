import scrapy
import requests
from bs4 import BeautifulSoup
from hw5.items import CompaniesmarketcapCompanyItem
from hw5.items import not_available
import yaml

class CompaniesmarketcapSpider(scrapy.Spider):
    name = 'companiesmarketcap'
    allowed_domains = ['companiesmarketcap.com']
    base_url = 'https://companiesmarketcap.com'
    
    ntopick = 1000
    with open("../config.yaml", "r") as f:
        ntopick = yaml.load(f, Loader=yaml.FullLoader)['ntopick']

    start_urls = ["https://companiesmarketcap.com/page/{}".format(i+1) for i in range((ntopick // 100) + 1)]

    #Rules
    def parse(self, response):
        soup = BeautifulSoup(response.text, 'lxml')
        all_companies = [x.parent['href'] for x in soup.find_all("div", class_="company-name")]
        
        if self.ntopick < 100:
            all_companies = all_companies[:self.ntopick]
        self.ntopick -= len(all_companies)
        
        for company_uri in all_companies:
            yield scrapy.Request(self.base_url + company_uri, callback=self.parse_company)
    

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
        company['rank'] = company_info[0].text.split("#")[1] if company_info[0] else not_available
        company['marketcap'] = company_info[1].text if company_info[1] else not_available
        company['country'] = company_info[2].a['href'].split("/")[1] if company_info[2] and company_info[2].a else not_available
        company['share_price'] = company_info[3].text if company_info[3] else not_available
        company['change1d'] = company_info[4].span.text if company_info[4] and company_info[4].span else not_available
        company['change1y'] = company_info[5].span.text if company_info[5] and company_info[5].span else not_available
        
        categories = []
        categ_str = ""
        if company_info[6]:
            categories = company_info[6].find_all("a")

        for category in categories:
            categ_str += "{} ".format(category['href'].split("/")[1])
        
        company['categories'] = categ_str if categ_str != "" else not_available

        yield company

        
