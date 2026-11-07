package br.mareeletrica;

/** The leveler never addresses a Y below its controller. Top-down order handles gravity blocks. */
public final class WorkArea {
    public static final int WIDTH=21,HEIGHT=21,CELLS=WIDTH*WIDTH*HEIGHT;
    private WorkArea() {}
    public static int[] offset(int index) {
        if(index<0||index>=CELLS)throw new IllegalArgumentException("index");
        return new int[]{index%WIDTH-WIDTH/2,20-index/(WIDTH*WIDTH),index/WIDTH%WIDTH-WIDTH/2};
    }
    public static int heading(int trip) {return Math.floorMod(trip,4);}
    public static int[] direction(int trip) {return switch(heading(trip)){case 0->new int[]{0,-1};case 1->new int[]{0,1};case 2->new int[]{1,0};default->new int[]{-1,0};};}
}
