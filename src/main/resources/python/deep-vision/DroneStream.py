import cv2
import numpy as np
import math
import threading
from utils import *
from darknet import Darknet
import urllib2
import time
from decimal import Decimal
import screeninfo
import os
os.system('nvpmodel -m0')
os.system('./jetson_clocks.sh')

try:
    filedata = urllib2.urlopen('http://192.168.53.1:7711/video')
    datatowrite = filedata.read()
    with open('video', 'wb') as f:
        f.write(datatowrite)
except:
    print("connection failed\n")

cfgfile = "cfg/yolov2.cfg"
weightfile = "backup/yolov2.weights"
namesfile="data/coco.names"

m = Darknet(cfgfile)
m.load_weights(weightfile)
m.cuda()

try:
    cap = cv2.VideoCapture('deepv_drone.sdp')
except:
    print("fail to acquire the stream\n")




frame=np.zeros((1, 1))
boxes=None
raw_boxes=None
stop=False
ready=False
stream_fps=0
detect_fps=0
time_up1=True
time_up2=True
time_det=True
time_h1s=False
obox_weight=0.5
nbox_weight=0.5
filter_thresh=0.7
timer_unit=0.1
def put_fps(img, str_fps,det_fps):
    font = cv2.FONT_HERSHEY_SIMPLEX
    img = cv2.putText(img, "Streaming Resolution: 854x480", (5, 20), font, 0.6, (219, 41, 148), 1, cv2.LINE_AA)
    img=cv2.putText(img, str_fps, (5, 40), font, 0.6, (216, 135, 78), 1, cv2.LINE_AA)
    img = cv2.putText(img, det_fps, (5, 60), font, 0.6, (67, 219, 183), 1, cv2.LINE_AA)
    return img




def plot(img, boxes, class_names,color=None):
    colors = torch.FloatTensor([[1,0,1],[0,0,1],[0,1,1],[0,1,0],[1,1,0],[1,0,0]]);
    def get_color(c, x, max_val):
        ratio = float(x)/max_val * 5
        i = int(math.floor(ratio))
        j = int(math.ceil(ratio))
        ratio = ratio - i
        r = (1-ratio) * colors[i][c] + ratio*colors[j][c]
        return int(r*255)

    width = img.shape[1]
    height = img.shape[0]
    for i in range(len(boxes)):
        box = boxes[i]
        x1 = int(round((box[0] - box[2]/2.0) * width))
        y1 = int(round((box[1] - box[3]/2.0) * height))
        x2 = int(round((box[0] + box[2]/2.0) * width))
        y2 = int(round((box[1] + box[3]/2.0) * height))

        if color:
            rgb = color
        else:
            rgb = (255, 0, 0)
        if len(box) >= 7 and class_names:
            cls_conf = box[5]
            cls_id = box[6]
            #print('%s: %f' % (class_names[cls_id], cls_conf))
            classes = len(class_names)
            offset = cls_id * 123457 % classes
            red   = get_color(2, offset, classes)
            green = get_color(1, offset, classes)
            blue  = get_color(0, offset, classes)
            if color is None:
                rgb = (red, green, blue)
            img = cv2.putText(img, class_names[cls_id], (x1,y1), cv2.FONT_HERSHEY_SIMPLEX, 1.2, rgb, 1)
        img = cv2.rectangle(img, (x1,y1), (x2,y2), rgb, 1)
    return img


class TimerThread(threading.Thread):
    def __init__(self):
        super(TimerThread, self).__init__()

    def run(self):
        print "Starting Timer Thread\n"
        global time_up1
        global time_up2
        global time_det
        global timer_unit
        global stop
        global time_h1s
        t2s=0.0
        while(stop!=True):
            t2s=t2s+timer_unit
            time_det=True
            if(t2s>=1):
                time_up1 = True
                time_up2 = True
                t2s=0.0
            if(t2s>=0.5):
                time_h1s=True
            #time_det=True
            time.sleep(timer_unit)
        print "Exiting Timer Thread\n"


class capThr(threading.Thread):
    def __init__(self,cap):
        super(capThr, self).__init__()
        self.cap=cap
    def run(self):
        global stop
        global frame
        global time_up1
        global stream_fps
        print "Starting Streaming Thread\n"
        while (stop!=True):
            if(time_up1):
                time_up1 = False
                t0=time.time()
                ret, frame = self.cap.read()
                t1=time.time()
                if(t1==t0):
                    stream_fps=30.0
                else:
                    stream_fps=1.0/(t1-t0)
                    stream_fps= Decimal(stream_fps)
                    stream_fps=round(stream_fps,2)
                if ret == False:
                    print("Frame is empty")
                    stop=False
                    break
            else:
                ret, frame = self.cap.read()
                if ret == False:
                    print("Frame is empty")
                    stop=False
                    break
        print "Exiting Cap Thread\n"


