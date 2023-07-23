/*
 * This file is part of  Treasure2.
 * Copyright (c) 2022 Mark Gottschling (gottsch)
 *
 * Treasure2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Treasure2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Treasure2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package mod.gottsch.fabric.treasure2.core.block.entity;

import mod.gottsch.fabric.gottschcore.enums.IRarity;
import mod.gottsch.fabric.gottschcore.util.LangUtil;
import mod.gottsch.fabric.treasure2.api.TreasureApi;
import mod.gottsch.fabric.treasure2.core.Treasure;
import mod.gottsch.fabric.treasure2.core.block.ITreasureChestBlock;
import mod.gottsch.fabric.treasure2.core.lock.LockState;
import mod.gottsch.fabric.treasure2.core.rarity.Rarity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * 
 * @author Mark Gottschling on Nov 10, 2022
 *
 */
public abstract class AbstractTreasureChestBlockEntity extends BlockEntity implements ITreasureChestBlockEntity, MenuProvider, Nameable {

	private static final String LOCK_STATES_TAG = "lockStates";
	private static final String FACING_TAG = "facing";
	private static final String SEALED_TAG = "sealed";

	private static final String LOOT_TABLE_TAG = "lootTable";
	
	private static final String GENERATION_CONTEXT_TAG = "generationContext";
	private static final String LOOT_RARITY_TAG = "lootRarity";
	private static final String CHEST_GENERATOR_TYPE_TAG = "chestGeneratorType";


	// TODO have to go back to old school chest inventory.
	/*
	 * The inventory of the chest
	 */
	private final ItemStackHandler itemHandler = createHandler();
	private LazyOptional<IItemHandler> instanceHandler = LazyOptional.empty();

	/*
	 * A list of lockStates the chest has. The list should be the size of the max
	 * allowed for the chestType.
	 */
	private List<LockState> lockStates;

	/*
	 * The FACING index value of the TreasureChestBlock
	 */
	private Direction facing;

	/*
	 * A flag to indicate if the chest has been opened for the first time
	 */
	private boolean sealed;

	/*
	 * Properties detailing how the tile entity was generated
	 */
	private GenerationContext generationContext;

	/*
	 * The loot table assigned to this block entity
	 */
	private ResourceLocation lootTable;

	/*
	 * The custom name of this block entity
	 */
	private Component name;

	/*
	 * Vanilla properties for controlling the lid
	 */
	/** The current angle of the lid (between 0 and 1) */
	public float lidAngle;
	/** The angle of the lid last tick */
	public float prevLidAngle;

	/*
	 * Server updated properties
	 */
	/** The number of players currently using this chest */
	public int openCount;
	/** Server sync counter (once per 20 ticks) */
	public int ticksSinceSync;

