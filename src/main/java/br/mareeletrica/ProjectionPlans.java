package br.mareeletrica;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class ProjectionPlans {
    public record Cell(BlockPos pos,BlockState original) {}
    public record Plan(int width,int height,int depth,List<Cell> cells) {public int rate(){return Math.max(200,cells.size()*2);}}
    private static final Map<ServerLevel,Map<Integer,Plan>> CACHE=new java.util.WeakHashMap<>();
    private ProjectionPlans() {}
    public static Plan get(ServerLevel level,int id) {return CACHE.computeIfAbsent(level,l->new HashMap<>()).computeIfAbsent(id,i->load(level,Blueprints.ALL.get(i)));}
    private static Plan load(ServerLevel level,Blueprints.Plan plan) {
        var cells=new ArrayList<Cell>();
        if(!plan.vanilla()) {
            for(var c:plan.cells())cells.add(new Cell(new BlockPos(c.x(),c.y(),c.z()),BuiltInRegistries.BLOCK.get(ResourceLocation.parse(c.block())).defaultBlockState()));
            return new Plan(plan.width(),plan.height(),plan.depth(),connected(cells));
        }
        var template=level.getStructureManager().get(ResourceLocation.parse(plan.template()));
        if(template.isEmpty())return new Plan(0,0,0,List.of());
        CompoundTag tag=template.get().save(new CompoundTag());ListTag palette=tag.getList("palette",10);
        if(palette.isEmpty())palette=tag.getList("palettes",9).getList(0);
        List<BlockState> states=new ArrayList<>();
        for(int i=0;i<palette.size();i++)states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),palette.getCompound(i)));
        ListTag blocks=tag.getList("blocks",10);
        for(int i=0;i<blocks.size();i++) {
            CompoundTag b=blocks.getCompound(i);int stateIndex=b.getInt("state");if(stateIndex<0||stateIndex>=states.size())continue;
            BlockState state=states.get(stateIndex);
            if(state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED))state=state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED,false);
            if(state.isAir()||state.is(Blocks.STRUCTURE_BLOCK)||state.is(Blocks.STRUCTURE_VOID)||state.is(Blocks.JIGSAW)||!state.getFluidState().isEmpty())continue;
            ListTag p=b.getList("pos",3);cells.add(new Cell(new BlockPos(p.getInt(0),p.getInt(1),p.getInt(2)),state));
        }
        var size=template.get().getSize();return new Plan(size.getX(),size.getY(),size.getZ(),List.copyOf(cells));
    }
    private static List<Cell> connected(List<Cell> cells) {
        Map<BlockPos,BlockState> byPos=new HashMap<>();for(Cell c:cells)byPos.put(c.pos(),c.original());
        List<Cell> result=new ArrayList<>();
        for(Cell c:cells) {
            BlockState s=c.original();
            if(s.getBlock() instanceof net.minecraft.world.level.block.FenceBlock || s.getBlock() instanceof net.minecraft.world.level.block.WallBlock || s.getBlock() instanceof net.minecraft.world.level.block.IronBarsBlock) {
                for(var direction:net.minecraft.core.Direction.Plane.HORIZONTAL) {
                    BlockState neighbor=byPos.get(c.pos().relative(direction));boolean connect=neighbor!=null&&!neighbor.isAir();
                    var property=s.getBlock().getStateDefinition().getProperty(direction.getName());
                    if(property instanceof net.minecraft.world.level.block.state.properties.BooleanProperty b)s=s.setValue(b,connect);
                    else if(property==net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH_WALL)s=s.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH_WALL,connect?net.minecraft.world.level.block.state.properties.WallSide.LOW:net.minecraft.world.level.block.state.properties.WallSide.NONE);
                    else if(property==net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST_WALL)s=s.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST_WALL,connect?net.minecraft.world.level.block.state.properties.WallSide.LOW:net.minecraft.world.level.block.state.properties.WallSide.NONE);
                    else if(property==net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH_WALL)s=s.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH_WALL,connect?net.minecraft.world.level.block.state.properties.WallSide.LOW:net.minecraft.world.level.block.state.properties.WallSide.NONE);
                    else if(property==net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST_WALL)s=s.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST_WALL,connect?net.minecraft.world.level.block.state.properties.WallSide.LOW:net.minecraft.world.level.block.state.properties.WallSide.NONE);
                }
            }
            result.add(new Cell(c.pos(),s));
        }
        return List.copyOf(result);
    }

}
