from bs4 import BeautifulSoup
import os
import shutil

from xls import Xls
from scrape import Scrape

def parse(xls,soup):

	global col
	global col_comments
	global row_comments
	scrape_page = Scrape()
	
	indices = [col,col_comments,row_comments]
	first_col =  scrape_page.extract_fields_first_column(file[:file.find(".")],soup)

	if first_col != 0 :
		indices = xls.write_xls(first_col,"../mozilla_bugzilla_xls/"+product+"/"+component+".xls",row,"fields",indices)

	second_col = scrape_page.extract_fields_second_column(soup)
	if second_col != 0:
		indices = xls.write_xls(second_col,"../mozilla_bugzilla_xls/"+product+"/"+component+".xls",row,"fields",indices)
	
	description = scrape_page.extract_description_comments(soup,file)
	if description != 0:
		indices = xls.write_xls(description,"../mozilla_bugzilla_xls/"+product+"/"+component+".xls",row_comments,"description",indices)	
		
	col = indices[0]
	col_comments = indices[1]
	row_comments = indices[2]



if __name__=="__main__":

	col=0
	if os.path.exists(os.getcwd()+"/"+"mozilla_bugzilla_xls"):
	    shutil.rmtree("mozilla_bugzilla_xls")

	os.mkdir("mozilla_bugzilla_xls")
	os.chdir("mozilla_bugzilla")
	xls = Xls()

	for product in os.listdir(os.getcwd()):
	    product_output_directory = "../mozilla_bugzilla_xls/"+product
	    os.mkdir(product_output_directory)
	    for component in os.listdir(os.getcwd()+"/"+product):
	        xls.create_xls(product_output_directory,component,"fields")
	        xls.create_xls(product_output_directory,component,"description")
	        row=1
	        row_comments = 1	        
	        for file in os.listdir(os.getcwd()+"/"+product+"/"+component):
	            soup = BeautifulSoup(open(os.getcwd()+"/"+product+"/"+component+"/"+file))
	            col = 0
	            col_comments = 0
	            [s.extract() for s in soup(['script', 'select'])] # ignore javascript during scraping
	            parse(xls,soup)
	            row+=1