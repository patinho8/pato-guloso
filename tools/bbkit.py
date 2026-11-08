"""shared kit for cube-built .bbmodel guns (make_deagle_claude.py style)."""
import json,uuid,base64,io,random,math,os
from PIL import Image
class Kit:
    def __init__(self,name,swatches,seed=1,brushed=()):
        random.seed(seed);self.name=name;self.SW={}
        tex=Image.new('RGBA',(32,32),(0,0,0,0))
        for i,(n,c) in enumerate(swatches.items()):
            x0,y0=(i%4)*8,(i//4)*8;self.SW[n]=(x0,y0,c)
            for x in range(8):
                for y in range(8):
                    d=random.randint(-5,5)
                    if n in brushed:d+=int(5*math.sin(x*1.3))
                    tex.putpixel((x0+x,y0+y),tuple(max(0,min(255,v+d)) for v in c)+(255,))
        assert len(swatches)<=16
        self.tex=tex;self.groups={};self.order=[]
    def uid(self):return str(uuid.uuid4())
    def cube(self,group,name,f,t,mat,rot=None,origin=None,faces=None):
        sx,sy,_=self.SW[mat];fs={}
        for d in ['north','east','south','west','up','down']:
            fs[d]={"uv":[sx+1,sy+1,sx+7,sy+7],"texture":0} if (faces is None or d in faces) else {"uv":[0,0,0,0],"texture":None}
        e={"name":name,"box_uv":False,"rescale":False,"locked":False,"render_order":"default","allow_mirror_modeling":True,
           "from":[round(v,4) for v in f],"to":[round(v,4) for v in t],"autouv":0,"color":0,
           "origin":origin or [round((f[i]+t[i])/2,4) for i in range(3)],"rotation":rot or [0,0,0],"faces":fs,"type":"cube","uuid":self.uid()}
        self.groups.setdefault(group,[]).append(e)
        if group not in self.order:self.order.append(group)
        return e
    def both(self,group,name,x0,y0,x1,y1,z0,z1,mat,rot=None,origin=None):
        """same piece on +z (z0..z1) and mirrored on -z"""
        self.cube(group,name,[x0,y0,z0],[x1,y1,z1],mat,rot,origin)
        o=None if origin is None else [origin[0],origin[1],-origin[2]]
        r=None if rot is None else [-rot[0],-rot[1],rot[2]]
        self.cube(group,name,[x0,y0,-z1],[x1,y1,-z0],mat,r,o)
    def tube(self,group,name,y,z,x0,x1,r,mat,sides=8):
        """octagonal tube along x"""
        for k in range(sides//2):
            a=180/sides*k;h=r*math.tan(math.pi/sides)
            self.cube(group,name,[x0,y-h,z-r],[x1,y+h,z+r],mat,rot=[a,0,0],origin=[x0,y,z])
    def post(self,group,name,x,z,y0,y1,r,mat):
        """octagonal post along y"""
        h=r*math.tan(math.pi/8)
        for a in (0,45):self.cube(group,name,[x-r,y0,z-h],[x+r,y1,z+h],mat,rot=[0,a,0],origin=[x,y0,z])
    def save(self,out):
        outliner=[];allel=[];idx={}
        for g in self.order:
            els=self.groups[g];idx[g]=list(range(len(allel),len(allel)+len(els)));allel+=els
            outliner.append({"name":g,"origin":[0,0,0],"color":0,"uuid":self.uid(),"export":True,"mirror_uv":False,"isOpen":True,"locked":False,"visibility":True,"autouv":0,"children":[e['uuid'] for e in els]})
        buf=io.BytesIO();self.tex.save(buf,'PNG');src='data:image/png;base64,'+base64.b64encode(buf.getvalue()).decode()
        bb={"meta":{"format_version":"4.10","model_format":"free","box_uv":False},"name":self.name,"model_identifier":"","visible_box":[1,1,0],
            "variable_placeholders":"","variable_placeholder_buttons":[],"timeline_setups":[],"unhandled_root_fields":{},
            "resolution":{"width":32,"height":32},"elements":allel,"outliner":outliner,
            "textures":[{"path":"","name":self.name+".png","folder":"block","namespace":"","id":"0","width":32,"height":32,"uv_width":32,"uv_height":32,
                 "particle":False,"render_mode":"default","render_sides":"auto","frame_time":1,"frame_order_type":"loop","frame_order":"","frame_interpolate":False,
                 "visible":True,"internal":True,"saved":False,"uuid":self.uid(),"source":src}]}
        json.dump(bb,open(os.path.join(out,self.name+'.bbmodel'),'w'),ensure_ascii=False)
        json.dump(idx,open(os.path.join(out,self.name+'_groups.json'),'w'))
        sw={f"{x0},{y0}":n for n,(x0,y0,_) in self.SW.items()}
        print(len(allel),'cubos',{g:len(v) for g,v in idx.items()})
        return idx,sw
