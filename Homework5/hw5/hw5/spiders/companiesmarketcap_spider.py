import scrapy

class TechnoparkSpider(scrapy.Spider):
    name = 'companiesmarketcap'
    allowed_domains = ['companiesmarketcap.com']
    base_url = 'https://companiesmarketcap.com'

    start_urls = ["https://companiesmarketcap.com/page/" + str(i) for i in range(1, 11)]

    #Rules
    def parse(self, response):
        all_companies = response.xpath("//*[contains(@class, 'company-name')]/ancestor::a[1]/@href")
        print(len(all_companies))
        for company in all_companies:
            yield scrapy.Request(self.base_url + company.get(), callback=self.parse_company)
    
    # TODO: Estrarre i dati dalle varie pagine
    def parse_company(self, response):
        print(response.url)