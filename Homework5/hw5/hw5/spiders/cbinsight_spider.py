import scrapy
import requests
from bs4 import BeautifulSoup
from hw5.items import CbinsightItem
from hw5.items import not_available
import yaml

nones = ['None', 'n/a', 'N/A', '-']

class CbinsightSpider(scrapy.Spider):
    name = 'cbinsight'
    allowed_domains = ['www.cbinsights.com']
    start_urls = [
        'https://www.cbinsights.com/research-unicorn-companies'
    ]
    base_url = 'https://www.cbinsights.com/research-unicorn-companies'

    ntopick = 1000

    with open("../config.yaml", "r") as f:
        ntopick = yaml.load(f, Loader=yaml.FullLoader)['ntopick']
    def parse(self, response):
        bs = BeautifulSoup(response.text, 'lxml')
        trow = bs.find_all('tr')
        
        for i in range(1, min(len(trow), self.ntopick + 1)):
            children_row = trow[i].findChildren('td', recursive=False)
            company = CbinsightItem()
            company_info = []
            for child in children_row:
                if not child or child.get_text() in nones:
                    company_info.append(not_available)
                else:
                    company_info.append(child.getText())

            company['name'] = company_info[0]
            company['valuation'] = company_info[1] +'B'
            company['dateJoined'] = company_info[2]
            company['country'] = company_info[3]
            company['city'] =  company_info[4]
            company['industry'] =  company_info[5]
            company['investors'] =  company_info[6]
            yield scrapy.Request(children_row[0].find('a')['href'], callback=self.parse_company, cb_kwargs=dict(company = company))
        
    def parse_company(self, response, company):
        if response.status != requests.codes.ok:
            return

        bs = BeautifulSoup(response.text, 'lxml')
        fields = ['Founded Year', 'Stage', 'Total Raised']
        company_info = []

        for field in fields:
            elem = bs.find(text =  field)
            if not elem:
                company_info.append(not_available)
            else: 
                value = elem.parent.next_sibling.get_text()
                if value in nones:
                    company_info.append(not_available)
                else:
                    company_info.append(value)

        company['founded'] = company_info[0]
        company['stage'] = company_info[1]
        company['totalRaised'] = company_info[2]
        yield company

def myselector(tag):
    return tag.name == 'td' and tag.findChildren('a')