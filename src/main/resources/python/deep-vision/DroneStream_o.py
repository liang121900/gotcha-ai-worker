import cv2
import numpy as np
import math
import threading
from utils import *
from darknet import Darknet


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
stop=False
ready=False

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

class streamThr(threading.Thread):
    def __init__(self,cap):
        super(streamThr, self).__init__()
        self.cap=cap

    def run(self):
        global boxes
        global frame
        global stop
        print "Starting Streaming Thread\n"
        class_names = load_class_names(namesfile)
        while (self.cap.isOpened()):
            # Capture frame-by-frame
            ret, frame = self.cap.read()
            if ret == False:
                print("Frame is empty")
                break
            if(boxes!=None):
                ploted_frame = plot(frame, boxes, class_names)
                cv2.imshow('frame', ploted_frame)
            else:
                cv2.imshow('frame', frame)
            if cv2.waitKey(1) & 0xFF == ord('q'):
                stop=True
                break
        # When everything done, release the capture
        self.cap.release()
	stop=True
        print "Exiting Streaming Thread\n"

class detectThr(threading.Thread):
    def __init__(self,m):
        super(detectThr, self).__init__()
        self.m=m

    def run(self):
        global frame
        global boxes
        global stop
        print "Starting Detection Thread\n"
        while(stop!=True):
            if(frame.size>1):
                sized = cv2.resize(frame, (self.m.width, self.m.height))
                boxes = do_detect(self.m, sized, 0.5, 0.4, 1)
        print "Exiting Detection Thread\n"


stream_t=streamThr(cap)
detect_t=detectThr(m)

stream_t.start()
detect_t.start()

stream_t.join()
detect_t.join()
