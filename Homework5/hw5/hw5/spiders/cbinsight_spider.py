import scrapy

class CbinsightSpider(scrapy.Spider):
    name = "cbinsight"
    allowed_domains = ['cbinsights.com']
    start_urls = [
        'https://www.cbinsights.com/research-unicorn-companies'
    ]
    base_url = 'https://www.cbinsights.com/research-unicorn-companies'

    #Rules

    def parse(self, response):
        all_companies = response.xpath('$x("//td/a[@href]")')
        for company in all_companies:
            company_url = self.start_urls[0] + \
                company.xpath('.//h3/a/@href').extract_first()
            yield scrapy.Request(company_url, callback=self.parse_book)
        
    def parse_book(self, response):
        
        yield {
            'Title': '//*[@id="__next"]/main/div/div[2]/div/header/div[2]/div[1]/h1'
        }
