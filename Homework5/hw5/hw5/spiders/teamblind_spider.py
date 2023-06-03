import scrapy
import requests
from bs4 import BeautifulSoup
from hw5.items import BlindItem
from hw5.items import not_available
import random
import yaml

nones = ['None', 'n/a', 'N/A', '-']

class TeamblindSpider(scrapy.Spider):
    name = 'teamblind'
    allowed_domains = ['www.teamblind.com']
    start_urls = [
        'https://www.teamblind.com/sitemap/companies'
    ]
    base_url = 'https://www.teamblind.com'

    ntopick = 1000
    with open("../config.yaml", "r") as f:
        ntopick = yaml.load(f, Loader=yaml.FullLoader)['ntopick']

    def parse(self, response):
        bs = BeautifulSoup(response.text, 'lxml')
        companies = bs.find_all('a', class_='name')
        for company in random.sample(companies, self.ntopick):
            yield scrapy.Request(self.base_url + company['href'], callback=self.parse_company)
        
    def parse_company(self, response):
        if response.status != requests.codes.ok:
            return
        bs = BeautifulSoup(response.text, 'lxml')
        company = BlindItem()
        company['name'] = bs.find('h1').get_text() if bs.find('h1').get_text() not in nones else not_available
        company['website'] = bs.find(text = 'Website').parent.parent.a.get_text().replace('\n', '').strip() if bs.find(text = 'Website').parent.parent.a or bs.find(text = 'Website').parent.parent.a.get_text() not in nones else not_available

        fields = ['Locations', 'Size', 'Industry', 'Founded']
        company_info = []

        for field in fields:
            elem = bs.find(text =  field).parent.next_sibling
            if not elem:
                company_info.append(not_available)
            else: 
                value = elem.replace('\n', '').strip()
                if value in nones:
                    company_info.append(not_available)
                else:
                    company_info.append(value)

        company['locations'] = company_info[0]
        company['size'] = company_info[1]
        company['industry'] = company_info[2]
        company['founded'] = company_info[3]
        yield company
