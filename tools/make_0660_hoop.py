"""0.66.0: nicer basketball hoop — round (octagonal) rim, tapered net (separate part so it can swish), backboard with frame and
shooting square, padded pole on a weighted base. The rim stays exactly where it was (the scoring point is unchanged)."""
import json,os
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica/models/block')
old=json.load(open(os.path.join(A,'basketball_hoop.json')))
TX=old['textures']
def el(f,t,tex,rot=None):
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#"+tex} for d in ["north","south","west","east","up","down"]}}
    if rot:e["rotation"]={"origin":rot[0],"axis":"y","angle":rot[1]}
    for v in f+t:assert -16<=v<=48
    return e
CX,CZ,RY=8,6.75,26.0   # rim centre (model px) and rim bottom
E=[el([3,0,10],[13,2.2,16.6],'dark'),el([3.5,2.2,10.5],[12.5,3,16.1],'gray'),               # weighted base
   el([6.6,0,14],[9.4,24,16],'steel'),el([6.1,3,13.6],[9.9,13,16.4],'blue'),                  # pole + padding
   el([-4,20,12.6],[20,31.4,13.4],'white'),                                                       # backboard
   el([-4.2,31.4,12.5],[20.2,32,13.5],'black'),el([-4.2,19.8,12.5],[20.2,20.4,13.5],'black'),
   el([-4.2,19.8,12.5],[-3.6,32,13.5],'black'),el([19.6,19.8,12.5],[20.2,32,13.5],'black'),
   el([3,24,12.5],[13,24.6,12.55],'red'),el([3,29.9,12.5],[13,30.5,12.55],'red'),el([3,24,12.5],[3.6,30.5,12.55],'red'),el([12.4,24,12.5],[13,30.5,12.55],'red'),
   el([6.5,24.6,11],[9.5,26.6,12.6],'orange'),el([7.2,23.4,11.8],[8.8,24.6,12.6],'orange')]   # rim bracket
h,t=5.0,.75
def square(y0,y1,h,t,tex,rot=None):
    return [el([CX-h-t,y0,CZ-h-t],[CX+h+t,y1,CZ-h],tex,rot),el([CX-h-t,y0,CZ+h],[CX+h+t,y1,CZ+h+t],tex,rot),
            el([CX-h-t,y0,CZ-h],[CX-h,y1,CZ+h],tex,rot),el([CX+h,y0,CZ-h],[CX+h+t,y1,CZ+h],tex,rot)]
E+=square(RY,RY+.65,h,t,'orange')+square(RY,RY+.65,h,t,'orange',([CX,RY,CZ],45))              # octagonal rim
NET=[]
for (y0,y1,hh) in ((22.5,RY,h-.2),(19.5,22.5,h-1.6)):                                            # tapered net
    for rot in (None,([CX,y0,CZ],45)):
        NET+=[el([CX-hh,y0,CZ-hh],[CX+hh,y1,CZ-hh+.05],'net',rot),el([CX-hh,y0,CZ+hh-.05],[CX+hh,y1,CZ+hh],'net',rot),
              el([CX-hh,y0,CZ-hh],[CX-hh+.05,y1,CZ+hh],'net',rot),el([CX+hh-.05,y0,CZ-hh],[CX+hh,y1,CZ+hh],'net',rot)]
json.dump({"textures":TX,"elements":E},open(os.path.join(A,'basketball_hoop.json'),'w'),separators=(',',':'))
json.dump({"textures":TX,"elements":NET},open(os.path.join(A,'basketball_hoop_net.json'),'w'),separators=(',',':'))
print('hoop',len(E),'net',len(NET))
