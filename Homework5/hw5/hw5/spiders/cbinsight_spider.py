import scrapy
import requests
from bs4 import BeautifulSoup
import csv
from hw5.items import CbinsightItem

class CbinsightSpider(scrapy.Spider):
    name = 'cbinsight'
    allowed_domains = ['www.cbinsights.com']
    start_urls = [
        'https://www.cbinsights.com/research-unicorn-companies'
    ]
    base_url = 'https://www.cbinsights.com/research-unicorn-companies'

    def parse(self, response):
        bs = BeautifulSoup(response.text, 'lxml')
        trow = bs.find_all('tr')

        for i in range(1, len(trow)):
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
        h = bs.find_all('h2', {'class': 'text-sm text-black'})
        for i in range(0, len(h)-1):
            if i == 0:
                company['founded'] = h[i].next_sibling.getText()
            elif i == 1: 
                company['stage'] = h[i].next_sibling.getText()
            elif i == 2:
                company['totalRaised'] = h[i].next_sibling.getText()
        yield company

def myselector(tag):
    return tag.name == 'td' and tag.findChildren('a')