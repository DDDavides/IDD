import scrapy
import requests
from bs4 import BeautifulSoup
import csv
from hw5.items import BlindItem

class CbinsightSpider(scrapy.Spider):
    name = 'teamblind'
    allowed_domains = ['www.teamblind.com']
    start_urls = [
        'https://www.teamblind.com/sitemap/companies'
    ]
    base_url = 'https://www.teamblind.com/sitemap/companies'

    def parse(self, response):
        bs = BeautifulSoup(response.text, 'lxml')
        companies = bs.find_all('a', class_='name')

        for company in companies:
            print(self.start_urls[0] + company['href'])
            yield scrapy.Request(self.start_urls[0] + company['href'], callback=self.parse_company)
        
    def parse_company(self, response):
        # if response.status != requests.codes.ok:
        #     return
        bs = BeautifulSoup(response.text, 'lxml')
        company = BlindItem()
        company['name'] = bs.find('h1').get_text()
        print(bs.find('h1').get_text())
        company['website'] = bs.find(text = 'Website').next_sibling.get_text()
        company['location'] = bs.find(text = 'Location').next_sibling.get_text()
        company['size'] = bs.find(text = 'Size').next_sibling.get_text() 
        company['industry'] = bs.find(text = 'Industry').next_sibling.get_text()
        company['founded'] = bs.find(text = 'Founded').next_sibling.get_text()
        company['salary'] = bs.find(text = 'Salary').next_sibling.get_text()
        yield company
