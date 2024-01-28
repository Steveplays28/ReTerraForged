package raccoonman.reterraforged.mixin;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import raccoonman.reterraforged.world.worldgen.GeneratorContext;
import raccoonman.reterraforged.world.worldgen.RTFRandomState;
import raccoonman.reterraforged.world.worldgen.cell.Cell;

@Mixin(NoiseBasedChunkGenerator.class)
class MixinNoiseBasedChunkGenerator {
	@Shadow
	@Final
    private Holder<NoiseGeneratorSettings> settings;
	
	@Redirect(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/levelgen/NoiseSettings;height()I"
		),
		require = 1,
		method = "fillFromNoise(Ljava/util/Executor;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/levelgen/StructureManager;Lnet/minecraft/world/level/levelgen/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"
	)
    public int fillFromNoise(NoiseSettings settings, Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess2) {
		GeneratorContext generatorContext;
		ChunkPos chunkPos = chunkAccess2.getPos();
		if((Object) randomState instanceof RTFRandomState rtfRandomState && (generatorContext = rtfRandomState.generatorContext()) != null) {
			return generatorContext.lookup.getGenerationHeight(chunkPos.x, chunkPos.z, this.settings.value(), true);
		} else {
    		return settings.height();
    	}
    }

	@Redirect(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/levelgen/NoiseSettings;height()I"
		),
		require = 2,
		method = "iterateNoiseColumn(Lnet/minecraft/world/level/levelgen/LevelHeightAccessor;Lnet/minecraft/world/level/levelgen/RandomState;IILorg/apache/commons/lang3/mutable/MutableObject;Ljava/util/function/Predicate;)Ljava/util/OptionalInt;"
	)
    private int iterateNoiseColumn(NoiseSettings settings, LevelHeightAccessor levelHeightAccessor, RandomState randomState, int blockX, int blockZ, @Nullable MutableObject<NoiseColumn> mutableObject, @Nullable Predicate<BlockState> predicate) {
		GeneratorContext generatorContext;
		if((Object) randomState instanceof RTFRandomState rtfRandomState && (generatorContext = rtfRandomState.generatorContext()) != null) {
			return generatorContext.lookup.getGenerationHeight(SectionPos.blockToSectionCoord(blockX), SectionPos.blockToSectionCoord(blockZ), this.settings.value(), false);
    	} else {
    		return settings.height();
    	}
    }
	
	@Inject(
		at = @At("TAIL"),
		method = "addDebugScreenInfo"
	)
    private void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos, CallbackInfo callback) {
		@Nullable
		GeneratorContext generatorContext;
		if((Object) randomState instanceof RTFRandomState rtfRandomState && (generatorContext = rtfRandomState.generatorContext()) != null) {
			Cell cell = new Cell();
			generatorContext.lookup.apply(cell, blockPos.getX(), blockPos.getZ());

			list.add("");
			list.add("Terrain Type: " + cell.terrain.getName());
			list.add("Terrain Region: " + cell.terrainRegionEdge);
			list.add("River Distance: " + (1.0F - cell.riverMask));
			list.add("");
    	}
    }
}
