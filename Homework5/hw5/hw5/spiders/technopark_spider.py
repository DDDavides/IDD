import scrapy

class CbinsightSpider(scrapy.Spider):
    name = 'technopark'
    allowed_domains = ['www.technopark.org']
    start_urls = [
        'https://www.technopark.org/company-a-z-listing'
    ]

    #Rules
    def parse(self, response):
        all_companies = response.xpath("//div[contains(@class, 'cmpny-detail')]//a[@href][1]/@href")
        print(all_companies)
        for company in all_companies:
            print(company.get())
            yield scrapy.Request(company.get(), callback=self.parse_company)
        
    def parse_company(self, response):
        if response.status != 200:
            return
        # print(response.url)
        return ""
