#!/usr/bin/env python
import sys
from pyvirtualdisplay import Display
from selenium import webdriver

display = Display(visible=0,size=(800,600))
display.start()

browser = webdriver.Firefox()
i = 0

for line in sys.stdin:
	browser.get(line)
	browser.save_screenshot('screenie{0}.png'.format(i))
	i = i + 1

browser.quit()
display.stop()
