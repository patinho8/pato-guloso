package br.mareeletrica;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Procedural holograms and references to the game's own village structure templates. */
public final class Blueprints {
    public record Cell(int x,int y,int z,String block) {}
    public record Plan(String id,int width,int height,int depth,List<Cell> cells,String template) {
        public Plan { cells=List.copyOf(cells); }
        public boolean vanilla() {return !template.isEmpty();}
        public int energy() {return Math.max(200,cells.size()*2);}
    }
    private static Plan village(String id,String path) {return new Plan(id,0,0,0,List.of(),"minecraft:village/"+path);}
    public static final List<Plan> ALL=List.of(
        tree(),village("house","plains/houses/plains_small_house_1"),tower(),garden(),arch(),platform(),
        bridge(),forest(),castle(),village("village_medium","plains/houses/plains_medium_house_1"),
        village("village_desert","desert/houses/desert_medium_house_1"),
        village("village_snow","snowy/houses/snowy_small_house_1"),
        village("village_taiga","taiga/houses/taiga_medium_house_1"),
        village("village_savanna","savanna/houses/savanna_medium_house_1"),
        concreteHouse(),concreteTower(),bunker(),garage(),concreteWall());
    private Blueprints() {}
    private static final class Draft {
        final String id;final int width,height,depth;final Map<String,Cell> cells=new LinkedHashMap<>();
        Draft(String id,int w,int h,int d) {this.id=id;width=w;height=h;depth=d;}
        void put(int x,int y,int z,String block) {if(x>=0&&x<width&&y>=0&&y<height&&z>=0&&z<depth)cells.put(x+","+y+","+z,new Cell(x,y,z,"minecraft:"+block));}
        void remove(int x,int y,int z) {cells.remove(x+","+y+","+z);}
        void fill(int x0,int y0,int z0,int x1,int y1,int z1,String block) {for(int x=x0;x<=x1;x++)for(int y=y0;y<=y1;y++)for(int z=z0;z<=z1;z++)put(x,y,z,block);}
        Plan finish() {var list=new ArrayList<>(cells.values());list.sort(Comparator.comparingInt(Cell::y));return new Plan(id,width,height,depth,list,"");}
    }
    private static void grow(Draft d,int x,int z,int height,int radius) {
        for(int y=0;y<height;y++)d.put(x,y,z,"oak_log");
        for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)for(int dy=-2;dy<=1;dy++)
            if(dx*dx+dz*dz+(dy+1)*(dy+1)<=radius*radius+1 && (dx!=0||dz!=0||dy>=0))d.put(x+dx,height+dy,z+dz,"oak_leaves");
        d.put(x,height+2,z,"oak_leaves");
    }
    private static Plan tree() {Draft d=new Draft("tree",9,11,9);grow(d,4,4,8,3);return d.finish();}
    private static Plan forest() {
        Draft d=new Draft("forest",41,16,41);
        for(int x:new int[]{4,12,18,28,36})for(int z=4;z<=36;z+=8)grow(d,x,z,8+Math.floorMod(x*13+z*7,5),3);
        for(int z=0;z<41;z++)d.fill(19,0,z,21,0,z,"oak_slab");
        for(int x=1;x<40;x+=3)for(int z=1;z<40;z+=3)if((x<18||x>22)&&!d.cells.containsKey(x+",0,"+z))d.put(x,0,z,"fern");
        return d.finish();
    }
    private static Plan bridge() {
        Draft d=new Draft("grand_bridge",11,18,49);
        d.fill(2,4,0,8,4,48,"stone_bricks");
        for(int z=0;z<49;z++)for(int x:new int[]{1,9})d.put(x,5,z,"cobblestone_wall");
        for(int z:new int[]{2,14,26,38,46}) {
            for(int x:new int[]{0,10}) {d.fill(x,0,z,x,14,z,"stone_bricks");d.put(x,15,z,"sea_lantern");d.put(x,16,z,"stone_brick_slab");}
            for(int x=0;x<=10;x++) {int h=14+(int)Math.round(2*Math.sin(Math.PI*x/10));d.put(x,h,z,"stone_bricks");}
        }
        for(int z=0;z<49;z++) {
            int y=7+(int)Math.round(6*Math.abs(Math.sin(Math.PI*z/24)));
            for(int x:new int[]{0,10}) {d.put(x,y,z,"iron_bars");for(int h=5;h<y;h++)if(z%4==0)d.put(x,h,z,"iron_bars");}
        }
        for(int z:new int[]{0,48})for(int x:new int[]{2,8})d.fill(x,0,z,x,3,z,"stone_bricks");
        // End staircases rise one block at a time to the elevated deck.
        for(int z=0;z<4;z++) {for(int x=3;x<=7;x++){d.remove(x,4,z);d.remove(x,4,48-z);}d.fill(3,0,z,7,z,z,"stone_bricks");d.fill(3,0,48-z,7,z,48-z,"stone_bricks");}
        return d.finish();
    }
    private static Plan tower() {
        Draft d=new Draft("tower",11,20,11);
        for(int y=0;y<17;y++)for(int x=1;x<10;x++)for(int z=1;z<10;z++) {
            boolean edge=x==1||x==9||z==1||z==9;
            if(y%5==0||edge) {if(x==5&&z==1&&y>0&&y<4)continue;d.put(x,y,z,y%5==3&&(x==5||z==5)?"glass":"stone_bricks");}
        }
        for(int x=0;x<11;x++)for(int z=0;z<11;z++)if((x==0||x==10||z==0||z==10)&&(x+z)%2==0)d.fill(x,17,z,x,18,z,"stone_bricks");
        d.put(5,19,5,"sea_lantern");return d.finish();
    }
    private static Plan castle() {
        Draft d=new Draft("castle",31,18,31);
        for(int x=0;x<31;x++)for(int z=0;z<31;z++) {
            if(x==0||x==30||z==0||z==30)for(int y=0;y<9;y++)if(!(z==0&&x>=13&&x<=17&&y<5))d.put(x,y,z,"stone_bricks");
            if((x==0||x==30||z==0||z==30)&&(x+z)%2==0)d.put(x,9,z,"cobblestone_wall");
        }
        for(int ox:new int[]{1,24})for(int oz:new int[]{1,24})for(int x=ox;x<ox+6;x++)for(int z=oz;z<oz+6;z++)for(int y=0;y<16;y++)
            if(x==ox||x==ox+5||z==oz||z==oz+5||y==14)d.put(x,y,z,"stone_bricks");
        for(int x=12;x<=18;x++)d.fill(x,0,0,x,0,30,"stone_slab");
        return d.finish();
    }
    private static Plan garden() {
        Draft d=new Draft("garden",15,8,15);d.fill(0,0,0,14,0,14,"stone_slab");
        for(int x=1;x<14;x+=2)for(int z=1;z<14;z+=2)d.put(x,1,z,(x+z)%4==0?"poppy":"dandelion");
        for(int y=1;y<=5;y++)d.put(7,y,7,"sea_lantern");
        d.fill(4,1,4,10,1,4,"stone_brick_slab");d.fill(4,1,10,10,1,10,"stone_brick_slab");
        grow(d,3,3,5,2);grow(d,11,11,5,2);return d.finish();
    }
    private static Plan arch() {
        Draft d=new Draft("arch",11,12,5);
        for(int x:new int[]{0,1,9,10})d.fill(x,0,0,x,8,4,"stone_bricks");
        for(int x=0;x<11;x++){int y=8+(int)Math.round(3*Math.sin(Math.PI*x/10));d.fill(x,y,0,x,y+1,4,"stone_bricks");}
        return d.finish();
    }
    private static Plan platform() {Draft d=new Draft("platform",15,3,15);d.fill(0,0,0,14,0,14,"stone_slab");for(int x=0;x<15;x++)for(int z=0;z<15;z++)if(x==0||x==14||z==0||z==14)d.put(x,1,z,"oak_fence");for(int x=6;x<=8;x++)d.remove(x,1,0);return d.finish();}
    // ---------------- concrete constructions (0.44.0) ----------------
    private static void box(Draft d,int x0,int y0,int z0,int x1,int y1,int z1,String wall){
        for(int x=x0;x<=x1;x++)for(int y=y0;y<=y1;y++)for(int z=z0;z<=z1;z++)if(x==x0||x==x1||z==z0||z==z1)d.put(x,y,z,wall);
    }
    private static Plan concreteHouse() {
        Draft d=new Draft("concrete_house",13,7,11);
        d.fill(0,0,0,12,0,10,"gray_concrete");
        box(d,0,1,0,12,4,10,"white_concrete");
        d.fill(0,5,0,12,5,10,"light_gray_concrete");d.fill(1,6,1,11,6,9,"smooth_stone_slab");
        for(int x=2;x<=10;x+=4){d.fill(x,2,0,x+1,3,0,"glass_pane");d.fill(x,2,10,x+1,3,10,"glass_pane");}
        d.fill(0,2,3,0,3,7,"glass_pane");d.fill(12,2,3,12,3,7,"glass_pane");
        d.remove(6,1,0);d.remove(6,2,0);                                   // front door opening
        for(int x=3;x<=9;x+=3)d.put(x,5,5,"sea_lantern");                  // ceiling lights
        d.fill(4,0,0,8,0,0,"smooth_stone");
        return d.finish();
    }
    private static Plan concreteTower() {
        Draft d=new Draft("concrete_building",9,22,9);
        for(int f=0;f<4;f++){int y=f*5;d.fill(0,y,0,8,y,8,"gray_concrete");box(d,0,y+1,0,8,y+4,8,"light_gray_concrete");
            for(int x=1;x<=7;x++){d.put(x,y+2,0,"glass");d.put(x,y+3,0,"glass");d.put(x,y+2,8,"glass");d.put(x,y+3,8,"glass");}
            for(int z=1;z<=7;z++){d.put(0,y+2,z,"glass");d.put(0,y+3,z,"glass");d.put(8,y+2,z,"glass");d.put(8,y+3,z,"glass");}
            for(int h=1;h<=5;h++)d.put(7,y+h,7,"ladder");d.remove(7,y+5,7);d.put(4,y+4,4,"sea_lantern");}
        d.fill(0,20,0,8,20,8,"gray_concrete");for(int x=0;x<9;x++)for(int z=0;z<9;z++)if(x==0||x==8||z==0||z==8)d.put(x,21,z,"iron_bars");
        for(int f=0;f<4;f++)d.remove(7,f*5,7);d.remove(4,1,0);d.remove(4,2,0);
        return d.finish();
    }
    private static Plan bunker() {
        Draft d=new Draft("bunker",11,5,11);
        d.fill(0,0,0,10,0,10,"gray_concrete");
        box(d,0,1,0,10,3,10,"gray_concrete");box(d,1,1,1,9,3,9,"gray_concrete");   // walls two blocks thick
        d.fill(0,4,0,10,4,10,"gray_concrete");
        for(int x=3;x<=7;x+=2){d.put(x,2,0,"iron_bars");d.put(x,2,1,"iron_bars");d.put(x,2,10,"iron_bars");d.put(x,2,9,"iron_bars");}
        for(int z=3;z<=7;z+=2){d.put(0,2,z,"iron_bars");d.put(1,2,z,"iron_bars");d.put(10,2,z,"iron_bars");d.put(9,2,z,"iron_bars");}
        d.remove(5,1,0);d.remove(5,2,0);d.remove(5,1,1);d.remove(5,2,1);             // entrance
        d.put(5,3,5,"sea_lantern");
        return d.finish();
    }
    private static Plan garage() {
        Draft d=new Draft("garage",11,6,13);
        d.fill(0,0,0,10,0,12,"black_concrete");for(int z=1;z<12;z+=2)d.put(5,0,z,"yellow_concrete");
        box(d,0,1,0,10,4,12,"light_gray_concrete");d.fill(0,5,0,10,5,12,"gray_concrete");
        for(int x=1;x<=9;x++)for(int y=1;y<=4;y++)d.remove(x,y,0);                  // wide door
        for(int z=3;z<=9;z+=3)d.put(5,5,z,"sea_lantern");d.fill(1,1,11,9,1,11,"smooth_stone_slab");
        return d.finish();
    }
    private static Plan concreteWall() {
        Draft d=new Draft("concrete_wall",27,11,5);
        d.fill(5,0,1,21,6,3,"gray_concrete");for(int x=5;x<=21;x+=2)d.put(x,7,1,"gray_concrete");for(int x=5;x<=21;x+=2)d.put(x,7,3,"gray_concrete");
        for(int x:new int[]{0,22}){d.fill(x,0,0,x+4,9,4,"light_gray_concrete");for(int i=0;i<5;i+=2){d.put(x+i,10,0,"light_gray_concrete");d.put(x+i,10,4,"light_gray_concrete");d.put(x,10,i,"light_gray_concrete");d.put(x+4,10,i,"light_gray_concrete");}
            d.put(x+2,6,0,"iron_bars");d.put(x+2,6,4,"iron_bars");d.put(x+2,9,2,"sea_lantern");}
        for(int x=12;x<=14;x++)for(int y=1;y<=4;y++)for(int z=1;z<=3;z++)d.remove(x,y,z);   // gate opening
        return d.finish();
    }
}
