import scrapy
import requests
from scrapy_playwright.page import PageMethod
from hw5.items import FinancialItem
from bs4 import BeautifulSoup
import re
    
class NasdaqSpider(scrapy.Spider):
    name = 'financial'
    allowed_domains = ['ft.com']
    start_urls = ['https://www.ft.com/ft1000-2022']
    n_to_pick = 100

    def start_requests(self):
        meta = dict(
            playwright = True,
            playwright_page_methods = [
                PageMethod("wait_for_selector", "//button[text()='Show more']"),
            ],
            playwright_include_page = True,
            errback=self.errback
        )
        yield scrapy.Request(self.start_urls[0], meta=meta)

    async def parse(self, response):
        if response.status != requests.codes.ok:
            print("\n\nError code in\n\n", response.url)
            return

        page = response.meta["playwright_page"]
        soup = BeautifulSoup(response.text, 'lxml')
        
        first_page = True
        # Se accedo al sito la prima volta, allora faccio caricare tutte le istanze
        if first_page:
            await page.locator("//button[text()='Show more']").click()
            first_page = False

        # Seguire le regole definite nel parse_column_name
        attrs = [
            'name',
            'country',
            'sector',
            'absolute_growth_rate_pct',
            'compound_annual_growth_rate_cagr_pct',
            'revenue_2020_euro',
            'revenue_2017_euro',
            'number_of_employees_2020',
            'number_of_employees_2017',
            'founding_year'
        ]
        
        # su tutti i tag strong, prendo il testo ed eseguo il parsing del nome degli attributi delle colonne
        column_header = self.parse_column_names(list(map(lambda x : x.get_text(), soup.find_all('strong'))))

        # prendo tutte le istanze
        rows = soup.find('tbody').find_all('tr')
        if len(rows) > self.n_to_pick:
            rows = rows[:self.n_to_pick]

        for row in rows:
            financeItem = FinancialItem()
            # prendo i campi della riga corrente
            data = row.find_all('td')
            # per ogni coppia (nome attributo del dato, dato associato)
            for (d_attr,d) in zip(column_header,data):
                # se è un attributo d'interesse
                if d_attr in attrs:
                    # controllo necessario perché il nome è dentro un tag <a> annidato al <td> corrente
                    # <td><a>nome società</a></td>
                    if d_attr == 'name':
                        d = d.get_text()
                    financeItem[d_attr] = d
            yield financeItem
        await page.close()

    async def errback(self, failure):
        print("FAIL")
        page = failure.request.meta["playwright_page"]
        await page.close()

    def parse_column_name(self, str):
        str = str.strip().lower()
        # "€" -> "euro"
        str = re.sub(r"€",r"euro",str)
        
        # "%" -> "pct"
        str = re.sub(r"%",r"pct",str)

        # " " -> ""
        str = re.sub(r" ",r"_",str)

        # "(" -> "_"
        # ")" -> "_"  
        str = re.sub(r"\(|\)",r"_",str)

        # Due o più underscore consecutivi diventano un solo underscore
        str = re.sub(r"_{2,}",r"_",str)
        
        # Togli gli underscore all'inizio e alla fine della stringa
        str = re.sub(r"_+$|^_+",r"",str)

        return str

    def parse_column_names(self,str_list):
        return list(map(self.parse_column_name, str_list))