'''
    Downloads the html files containing the bug details from Bugzilla@Mozilla

'''
import time
import os
import urllib2
from selenium import webdriver

os.chdir("mozilla_bugzilla_works")
driver = webdriver.Firefox()
driver.get("https://bugzilla.mozilla.org/describecomponents.cgi")
products = driver.find_elements_by_xpath("//*[@id='choose_product']/table/tbody/tr[*]/td[2]/h2/a")

for i in range(0,len(products)):
    product_name = products[i].text.replace("/", "_")
    os.mkdir(product_name)
    os.chdir(product_name)

    products[i].click()
    cuurent_url = driver.current_url
    components = driver.find_elements_by_xpath("//*[@id='bugzilla-body']/div[*]/div[1]/a[1]")
    for i in range(1, len(components)): # 1?
        dir_name = components[i].text.replace("/", "_")
        os.mkdir(dir_name)
        os.chdir(dir_name)

        components[i].click()
        bug_list_url = driver.current_url  # bug list page
        try :
            bugs = driver.find_element_by_class_name("sorttable_body")  # bug list
        except Exception:
            driver.get(cuurent_url)
            os.chdir("..")
            components = driver.find_elements_by_xpath("//*[@id='bugzilla-body']/div[*]/div[1]/a[1]")
            continue

        bug_description = bugs.find_elements_by_tag_name("a")

        for i in range(0, len(bug_description), 2):
            filename = bug_description[i].text + ".html"
            fd = open(filename, "w")
            bug_description[i].click()

            response = urllib2.urlopen(driver.current_url)
            html = response.read()

            time.sleep(0.1)
            fd.write(html)

            driver.get(bug_list_url)  # bug list page
            bugs = driver.find_element_by_class_name("sorttable_body")
            bug_description = bugs.find_elements_by_tag_name("a")

        driver.get(cuurent_url)
        os.chdir("..")
        components = driver.find_elements_by_xpath("//*[@id='bugzilla-body']/div[*]/div[1]/a[1]")

    driver.get("https://bugzilla.mozilla.org/describecomponents.cgi")
    products = driver.find_elements_by_xpath("//*[@id='choose_product']/table/tbody/tr[*]/td[2]/h2/a")
    os.chdir("..")



