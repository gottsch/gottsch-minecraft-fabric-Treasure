/*
 * This file is part of  Treasure2.
 * Copyright (c) 2023 Mark Gottschling (gottsch)
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
package mod.gottsch.fabric.treasure2.core;

import mod.gottsch.fabric.treasure2.core.config.MyConfig;
import mod.gottsch.fabric.treasure2.core.setup.Registration;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mark Gottschling on May 18, 2023. Fabric version.
 */
public class Treasure implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger(Treasure.MOD_ID);
	public static final String MOD_ID = "treasure2";

	public static final MyConfig CONFIG = MyConfig.createAndLoad();

	@Override
	public void onInitialize() {
		Registration.register();
	}
}
