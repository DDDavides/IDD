import scrapy

class QuotesSpider(scrapy.Spider):
    name = "quotes"
    allowed_domains = ['binsights.com']
    start_urls = [
        'https://www.cbinsights.com/research-unicorn-companies'
    ]
    base_url = 'https://www.cbinsights.com/research-unicorn-companies'

    #Rules

    def parse(self, response):
        all_companies = response.xpath('$x("//td/a[@href]")')