	/**
	 * 
	 * @param type
	 * @param pos
	 * @param state
	 */
	public AbstractTreasureChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setFacing(Direction.NORTH.get3DDataValue());
	}

	/**
	 * 
	 */
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
		Treasure.LOGGER.debug("is chest sealed -> {}", this.isSealed());
		if (this.isSealed()) {
			this.setSealed(false);
			Treasure.LOGGER.debug("chest gen type -> {}", this.getGenerationContext().getChestGeneratorType()); 
			IChestGeneratorType chestGeneratorType = this.getGenerationContext().getChestGeneratorType();
			Optional<IChestGenerator> chestGenerator = ChestGeneratorRegistry.get(chestGeneratorType);
			if (chestGenerator.isPresent()) {
				Treasure.LOGGER.debug("chest gen  -> {}", chestGenerator.get().getClass().getSimpleName());
				// fill the chest with loot
				chestGenerator.get().fillChest(getLevel(), new Random(), this, this.getGenerationContext().getLootRarity(), playerEntity);
			}
			else {
				Treasure.LOGGER.warn("treasure chest at -> {} does not reference a valid generator -> {}", this.worldPosition, chestGenerator.get().getClass().getSimpleName());
			}
		}
    	return createChestContainerMenu(windowId, playerInventory, playerEntity);
    }
    
    /**
     * 
     * @param windowId
     * @param playerInventory
     * @param playerEntity
     * @return
     */
    public AbstractContainerMenu createChestContainerMenu(int windowId, Inventory playerInventory, Player playerEntity) {
    	return new StandardChestContainerMenu(windowId, this.worldPosition, playerInventory, playerEntity);
    }
    
	/**
	 * 
	 * @return
	 */
	private ItemStackHandler createHandler() {
		return new ItemStackHandler(getInventorySize()) {

			@Override
			protected void onContentsChanged(int slot) {
				// To make sure the BE persists when the chunk is saved later we need to
				// mark it dirty every time the item handler changes
				setChanged();
			}

			@Override
			public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
				// TODO add check for locked property and return false is enabled
				//                return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
				return true;
			}

			@Nonnull
			@Override
			public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
				//                if (ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) <= 0) {
				//                    return stack;
				//                }
				return super.insertItem(slot, stack, simulate);
			}
		};
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return instanceHandler.cast();
		}
		return super.getCapability(cap);
	}

	/**
	 * 
	 * @param level
	 * @param pos
	 */
	public void dropContents(Level level, BlockPos pos) {
		Optional<IItemHandler> handler = 	getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(h -> h);
		if (handler.isPresent()) {
			int numberOfSlots = handler.get().getSlots();
			for (int i = 0; i < numberOfSlots; i++) {
				ItemStack stack = handler.get().getStackInSlot(i);
				if (stack != null && stack != ItemStack.EMPTY) {
					Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
				}
			}
		}
	}

	/**
	 * 
	 */
	@Override
	public void tickClient() {
		this.prevLidAngle = this.lidAngle;
		if (this.openCount > 0 && this.lidAngle == 0.0F) {
			this.playSound(SoundEvents.CHEST_OPEN);
		}

		if (this.openCount == 0 && this.lidAngle > 0.0F || this.openCount > 0 && this.lidAngle < 1.0F) {
			float f2 = this.lidAngle;

			if (this.openCount > 0) {
				this.lidAngle += 0.1F;
			} else {
				this.lidAngle -= 0.1F;
			}

			if (this.lidAngle > 1.0F) {
				this.lidAngle = 1.0F;
			}

			//float f3 = 0.5F;
			if (this.lidAngle < 0.5F && f2 >= 0.5F) {
				this.playSound(SoundEvents.CHEST_CLOSE);
			}

			if (this.lidAngle < 0.0F) {
				this.lidAngle = 0.0F;
			}
		}	
	}
	
	@Override
	public void tickServer() {
		if (Config.SERVER.effects.enableUndiscoveredEffects.get()
				&& ITreasureChestBlock.getUndiscovered(this.getBlockState())) {
			
			// TODO test ticks ie world time
			
			// TODO move this to IChestEffects
			// TODO server spawn particles
			// TODO use gold and silver coins as particles to move upwards and spin
			// TODO use long particles to simulate beams of light eminating from the chest.. see Ars mod?
			((ServerLevel) getLevel()).sendParticles(ParticleTypes.SMOKE,
					getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), 20, 0.0D, 0.0D, 0.0D, 0.5D);
		}
	}

	protected void playSound(SoundEvent sound) {
		double d0 = (double)getBlockPos().getX() + 0.5D;
		double d1 = (double)getBlockPos().getY() + 0.5D;
		double d2 = (double)getBlockPos().getZ() + 0.5D;
		// TODO ensure that other players can hear the chest opening as it is playLocalSound()
		level.playLocalSound(d0, d1, d2, sound, SoundSource.BLOCKS, 1.0F, 1.0F, false);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		instanceHandler = LazyOptional.of(() -> itemHandler);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		instanceHandler.invalidate();
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		tag.put("inventory", itemHandler.serializeNBT());
		// write lock states
		saveLockStates(tag);
		saveProperties(tag);
		super.saveAdditional(tag);
	}

	/**
	 * 
	 * @param tag
	 * @return
	 */
	public CompoundTag saveLockStates(CompoundTag tag) {
		try {
			// write lock states
			if (getLockStates() != null && !getLockStates().isEmpty()) {
				ListTag list = new ListTag();
				// write custom tile entity properties
				for (LockState state : getLockStates()) {
					Treasure.LOGGER.trace("saving lock state:" + state);
					CompoundTag stateTag = new CompoundTag();
					state.save(stateTag);
					list.add(stateTag);
				}
				tag.put(LOCK_STATES_TAG, list);
			}
		} catch (Exception e) {
			Treasure.LOGGER.error("error writing LockStates to nbt:", e);
		}
		return tag;
	}

	public CompoundTag saveProperties(CompoundTag tag) {
		try {
			// write facing
			tag.putInt(FACING_TAG, getFacing().get3DDataValue());
			tag.putBoolean(SEALED_TAG, isSealed());
//			tag.putBoolean(UNDISCOVERED_TAG, isUndiscovered());
			if (getLootTable() != null) {
				tag.putString(LOOT_TABLE_TAG, getLootTable().toString());
			}

			if (getGenerationContext() != null) {
				CompoundTag contextTag = new CompoundTag();
				contextTag.putString(LOOT_RARITY_TAG, getGenerationContext().getLootRarity().getValue());
				contextTag.putString(CHEST_GENERATOR_TYPE_TAG, getGenerationContext().getChestGeneratorType().getName());
				tag.put(GENERATION_CONTEXT_TAG, contextTag);
			}
		} catch (Exception e) {
			Treasure.LOGGER.error("error writing Properties to nbt:", e);
		}
		return tag;
	}

	/**
	 * 
	 */
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		try {
			itemHandler.deserializeNBT(tag.getCompound("inventory"));
			loadLockStates(tag);
			loadProperties(tag);
		} catch (Exception e) {
			Treasure.LOGGER.error("error reading to nbt:", e);
		}		
	}

	/**
	 * Same as load but does not call super.load()
	 * @param tag
	 */
	public void loadFromItem(CompoundTag tag) {
		try {
			itemHandler.deserializeNBT(tag.getCompound("inventory"));
			loadLockStates(tag);
			loadProperties(tag);
		} catch (Exception e) {
			Treasure.LOGGER.error("error reading to nbt:", e);
		}
	}

	/**
	 * 
	 * @param tag
	 */
	public void loadLockStates(CompoundTag tag) {
		try {
			// read the lockstates
			if (tag.contains(LOCK_STATES_TAG)) {
				//	Treasure.LOGGER.info("Has lockStates");
				if (this.getLockStates() != null) {
					//	Treasure.LOGGER.info("size of internal lockstates:" + this.getLockStates().size());
				} else {
					this.setLockStates(new LinkedList<LockState>());
					//	Treasure.LOGGER.info("created lockstates:" + this.getLockStates().size());
				}

				List<LockState> states = new LinkedList<LockState>();
				ListTag list = tag.getList(LOCK_STATES_TAG, Tag.TAG_COMPOUND);
				for (int i = 0; i < list.size(); i++) {
					CompoundTag c = list.getCompound(i);
					LockState lockState = LockState.load(c);
					states.add(lockState.getSlot().getIndex(), lockState);
					//	Treasure.LOGGER.info("Read NBT lockstate:" + lockState);
				}
				// update the tile entity
				setLockStates(states);
			}
		} catch (Exception e) {
			Treasure.LOGGER.error("error reading Lock States from nbt:", e);
		}
	}

	public void loadProperties(CompoundTag nbt) {
		try {
			// read the facing
			if (nbt.contains(FACING_TAG)) {
				this.setFacing(nbt.getInt(FACING_TAG));
			}
			if (nbt.contains(SEALED_TAG)) {
				this.setSealed(nbt.getBoolean(SEALED_TAG));
			}
//			if (nbt.contains(UNDISCOVERED_TAG)) {
//				this.setUndiscovered(nbt.getBoolean(UNDISCOVERED_TAG));
//			}
			if (nbt.contains(LOOT_TABLE_TAG)) {
				if (!nbt.getString(LOOT_TABLE_TAG).isEmpty()) {
					this.setLootTable(new ResourceLocation(nbt.getString(LOOT_TABLE_TAG)));
				}
			}
			if (nbt.contains(GENERATION_CONTEXT_TAG)) {
				CompoundTag contextTag = nbt.getCompound(GENERATION_CONTEXT_TAG);
				Optional<IRarity> rarity = null;
				IChestGeneratorType genType = null;
				if (contextTag.contains(LOOT_RARITY_TAG)) {
					// TODO should NOT use Rarity.getByValue but look at Registry
//					rarity = Rarity.getByValue(contextTag.getString(LOOT_RARITY_TAG));
					rarity = TreasureApi.getRarity(contextTag.getString(LOOT_RARITY_TAG));
				}
				if (contextTag.contains(CHEST_GENERATOR_TYPE_TAG)) {
					genType = ChestGeneratorType.valueOf(contextTag.getString(CHEST_GENERATOR_TYPE_TAG).toUpperCase());
				}
				
				GenerationContext generationContext = this.new GenerationContext(rarity.orElse(Rarity.NONE), genType);
				this.setGenerationContext(generationContext);
			}	
		} catch (Exception e) {
			Treasure.LOGGER.error("error reading Properties from nbt:", e);
		}
	}

	@Override
	public boolean isLocked() {
		return hasLocks();
	}

	/**
	 * 
	 */
	@Override
	public boolean hasLocks() {
		// TODO TEMP do this for now. should have another property numActiveLocks so
		// that the renderer doesn't keep calling this
		if (getLockStates() == null || getLockStates().isEmpty()) {
			return false;
		}
		for (LockState state : getLockStates()) {
			if (state.getLock() != null)
				return true;
		}
		return false;
	}

	@Override
	public Component getName() {
		return this.name != null ? this.name : this.getDefaultName();
	}

	@Override
	public Component getDisplayName() {
		return this.getName();
	}

	@Override
	public Component getDefaultName() {
		return new TranslatableComponent(LangUtil.screen("default_chest.name"));
	}

	@Override
	public Component getCustomName() {
		return this.name;
	}

	public void setCustomName(Component name) {
		this.name = name;
	}

	/**
	 * Sync client and server states
	 */
	@Override
	public void sendUpdates() {		
		BlockState blockState = level.getBlockState(getBlockPos());
		level.sendBlockUpdated(getBlockPos(), blockState, blockState, 3);
		setChanged();
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		saveAdditional(tag);
		return tag;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		if (tag != null) {
			load(tag);
		}
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		CompoundTag tag = pkt.getTag();
		handleUpdateTag(tag);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		instanceHandler.invalidate();
	}

	@Override
	public List<LockState> getLockStates() {
		return lockStates;
	}

	@Override
	public void setLockStates(List<LockState> lockStates) {
		this.lockStates = lockStates;
	}

	@Override
	public Direction getFacing() {
		return facing;
	}	
	public void setFacing(Direction facing) {
		this.facing = facing;
	}
	@Override
	public void setFacing(int facingIndex) {
		this.facing = Direction.from3DDataValue(facingIndex);		
	}

	@Override
	public boolean isSealed() {
		return sealed;
	}
	@Override
	public void setSealed(boolean sealed) {
		this.sealed = sealed;
	}

	@Override
	public ResourceLocation getLootTable() {
		return lootTable;
	}

	@Override
	public void setLootTable(ResourceLocation lootTable) {
		this.lootTable = lootTable;
	}
	

	@Override
	public GenerationContext getGenerationContext() {
		return generationContext;
	}

	@Override
	public void setGenerationContext(GenerationContext generationContext) {
		this.generationContext = generationContext;
	}
	
	/*
	 * 
	 */
	public class GenerationContext {
		/*
		 * The rarity level of the loot that the chest will contain
		 */
		private IRarity lootRarity;

		private IChestGeneratorType chestGeneratorType;

		public GenerationContext(IRarity rarity, IChestGeneratorType chestGeneratorType) {
			this.lootRarity = rarity;
			this.chestGeneratorType = chestGeneratorType;
		}

		public GenerationContext(ResourceLocation lootTable, IRarity rarity, IChestGeneratorType chestGeneratorType) {
			// TODO move the loot table to this class
			AbstractTreasureChestBlockEntity.this.lootTable = lootTable;
			this.lootRarity = rarity;
			this.chestGeneratorType = chestGeneratorType;
		}

		public IRarity getLootRarity() {
			return lootRarity;
		}

		public IChestGeneratorType getChestGeneratorType() {
			return chestGeneratorType;
		}

		public ResourceLocation getLootTable() {
			return AbstractTreasureChestBlockEntity.this.lootTable;
		}

	}
}
