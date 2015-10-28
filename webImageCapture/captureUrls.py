### Functions for URL to file name conversion ###

import os
import re
import time
import sys
from urllib import quote_plus

# The .png or .jpg at the end of the file sufix controls 
# the image format of the file.  
# The tomcat servlet expects png.
imageType = ".png"
fileSufix = "Image" + imageType

def queryToPath(queryStr):
  """Convert the query string of a URL to a path"""    
  return "/".join(map(quote_plus, queryStr.split("&")))
  
def urlToFileName(url):
  """Convert the URL and query string into a file path.
     The returned string will not start with a slash. """
  urlToPathAndQuery = re.compile(r"^http://([^\?]*)\?(.*)")
  match = urlToPathAndQuery.match(url)
  
  filename = None
  
  if match is not None:
    # query found, try this:
    filename = match.groups()[0] + "/" + queryToPath(match.groups()[1])
  else:
    urlregex = re.compile(r"^http://(.*)$")
    match = urlregex.match(url)
    if match is not None:
      filename= match.groups()[0]
    else:
      # not sure what it is but it might be a hostname
      filename = url
  
  return filename.replace('%','_') + fileSufix
  

def fileNameToThumbnailName( filename ):
    return 'thumbnail.' + filename
 
