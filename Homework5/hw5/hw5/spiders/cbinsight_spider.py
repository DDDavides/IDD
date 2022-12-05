import scrapy
import requests
from bs4 import BeautifulSoup
from hw5.items import CbinsightItem
import yaml

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
            company['name'] = children_row[0].getText()
            company['valuation'] = children_row[1].getText() +'B'
            company['dateJoined'] = children_row[2].getText()
            company['country'] = children_row[3].getText()
            company['city'] =  children_row[4].getText()
            company['industry'] =  children_row[5].getText()
            company['investors'] =  children_row[6].getText()
            yield scrapy.Request(children_row[0].find('a')['href'], callback=self.parse_company, cb_kwargs=dict(company = company))
        
    def parse_company(self, response, company):
        if response.status != requests.codes.ok:
            return
        bs = BeautifulSoup(response.text, 'lxml')
        company['founded'] = bs.find(text =  'Founded Year').parent.next_sibling.get_text() if bs.find(text =  'Founded Year') else 'None'
        company['stage'] = bs.find(text =  'Stage').parent.next_sibling.get_text() if bs.find(text =  'Stage') else 'None'
        company['totalRaised'] = bs.find(text =  'Total Raised').parent.next_sibling.get_text() if bs.find(text =  'Total Raised') else 'None'
        yield company

def myselector(tag):
    return tag.name == 'td' and tag.findChildren('a')