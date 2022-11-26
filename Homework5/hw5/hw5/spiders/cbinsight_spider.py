import scrapy
import requests
from bs4 import BeautifulSoup

def myselector(tag):
    return tag.name == 'td' and tag.findChildren('a')

class CbinsightSpider(scrapy.Spider):
    name = 'cbinsight'
    allowed_domains = ['www.cbinsights.com']
    start_urls = [
        'https://www.cbinsights.com/research-unicorn-companies'
    ]
    base_url = 'https://www.cbinsights.com/research-unicorn-companies'

    def parse(self, response):
        bs = BeautifulSoup(response.text, 'lxml')
        td = bs.find_all(myselector)
        for a in td:
            yield scrapy.Request(a.find('a')['href'], callback=self.parse_company)
        
    def parse_company(self, response):
        if response.status != requests.codes.ok:
            return
        bs = BeautifulSoup(response.text, 'lxml')
        title = bs.find('h1').getText()
        h = bs.find_all('h2', {'class': 'text-sm text-black'})
        founded, stage, totalRaised = None, None, None
        for i in range(0, len(h)-1):
            if i == 0:
                founded = h[i].next_sibling.getText()
            elif i == 1: 
                stage = h[i].next_sibling.getText()
            elif i == 2:
                totalRaised = h[i].next_sibling.getText()
        if title == None:
            print(response.url)
        print(title)
        print(founded)
        print(stage)
        print(totalRaised)

