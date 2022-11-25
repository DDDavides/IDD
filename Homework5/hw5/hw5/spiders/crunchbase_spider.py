import scrapy

user_agent_list = ['Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.1 Safari/605.1.15', 
                    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.79 Safari/537.36']

class CbinsightSpider(scrapy.Spider):
    name = 'crunchbase'
    allowed_domains = ['www.crunchbase.com']
    start_urls = [
        'https://news.crunchbase.com/unicorn-company-list/'
    ]

    #Rules

    def parse(self, response):
        all_companies = response.xpath("//td/a/@href")
        headers = {'User-Agent': user_agent_list}
        for company in all_companies:
            yield scrapy.Request(company.get(), callback=self.parse_company, headers=headers)
        
    def parse_company(self, response):
        if response.status != 200:
            return
        title = response.xpath("//h1[contains(@class,'profile-name')]/text()").get()
        if title == None:
            print(response.url)
        yield {
            'Title': title
        }
