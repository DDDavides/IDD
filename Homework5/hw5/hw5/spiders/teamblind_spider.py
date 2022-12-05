import scrapy
import requests
from bs4 import BeautifulSoup
from hw5.items import BlindItem
from hw5.items import not_available
import random
import yaml

user_agent_list = [
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 14_4_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Mobile/15E148 Safari/604.1',
    'Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.1)',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36 Edg/87.0.664.75',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36 Edge/18.18363',
]


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
            yield scrapy.Request(self.base_url + company['href'], callback=self.parse_company, headers={"User-Agent": user_agent_list[random.randint(0, len(user_agent_list)-1)]})
        
    def parse_company(self, response):
        if response.status != requests.codes.ok:
            return
        bs = BeautifulSoup(response.text, 'lxml')
        company = BlindItem()
        company['name'] = bs.find('h1').get_text()
        company['website'] = bs.find(text = 'Website').parent.parent.a.get_text().replace('\n', '').strip() if bs.find(text = 'Website').parent.parent.a else not_available
        company['locations'] = bs.find(text = 'Locations').parent.next_sibling.replace('\n', '').strip() if bs.find(text = 'Locations').parent.next_sibling else not_available
        company['size'] = bs.find(text = 'Size').parent.next_sibling.replace('\n', '').strip() if bs.find(text = 'Size').parent.next_sibling else not_available
        company['industry'] = bs.find(text = 'Industry').parent.next_sibling.replace('\n', '').strip() if bs.find(text = 'Industry').parent.next_sibling else not_available
        company['founded'] = bs.find(text = 'Founded').parent.next_sibling.replace('\n', '').strip() if bs.find(text = 'Founded').parent.next_sibling else not_available
        yield company
