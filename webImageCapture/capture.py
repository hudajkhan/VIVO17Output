#!/usr/bin/python
# should be python 2.7
import os
import re
import time
import sys

from pyvirtualdisplay import Display
from selenium import webdriver

from captureResize import * 
from captureUrls import * 

# make a screen shot for each URL in STDIN
# output resulting .PNG file to STDOUT

# ## to intall selenium:
# sudo pip install -U selenium

# ## to install pyvirtualdisplay on RHEL5:
# sudo bash
# curl http://python-distribute.org/distribute_setup.py | python
# curl -k https://raw.github.com/pypa/pip/master/contrib/get-pip.py | python

# requires Python Imaging Library
# http://www.pythonware.com/library/pil/handbook/index.htm

largeSize = (400, 300) # width height
thumbnailSize = (200, 150) # width height

basePath = "./imageCache/"

try:

    if len(sys.argv) <= 1:        
        sys.stderr.write('Error need URL\ncapture.py URL [thumbnail]\n')
        sys.exit(1)
        
    url = sys.argv[1]                
        
    if len(sys.argv) >= 3 and sys.argv[2] == 'thumbnail':
        type = 'thumbnail'
    else:
        type = 'large'            
        
    saveTo = basePath + urlToFileName(url)
    dirname = os.path.dirname(saveTo)
    filename = os.path.basename(saveTo) 	   
    if not os.path.exists(dirname):            
        os.makedirs(dirname)
    	 
    if not os.path.exists( saveTo ):
        try:
            try:
                display = Display(visible=0, size=(800, 500))
                display.start()
                browser = webdriver.Firefox()
                browser.get( url )
            except Exception, err:
                sys.stderr.write('ERROR: Could not open browser to page. %s\n' % str(err))                        
            try:
                browser.save_screenshot(saveTo)
            except Exception, err:
                sys.stderr.write('ERROR: Could not take screenshot. %s\n' % str(err))                
        except Exception, err:
            sys.stderr.write('ERROR: %s\n' % str(err))                        
        finally:
            browser.quit()
            display.stop()
        makeLargeAndThumbnail( saveTo, largeSize, thumbnailSize )
    else:
        sys.stderr.write(" file exists")
        
    if type == 'thumbnail' :
        sys.stderr.write('doing thumbnail')
        fileToOutput = dirname + '/' + fileNameToThumbnailName(filename)        
    else:
        sys.stderr.write('doing large')
        fileToOutput = saveTo
    
    # return image file contents as binary data    
    sys.stdout.write(file(fileToOutput, "rb").read())
    sys.stdout.flush()
    
except Exception, err:
        sys.stderr.write('ERROR: %s\n' % str(err))
        exit(1)
    
