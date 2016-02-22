package pneumaticCraft.common.heat.behaviour;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import pneumaticCraft.common.util.FluidUtils;

public abstract class HeatBehaviourLiquidTransition extends HeatBehaviourTransition{

    @Override
    public boolean isApplicable(){
        Fluid fluid = getFluid();
        return fluid != null && fluid.getTemperature() >= getMinFluidTemp() && fluid.getTemperature() <= getMaxFluidTemp() && super.isApplicable();
    }

    protected abstract int getMinFluidTemp();

    protected abstract int getMaxFluidTemp();

    protected abstract Block getTransitionedSourceBlock();

    protected abstract Block getTransitionedFlowingBlock();

    @Override
    protected void transformBlock(){
        transformSourceBlock(getTransitionedSourceBlock(), getTransitionedFlowingBlock());
        onTransition(getPos());
    }

    protected void transformSourceBlock(Block turningBlockSource, Block turningBlockFlowing){
        if(FluidUtils.isSourceBlock(getWorld(), getPos())) {
            getWorld().setBlockState(getPos(), turningBlockSource.getDefaultState());
        } else {
            Set<BlockPos> traversed = new HashSet<BlockPos>();
            Stack<BlockPos> pending = new Stack<BlockPos>();
            pending.push(getPos());
            traversed.add(getPos());
            while(!pending.isEmpty()) {
                BlockPos pos = pending.pop();
                for(EnumFacing d : EnumFacing.VALUES) {
                    BlockPos newPos = pos.offset(d);
                    Block checkingBlock = getWorld().getBlockState(newPos).getBlock();
                    if((checkingBlock == getBlockState().getBlock() || getBlockState().getBlock() == Blocks.flowing_water && checkingBlock == Blocks.water || getBlockState().getBlock() == Blocks.flowing_lava && checkingBlock == Blocks.lava) && traversed.add(newPos)) {
                        if(FluidUtils.isSourceBlock(getWorld(), newPos)) {
                            getWorld().setBlockState(newPos, turningBlockSource.getDefaultState());
                            onTransition(newPos);
                            return;
                        } else {
                            getWorld().setBlockState(newPos, turningBlockFlowing.getDefaultState());
                            onTransition(newPos);
                            pending.push(newPos);
                        }
                    }
                }
            }
        }
    }
}
