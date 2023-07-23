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
package mod.gottsch.fabric.treasure2.core.block;

import mod.gottsch.fabric.gottschcore.enums.IRarity;
import mod.gottsch.fabric.treasure2.core.lock.LockLayout;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

/**
 * 
 * @author Mark Gottschling on Nov 15, 2022
 *
 */
public interface ITreasureChestBlock {
	public static final EnumProperty<Direction> FACING = EnumProperty.of("facing", Direction.class);
	public static final Property<Boolean> UNDISCOVERED = BooleanProperty.of("undiscovered");

	/**
	 * Convenience method.
	 * @param state
	 * @return
	 */
	public static Direction getFacing(BlockState state) {
		return state.get(FACING);
	}
	
	public static Boolean getUndiscovered(BlockState state) {
		return state.get(UNDISCOVERED);
	}
	
	Class<?> getBlockEntityClass();

	LockLayout getLockLayout();

	// TODO is this a thing still in the ChestBlock or registered elsewhere?
	IRarity getRarity();

	// TODO review - shouldn't need these in general
	// maybe create a method createBounds() that take a functional interface instead.
	VoxelShape[] getBounds();

	ITreasureChestBlock setBounds(VoxelShape[] bounds);
}