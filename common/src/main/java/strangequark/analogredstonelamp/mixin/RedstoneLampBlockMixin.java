package strangequark.analogredstonelamp.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.redstone.Orientation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import strangequark.analogredstonelamp.CommonClass;

@Mixin(RedstoneLampBlock.class)
public abstract class RedstoneLampBlockMixin {

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void addProperty(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(CommonClass.ANALOG_LIGHT_LEVEL);
    }

    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void setInitialState(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = cir.getReturnValue();
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        int power = level.getBestNeighborSignal(pos);
        BlockState newState = state.setValue(CommonClass.ANALOG_LIGHT_LEVEL, power).setValue(RedstoneLampBlock.LIT, power > 0);

        cir.setReturnValue(newState);
    }

    @Inject(method = "neighborChanged", at = @At("HEAD"), cancellable = true)
    private void handleNeighborChange(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean flag, CallbackInfo ci) {
        if (!level.isClientSide) {
            int power = level.getBestNeighborSignal(pos);
            int current = state.getValue(CommonClass.ANALOG_LIGHT_LEVEL);
            boolean lit = state.getValue(RedstoneLampBlock.LIT);

            // Only schedule a tick if the power level actually changed
            if (power != current) {
                if (!lit && power > 0) {
                    // Turning on: immediate update
                    level.setBlock(pos, state.setValue(CommonClass.ANALOG_LIGHT_LEVEL, power).setValue(RedstoneLampBlock.LIT, true), Block.UPDATE_CLIENTS);
                } else {
                    // Turning off or changing levels: schedule tick for delay
                    level.scheduleTick(pos, state.getBlock(), 4);
                }
            }

            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void updateOnTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        int power = level.getBestNeighborSignal(pos);
        int current = state.getValue(CommonClass.ANALOG_LIGHT_LEVEL);

        // Only update if power changed
        if (power != current) {
            BlockState newState = state.setValue(CommonClass.ANALOG_LIGHT_LEVEL, power).setValue(RedstoneLampBlock.LIT, power > 0);
            level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
        }

        ci.cancel();
    }
}