import scrapy
import requests
from bs4 import BeautifulSoup
import pandas as pd

class CbinsightSpider(scrapy.Spider):
    name = 'cbinsight'
    allowed_domains = ['www.cbinsights.com']
    start_urls = [
        'https://www.cbinsights.com/research-unicorn-companies'
    ]
    base_url = 'https://www.cbinsights.com/research-unicorn-companies'

    def parse(self, response):
        bs = BeautifulSoup(response.text, 'lxml')
        self.extract_data_from_table(bs)
        self.extract_data_of_company(bs)

    def extract_data_from_table(self, bs):
        trow = bs.find_all('tr')
        table = pd.DataFrame({
            "Name": [],
            "Valuation": [],
            "Date Joined": [],
            "Country": [],
            "City": [],
            "Industry": [],
            "Investors": []
        })
        for i in range(1, len(trow)):
            children_row = trow[i].findChildren('td', recursive=False)
            row = []
            for j in range(0, len(children_row)):
                if i == 1 and j == 0:
                    print(table[i])
                d = children_row[j]
            #     row.append(d.getText())
            # table.append(row)
            # print(table)

    def extract_data_of_company(self, bs):
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


def myselector(tag):
    return tag.name == 'td' and tag.findChildren('a')