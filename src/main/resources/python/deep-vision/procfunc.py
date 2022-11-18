import os
import matplotlib.pylab as plt
import cv2
import time
import numpy as np
import xml.dom.minidom
import random
#import detect

#terry new
from utils import *
from darknet import Darknet
cfgfile = "cfg/tiny-yolo.cfg"
weightfile = "backup/418_000060.weights"

#terry end

imageSize = (360, 640, 3)

##must be called to creat default directory
def setupDir(homeFolder, teamName):
    imgDir = homeFolder + '/images'
    resultDir = homeFolder + '/result'
    timeDir = resultDir + '/time'
    xmlDir = resultDir + '/xml'
    myXmlDir = xmlDir + '/' + teamName
    allTimeFile = timeDir + '/alltime.txt'
    if os.path.isdir(homeFolder):
        pass
    else:
        os.mkdir(homeFolder)
        
    if os.path.isdir(imgDir):
        pass
    else:
        os.mkdir(imgDir)
        
    if os.path.isdir(resultDir):
        pass
    else:
        os.mkdir(resultDir)
        
    if os.path.isdir(timeDir):
        pass
    else:    
        os.mkdir(timeDir)
        
    if os.path.isdir(xmlDir):
        pass
    else:
        os.mkdir(xmlDir)

            
    if os.path.isdir(myXmlDir):
        pass
    else:
        os.mkdir(myXmlDir)
    ##create timefile file
    ftime = open(allTimeFile,'a+')
    ftime.close()

    return [imgDir, resultDir, timeDir, xmlDir, myXmlDir, allTimeFile]

## get image name list
def getImageNames(imgDir):
    nameset1 = []
    nameset2 = []
    namefiles= os.listdir(imgDir)
    for f in namefiles:
        if 'jpg' in f:
            imgname = f.split('.')[0]
            nameset1.append(imgname)
    nameset1.sort(key = int)
    for f in nameset1:
        f = f + ".jpg"
        nameset2.append(f)
    imageNum = len(nameset2)
    return [nameset2, imageNum]

def readImagesBatch(imgDir, allImageName, imageNum, iter, batchNumDiskToDram):
    start = iter*batchNumDiskToDram
    end = start + batchNumDiskToDram
    if end > imageNum:
        end = imageNum
    #changed to assure images are stored as supported data type:uint8
    batchImageData = np.zeros((end-start, imageSize[0], imageSize[1], imageSize[2])).astype('uint8')
    for i in range(start, end):
        imgName = imgDir + '/' + allImageName[i]
        img = cv2.imread(imgName, 1)
        #terry resize if size not 640x360
        img= cv2.resize(img, (640, 360))
        #terry end
        batchImageData[i-start,:,:] = img[:,:]
    return batchImageData

## detection and tracking algorithm
def detectionAndTracking(inputImageData, batchNum,m):
    resultRectangle = np.zeros((batchNum, 4))

    for i in range(0,batchNum):
        img=inputImageData[i,:,:,:]
        sized = cv2.resize(img, (m.width, m.height))
        sized = cv2.cvtColor(sized, cv2.COLOR_BGR2RGB)
        boxes = do_detect(m, sized, 0.5, 0.4, 1)
        #class_names = load_class_names(namesfile)
        width = img.shape[1]
        height = img.shape[0]
        if len(boxes)>0:
            box = boxes[0]
            #xmin
            resultRectangle[i, 0]  = int(round((box[0] - box[2]/2.0) * width))
            #xmax
            resultRectangle[i, 1] = int(round((box[0] + box[2]/2.0) * width))
            #ymin
            resultRectangle[i, 2] = int(round((box[1] - box[3]/2.0) * height))
            #ymax
            resultRectangle[i, 3] = int(round((box[1] + box[3]/2.0) * height))
        else:
            #xmin
            resultRectangle[i, 0] = 0.0
            #xmax
            resultRectangle[i, 1] = 0.0
            #ymin
            resultRectangle[i, 2] = 0.0
            #ymax
            resultRectangle[i, 3] = 0.0

    return resultRectangle

## store the results about detection accuracy to XML files
def storeResultsToXML(resultRectangle, allImageName, myXmlDir):
    for i in range(len(allImageName)):
        doc = xml.dom.minidom.Document()
        root = doc.createElement('annotation')

        doc.appendChild(root)
        nameE = doc.createElement('filename')
        nameT = doc.createTextNode(allImageName[i])
        nameE.appendChild(nameT)
        root.appendChild(nameE)

        sizeE = doc.createElement('size')
        nodeWidth = doc.createElement('width')
        nodeWidth.appendChild(doc.createTextNode("640"))
        nodelength = doc.createElement('length')
        nodelength.appendChild(doc.createTextNode("360"))
        sizeE.appendChild(nodeWidth)
        sizeE.appendChild(nodelength)
        root.appendChild(sizeE)

        object = doc.createElement('object')
        nodeName = doc.createElement('name')
        nodeName.appendChild(doc.createTextNode("NotCare"))
        nodebndbox = doc.createElement('bndbox')
        nodebndbox_xmin = doc.createElement('xmin')
        nodebndbox_xmin.appendChild(doc.createTextNode(str(resultRectangle[i, 0])))
        nodebndbox_xmax = doc.createElement('xmax')
        nodebndbox_xmax.appendChild(doc.createTextNode(str(resultRectangle[i, 1])))
        nodebndbox_ymin = doc.createElement('ymin')
        nodebndbox_ymin.appendChild(doc.createTextNode(str(resultRectangle[i, 2])))
        nodebndbox_ymax = doc.createElement('ymax')
        nodebndbox_ymax.appendChild(doc.createTextNode(str(resultRectangle[i, 3])))
        nodebndbox.appendChild(nodebndbox_xmin)
        nodebndbox.appendChild(nodebndbox_xmax)
        nodebndbox.appendChild(nodebndbox_ymin)
        nodebndbox.appendChild(nodebndbox_ymax)

        #nodebndbox.appendChild(doc.createTextNode("360"))
        object.appendChild(nodeName)
        object.appendChild(nodebndbox)
        root.appendChild(object)

        fileName = allImageName[i].replace('jpg', 'xml')
        fp = open(myXmlDir + "/" + fileName, 'w')
        doc.writexml(fp, indent='\t', addindent='\t', newl='\n', encoding="utf-8")
    return

##write time result to alltime.txt
def write(imageNum,runTime,teamName, allTimeFile):
    FPS = imageNum / runTime
    ftime = open(allTimeFile, 'a+')
    ftime.write( "\n" + teamName + " Frames per second:" + str((FPS)) + '\n')
    ftime.close()
    return

#terry
def setup():
    m = Darknet(cfgfile)
    m.load_weights(weightfile)
    m.cuda()
    return m

