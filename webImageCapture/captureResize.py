import os
import sys
import math

from PIL import Image
from captureUrls import *
    
def makeLargeAndThumbnail( saveTo, largeSize, thumbnailSize ):
    ''' Makes thumbnail and then replaces saveTo with 
        cropped and resized image.'''            
    try:                        
        img = Image.open(saveTo)
    except Exception, err:
        sys.stderr.write('ERROR: Could not open image. %s\n' % str(err))
    
    try:
        dirname = os.path.dirname(saveTo)
        filename = os.path.basename(saveTo)
        cropAndResizeImage( img, dirname+'/'+fileNameToThumbnailName(filename), thumbnailSize)
    except Exception, err:
        sys.stderr.write('ERROR: Could not crop and resize image. %s\n' % str(err))
            
    try:         
        cropAndResizeImage( img, saveTo, largeSize)
    except Exception, err:
        sys.stderr.write('ERROR: Could not crop and resize image. %s\n' % str(err))
        
    
def cropAndResizeImage( orgImage, saveToFileName, size):
    '''Crop image to get the most width of the original image and 
       preserve the aspect ratio of the size. Then resize and save
       as saveToFileName.
       Parameter orgImage must be a PIL image object.
       Parameter size must be a tuple of (width,height) ex. (800,500)  '''
                                                    
    img = orgImage.copy()
            
    orgWidth = img.size[0]
    aspectRatio = float( size[0])/float( size[1])
    cropHeight = int(math.ceil( float(orgWidth) / aspectRatio ))             
    area = img.crop( (0,0, orgWidth, cropHeight ) )
            
    resized = area.resize( size , Image.ANTIALIAS)
    resized.save( saveToFileName, quality=100 )        
                                     
