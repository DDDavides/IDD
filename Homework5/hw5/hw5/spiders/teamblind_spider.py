import scrapy
import requests
from bs4 import BeautifulSoup
from hw5.items import BlindItem
from hw5.items import not_available
import random
import yaml

def random_line(fname):
    lines = open(fname).read().splitlines()
    return random.choice(lines)


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
            yield scrapy.Request(self.base_url + company['href'], callback=self.parse_company,
            meta={"proxy": 'http://' + str(random_line('/Users/davidemolitierno/Repositories/IDD/Homework5/hw5/hw5/proxies.txt'))})
        
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
