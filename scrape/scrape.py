
class Scrape :


	def extract_summary(self,soup):
		summary = soup.find("span",id = "summary_alias_container")
		return summary.get_text()	


	def extract_fields_first_column(self,bugid,soup):
		'''
			first column refers to the list of fields in the
			left hand side of the bug details 
		'''
		fields = soup.find("table",class_="edit_form")
		if fields == None :
			return 0

		first_column = fields.find("td",id="bz_show_bug_column_1")
		if first_column == None:
			return 0

		rows =first_column.find_all("tr")
		if rows == None:
			return 0

		INVALID_ROWS=[3,5,9,15,17,20]
		result = []
		for i in range(0,len(rows)):
			if i not in INVALID_ROWS:
				str=rows[i].find("td").get_text().replace('\n', ' ').replace('\r', '')
				result.append(" ".join(str.split()))
		result.insert(0,bugid)
		result.insert(1,self.extract_summary(soup))
		return  result


	def extract_fields_second_column(self,soup):
		'''
			second column refers to the list of fields in the
			right hand side of the bug details 
		'''
		fields = soup.find("table", class_="edit_form")
		if fields == None:
			return 0

		second_column = fields.find("td", id="bz_show_bug_column_2")
		if second_column == None:
			return 0

		table = second_column.find("table")
		if table == None:
			return 0

		rows = table.find_all("tr", recursive=False)
		if rows == None:
			return 0

		INVALID_ROWS=[3,5]
		PROJECT_TRACKING = [12, 13] # handle project and tracking flags
		temp = []
		for i in range(0, len(rows)):
			if i not in INVALID_ROWS:
				if i in PROJECT_TRACKING:
					if len(rows[i].find_all("td")) < 2: # handling project flags and tracking flags
						temp.append("")
					else:
						temp.append(rows[i].find_all("td")[1].get_text())
				else:
					temp.append(rows[i].find("td").get_text())
		result =[]
		for i in temp:
			str=i.replace('\n', ' ').replace('\r', '')
			result.append(" ".join(str.split()))
		return  result


	def extract_description_comments(self,soup,file):
		com = soup.find_all("pre", class_="bz_comment_text")
		if com == None:
			return 0
		result = []
		for i in com:
			result.append(i.get_text())
		result.insert(0,file[:file.find(".")])
		return result
