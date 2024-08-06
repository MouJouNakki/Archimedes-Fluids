package com.moujounakki.archimedesfluids.mixins;

import com.moujounakki.archimedesfluids.ArchimedesFluidsCommonConfig;
import com.moujounakki.archimedesfluids.IMixinFlowingFluid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmBlock.class)
public abstract class MixinFarmBlock extends Block {
    @Shadow
    public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;

    @Shadow
    private static boolean isUnderCrops(BlockGetter p_53251_, BlockPos p_53252_) {
        return false;
    }
    @Shadow
    public static void turnToDirt(BlockState p_53297_, Level p_53298_, BlockPos p_53299_) {}

    public MixinFarmBlock(Properties p_49795_) {
        super(p_49795_);
    }
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    public void onRandomTick(BlockState blockstate, ServerLevel level, BlockPos pos, RandomSource randomSource, CallbackInfo callbackInfo) {
        int conf = ArchimedesFluidsCommonConfig.getFarmlandWaterConsumption();
        if (conf <= 0)
            return;
        int i = blockstate.getValue(MOISTURE);
        if (i == 0) {
            boolean found = false;
            if (net.minecraftforge.common.FarmlandWaterManager.hasBlockWaterTicket(level, pos))
                found = true;
            else if (level.isRainingAt(pos.above()))
                found = true;
            else {
                BlockState state = level.getBlockState(pos);
                for (BlockPos blockpos : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
                    if (state.canBeHydrated(level, pos, level.getFluidState(blockpos), blockpos)) {
                        found = true;
                        if (level.getFluidState(blockpos).getAmount() > 0)
                            ((IMixinFlowingFluid) level.getFluidState(blockpos).getType()).changeFluid(level,blockpos,-1);
                        break;
                    }
                }
            }
            if (found)
                level.setBlock(pos, blockstate.setValue(MOISTURE, Integer.valueOf(7)), 2);
            else if (!isUnderCrops(level, pos))
                turnToDirt(blockstate, level, pos);
//            if (!isNearWater(level, pos) && !level.isRainingAt(pos.above())) {
//                if (i > 0) {
//                    level.setBlock(pos, blockstate.setValue(MOISTURE, Integer.valueOf(i - 1)), 2);
//                } else if (!isUnderCrops(level, pos)) {
//                    turnToDirt(blockstate, level, pos);
//                }
//            } else if (i < 7) {
//                level.setBlock(pos, blockstate.setValue(MOISTURE, Integer.valueOf(7)), 2);
//            }
        }
        callbackInfo.cancel();
    }
}