class disThr(threading.Thread):
    def __init__(self,cap):
        super(disThr, self).__init__()
        self.cap=cap

    def run(self):
        global boxes
        global frame
        global stop
        global detect_fps
        global stream_fps
        global time_h1s
        print "Starting Streaming Thread\n"
        class_names = load_class_names(namesfile)
        oboxs=None

        # get the size of the screen
        screen = screeninfo.get_monitors()[0]
        window_name = 'DeepVision'
        #width, height = screen.width, screen.height
        #cv2.namedWindow(window_name, cv2.WND_PROP_FULLSCREEN)
        #cv2.moveWindow(window_name, screen.x - 1, screen.y - 1)
        #cv2.setWindowProperty(window_name,cv2.WND_PROP_FULLSCREEN,cv2.WINDOW_AUTOSIZE)

        #kernel = np.ones((5, 5), np.float32) / 25


        while True:
            #f = cv2.resize(frame, (screen.width, screen.height))
            #f = cv2.blur(f,(5,5))
            #f=cv2.GaussianBlur(f, (5, 5), 0)
            #f = cv2.bilateralFilter(f, 9, 75, 75)
            #f = cv2.medianBlur(f, 5)
            if(boxes!=None and len(boxes)>0):
                nboxs = boxes
                nboxs = filter_box(oboxs, nboxs,time_h1s)
                oboxs = nboxs
                ploted_frame = plot(frame, boxes, class_names)
                #stream_fps=1.0/(time.time()-t0)
                fps_text1 = 'Streaming Fps: ' + str(stream_fps)
                fps_text2 ='Detection Fps: ' + str(detect_fps)
                ploted_frame = put_fps(ploted_frame, fps_text1,fps_text2)
                cv2.imshow(window_name, ploted_frame)
            else:
                fps_text1 = 'Streaming Fps: ' + str(stream_fps)
                fps_text2 ='Detection Fps: ' + str(detect_fps)
                ploted_frame = put_fps(frame, fps_text1, fps_text2)
                cv2.imshow(window_name, ploted_frame)
            if cv2.waitKey(1) & 0xFF == ord('q'):
                cv2.destroyAllWindows()
                stop=True
                break
        # When everything done, release the capture
        #self.cap.release()
        stop=True
        print "Exiting Streaming Thread\n"


def filter_box(oboxs,nboxs,time_h1s):
    if(oboxs and nboxs):
        for obox in range(len(oboxs)):
            for nbox in range(len(nboxs)):
                    #if same class, close distance
                if(oboxs[obox][6]==nboxs[nbox][6] and bbox_iou(oboxs[obox],nboxs[nbox],False)>filter_thresh):
                    nboxs[nbox][0:4]=np.add(np.multiply(nbox_weight,nboxs[nbox][0:4]),np.multiply(obox_weight, oboxs[obox][0:4]))
                    break
            if(time_h1s == False):
                    # if a previous box disappear because confidence flutuates, keep it
                nboxs.append(oboxs[obox])
            elif (time_h1s == True):
                time_h1s = False
                break
    return nboxs



class filterThr(threading.Thread):
    def __init__(self,lock):
        super(filterThr, self).__init__()
        self.lock=lock
    def run(self):
        global boxes
        global frame
        global raw_boxes
        global stop
        global time_h1s
        #oboxs=raw_boxes
        #nboxs=boxes
        while(stop!=True):
            self.lock.acquire()
            nboxes = raw_boxes
            self.lock.release()
            oboxes=boxes
            if(nboxes):
                if(boxes):
                    for obox in range(len(oboxes)):
                        for nbox in range(len(nboxes)):
                            # if same class, close distance
                            if (oboxes[obox][6] == nboxes[nbox][6] and bbox_iou(oboxes[obox], nboxes[nbox],False) > filter_thresh):
                                nboxes[nbox][0:4] = np.add(np.multiply(nbox_weight, nboxes[nbox][0:4]),np.multiply(obox_weight, oboxes[obox][0:4]))
                                break
                            elif(time_h1s==False):
                                # if a previous box disappear because confidence flutuates, keep it
                                nboxes.append(oboxes[obox])
                            elif(time_h1s==True):
                                time_h1s=False
                    boxes=nboxes
                else:
                    boxes=nboxes
        print "Exiting filter Thread\n"




class detectThr(threading.Thread):
    def __init__(self,m):
        super(detectThr, self).__init__()
        self.m=m

    def run(self):
        global frame
        global boxes
        global stop
        global detect_fps
        global time_up2
        global time_det
        print "Starting Detection Thread\n"
        nboxes=None
        while(stop!=True):
            if (time_up2):
                time_up2 = False
                time_det=False
                t0=time.time()
                if(frame.size>1):
                    sized = cv2.resize(frame, (self.m.width, self.m.height))
                    boxes = do_detect(self.m, sized, 0.4, 0.4, 1)
                t1=time.time()
                if(t1==t0):
                    detect_fps=30
                else:
                    detect_fps=1.0/(t1-t0)
                    detect_fps = Decimal(detect_fps)
                    detect_fps = round(detect_fps, 2)
            elif (time_det):
                time_det=False
                if (frame.size > 1):
                    sized = cv2.resize(frame, (self.m.width, self.m.height))
                    boxes = do_detect(self.m, sized, 0.4, 0.4, 1)
                #boxes=filter_box(boxes,nboxes)
        print "Exiting Detection Thread\n"


cap_t=capThr(cap)
dis_t=disThr(cap)
detect_t=detectThr(m)
timer=TimerThread()
#filter_t=filterThr(lock)

cap_t.start()
dis_t.start()
detect_t.start()
timer.start()
#filter_t.start()

cap_t.join()
dis_t.join()
detect_t.join()
#filter_t.join()
timer.join()
