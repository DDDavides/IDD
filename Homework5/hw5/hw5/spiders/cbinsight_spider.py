import scrapy

class CbinsightSpider(scrapy.Spider):
    name = 'cbinsight'
    allowed_domains = ['www.cbinsights.com']
    start_urls = [
        'https://www.cbinsights.com/research-unicorn-companies'
    ]
    base_url = 'https://www.cbinsights.com/research-unicorn-companies'

    #Rules

    def parse(self, response):
        all_companies = response.xpath("//td/a[@href]/@href")
        for company in all_companies:
            yield scrapy.Request(company.get(), callback=self.parse_company)
        
    def parse_company(self, response):
        if response.status != 200:
            return
        title = response.xpath('(//h1/text())[1]').get()
        if title == None:
            print(response.url)
        print(title)
        yield {
            'Title': title
        }
