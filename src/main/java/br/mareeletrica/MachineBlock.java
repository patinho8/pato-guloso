package br.mareeletrica;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MachineBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty EXPANDED = BooleanProperty.create("expanded");
    public static final BooleanProperty WIDE=BooleanProperty.create("wide");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public final Kind kind;
    private final VoxelShape shape;
    public MachineBlock(Kind kind, Properties properties) {
        super(properties); this.kind = kind;
        shape = switch(kind) {
            case LIFT, TELEPORT_PAD -> box(1, 0, 1, 15, 3, 15);
            case WATER_WHEEL -> Shapes.or(box(1, 0, 2, 15, 3, 14), box(2, 3, 2, 14, 15, 14));
            case SEA_TURBINE -> Shapes.or(box(1, 0, 1, 15, 3, 15), box(3, 3, 3, 13, 16, 13));
            case CORE_SMALL, CORE_MEDIUM, CORE_LARGE, CORE_QUANTUM -> Shapes.or(box(1, 0, 1, 15, 3, 15), box(3, 3, 3, 13, 15, 13));
            case IRRIGATOR, LAMP, REPELLER -> Shapes.or(box(3, 0, 3, 13, 3, 13), box(6, 3, 6, 10, 13, 10), box(2, 13, 2, 14, 16, 14));
            case SOLAR_PANEL -> Shapes.or(box(3, 0, 3, 13, 3, 13), box(6, 3, 6, 10, 8, 10), box(0, 8, 0, 16, 11, 16));
            case PRESENCE_SENSOR, MINING_BOOSTER -> Shapes.or(box(3, 0, 3, 13, 3, 13), box(6, 3, 6, 10, 13, 10), box(3, 10, 3, 13, 16, 13));
            default -> Shapes.or(box(1, 0, 1, 15, 3, 15), box(2, 3, 2, 14, 14, 14));
        };
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false).setValue(WATERLOGGED, false).setValue(EXPANDED,false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE, WATERLOGGED, EXPANDED);
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (kind.large()) {
            Direction facing=context.getHorizontalDirection().getOpposite();
            if (!LargeMachineStructure.hasSpace(context.getLevel(),context.getClickedPos(),facing,kind,true)) {
                if (!context.getLevel().isClientSide && context.getPlayer()!=null)
                    context.getPlayer().displayClientMessage(Component.translatable("gui.mareeletrica.large_space",kind.structureWidth(),kind.structureWidth(),kind.structureHeight()),true);
                return null;
            }
            BlockState placed=defaultBlockState().setValue(FACING,facing).setValue(EXPANDED,kind == Kind.QUARRY || kind == Kind.ROVER_BASE || kind == Kind.GIANT_WHEEL || kind==Kind.PURIFIER||kind==Kind.CORE_QUANTUM).setValue(WATERLOGGED,kind.waterStructure()&&context.getLevel().getFluidState(context.getClickedPos()).is(net.minecraft.tags.FluidTags.WATER));return kind==Kind.GIANT_WHEEL?placed.setValue(WIDE,true):placed;
        }
        if (kind == Kind.POTABILIZER) {
            Direction facing = context.getHorizontalDirection().getOpposite();
            if (!PotabilizerStructure.hasSpace(context.getLevel(),context.getClickedPos(),facing,true)) {
                if (!context.getLevel().isClientSide && context.getPlayer() != null)
                    context.getPlayer().displayClientMessage(Component.translatable("gui.mareeletrica.potabilizer_space"),true);
                return null;
            }
            return defaultBlockState().setValue(FACING,facing).setValue(EXPANDED,kind == Kind.QUARRY || kind == Kind.ROVER_BASE || kind == Kind.GIANT_WHEEL);
        }
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
            .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (kind.large()) return MachinePanel.add(state,LargeMachineStructure.shape(kind,0,state.getValue(FACING),state.getValue(EXPANDED),LargeMachineStructure.wide(state)));
        return MachinePanel.add(state,kind == Kind.POTABILIZER ? PotabilizerStructure.shape(0,state.getValue(FACING)) : shape);
    }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level,pos,state,placer,stack);
        if (placer instanceof Player p && level.getBlockEntity(pos) instanceof MachineEntity m) { m.owner = p.getUUID(); m.setChanged(); }
        if (kind == Kind.POTABILIZER) PotabilizerStructure.form(level,pos,state);
        if (kind.large()) LargeMachineStructure.form(level,pos,state);
    }
    @Override protected VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
        return kind==Kind.GIANT_WHEEL||kind==Kind.CURRENT_TURBINE?Shapes.empty():super.getCollisionShape(state,level,pos,context);
    }
    @Override protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
    @Override public boolean canPlaceLiquid(Player player, BlockGetter level, BlockPos pos, BlockState state, net.minecraft.world.level.material.Fluid fluid) {
        return kind.waterStructure() ? HydroWater.canFill(state, fluid) : SimpleWaterloggedBlock.super.canPlaceLiquid(player, level, pos, state, fluid);
    }
    @Override public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
        return kind.waterStructure() ? HydroWater.fill(level, pos, state, fluid) : SimpleWaterloggedBlock.super.placeLiquid(level, pos, state, fluid);
    }
    @Override protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new MachineEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == MareEletrica.MACHINE_ENTITY.get() ? (world, pos, blockState, entity) -> ((MachineEntity) entity).tick() : null;
    }
    @Override protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        if(kind==Kind.GIANT_WHEEL)return null;
        return level.getBlockEntity(pos) instanceof MachineEntity machine ? machine : null;
    }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return kind == Kind.PRESENCE_SENSOR; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return kind == Kind.PRESENCE_SENSOR && state.getValue(ACTIVE) ? 15 : 0;
    }
    @Override protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,net.minecraft.world.InteractionHand hand,BlockHitResult hit){if(stack.is(net.minecraft.world.item.Items.IRON_BLOCK)&&level.getBlockEntity(pos) instanceof MachineEntity hm&&hm.burnt){if(!level.isClientSide){hm.repair();if(!player.isCreative())stack.shrink(1);level.playSound(null,pos,net.minecraft.sounds.SoundEvents.ANVIL_USE,net.minecraft.sounds.SoundSource.BLOCKS,1F,1.2F);player.displayClientMessage(net.minecraft.network.chat.Component.literal("Máquina consertada. Deixe esfriar antes de forçar de novo."),true);}return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);}if(kind==Kind.POWER_CONTROLLER&&(stack.is(MareEletrica.CONTROL_MODULE.get())||stack.is(ControlLinks.PENDRIVE.get()))){if(!level.isClientSide&&player instanceof net.minecraft.server.level.ServerPlayer sp&&level.getBlockEntity(pos) instanceof MachineEntity m){if(stack.is(ControlLinks.PENDRIVE.get()))ControlLinks.link(sp,m,stack);else ControlLinks.open(sp,m);}return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);}if(stack.getItem() instanceof WirelessPlateItem || stack.getItem() instanceof WaterShaftItem || stack.is(MareEletrica.MACHINES.get(Kind.GIANT_WHEEL).get().asItem()))return net.minecraft.world.ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;return super.useItemOn(stack,state,level,pos,player,hand,hit);}
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if(kind==Kind.GIANT_WHEEL)return ControlMachines.turnCrank(level,pos,player)?InteractionResult.sidedSuccess(level.isClientSide):InteractionResult.PASS;
        if (kind == Kind.POTABILIZER || kind.large()) {
            PotabilizerStructure.interact(level,pos,player);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if(kind==Kind.POWER_CONTROLLER&&!player.isShiftKeyDown()){if(!level.isClientSide&&player instanceof net.minecraft.server.level.ServerPlayer sp&&level.getBlockEntity(pos) instanceof MachineEntity m)ControlLinks.open(sp,m);return InteractionResult.sidedSuccess(level.isClientSide);}
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                if(kind==Kind.BUILDER) {if(level.getBlockEntity(pos) instanceof MachineEntity m)ConstructionProjector.stop(m);}
                level.setBlock(pos, state.setValue(FACING, state.getValue(FACING).getClockWise()), 3);
            } else if (level.getBlockEntity(pos) instanceof MachineEntity machine) {
                player.openMenu(machine);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!level.isClientSide && kind == Kind.POTABILIZER && !state.is(next.getBlock()))
            PotabilizerStructure.removeParts(level,pos,state.getValue(FACING));
        if (!level.isClientSide && kind.large() && !state.is(next.getBlock()))
            LargeMachineStructure.removeParts(level,pos,state.getValue(FACING),kind,state.getValue(EXPANDED),LargeMachineStructure.wide(state));
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof MachineEntity machine) {
            if(!level.isClientSide&&machine.crank)net.minecraft.world.Containers.dropItemStack(level,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,new ItemStack(CrankBlocks.HAND_CRANK_ITEM.get()));
            WirelessPower.drop(machine);if(machine.kind==Kind.LAMP)FloodlightBeam.clear(machine);
            if (machine.kind == Kind.BUILDER) ConstructionProjector.stop(machine);
            if (machine.kind == Kind.XP_COLLECTOR && machine.storedXp > 0 && level instanceof net.minecraft.server.level.ServerLevel server) {
                net.minecraft.world.entity.ExperienceOrb.award(server, net.minecraft.world.phys.Vec3.atCenterOf(pos), machine.storedXp);
                machine.storedXp = 0;
            }
            for (int i = 0; i < machine.inventory.getSlots(); i++) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, machine.inventory.getStackInSlot(i));
            }
        }
        if(!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof MachineEntity m)for(int i=0;i<m.gasInventory.getSlots();i++)Containers.dropItemStack(level,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,m.gasInventory.getStackInSlot(i));
        super.onRemove(state, level, pos, next, moving);
    }
}
