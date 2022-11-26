import scrapy
import requests
from bs4 import BeautifulSoup
import csv


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

    def extract_data_from_table(self, bs):
        trow = bs.find_all('tr')
        print(len(trow))
        table = []
        for i in range(1, len(trow)):
            children_row = trow[i].findChildren('td', recursive=False)
            table.append({
                "Name": children_row[0].getText(),
                "Valuation": children_row[1].getText(),
                "Date Joined": children_row[2].getText(),
                "Country": children_row[3].getText(),
                "City": children_row[4].getText(),
                "Industry": children_row[5].getText(),
                "Investors": children_row[6].getText()
            })
            # if j == 0:
            #     yield scrapy.Request(children_row[j].find('a')['href'], callback=self.parse_company)
        csv_file = './dataset/cbinsight.csv'
        csv_columns = ['Name','Valuation',"Date Joined","Country","City","Industry", "Investors"]
        with open(csv_file, 'w') as csvfile:
            writer = csv.DictWriter(csvfile, fieldnames=csv_columns)
            writer.writeheader()
            for data in table:
                writer.writerow(data)
        
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