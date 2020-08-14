import xlwt
from xlrd import open_workbook
from xlutils.copy import copy


class Xls:

	'''
			Prepares an xls sheet into which the scrapes values will be stored 
			by adding the neccessary headers .
			
			Two sheets .. one for the fields and one for description and comments is
			created

	'''

	def create_xls(self,product,component_name,type):
		
	    if type == "description":
	    	rb = open_workbook(product+"/"+component_name+".xls")
	        workbook = copy(rb)

	        sheet = workbook.add_sheet("Comments")
	        style = xlwt.easyxf('font: bold 1;')
	        fd = open("../headers_description_comments.txt")
	    else:
	        workbook = xlwt.Workbook()
	        sheet = workbook.add_sheet("Fields")
	        style = xlwt.easyxf('font: bold 1;')
	        fd = open("../headers.txt")

	    lines = fd.readlines()
	    row = 0
	    col = 0
	    for line in lines:
	        sheet.write(row, col, line, style)
	        col += 1
	    workbook.save(product+"/"+component_name+".xls")


	'''
		Appends fields to the created xls sheet 
	

    global col
    global col_comments
    global row_comments
    '''

	def write_xls(self,fields,filename,row,type,indices):
		col = indices[0]
		col_comments = indices[1]
		row_comments = indices[2]
		rb = open_workbook(filename)
		wb = copy(rb)
		if type=="description":
			s = wb.get_sheet(1)
			bugid = fields[0]
			for i, comment in enumerate(fields[1:]):
				col_comments = 0
				s.write(row_comments,col_comments,bugid)
				col_comments +=1
				s.write(row_comments,col_comments,i)
				col_comments +=1
				s.write(row_comments,col_comments,comment)
				row_comments +=1
		else:
			s = wb.get_sheet(0)
			for field in fields:
				s.write(row, col, field)
				col+=1
		wb.save(filename)
		return [col,col_comments,row_comments]