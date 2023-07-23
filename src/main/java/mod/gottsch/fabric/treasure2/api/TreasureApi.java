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
package mod.gottsch.fabric.treasure2.api;

import mod.gottsch.fabric.gottschcore.enums.IEnum;
import mod.gottsch.fabric.gottschcore.enums.IRarity;
import mod.gottsch.fabric.treasure2.core.item.IKeyLockCategory;
import mod.gottsch.fabric.treasure2.core.registry.EnumRegistry;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;



/**
 *
 * @author Mark Gottschling on Nov 10, 2022
 *
 */
public class TreasureApi {
    public static final String RARITY = "rarity";
    public static final String KEY_LOCK_CATEGORY = "keyLockCategory";
    public static final String GENERATOR_TYPE = "generatorType";
    public static final String CHEST_GENERATOR_TYPE = "chestGeneratorType";
    public static final String REGION_PLACEMENT = "regionPlacement";
    @Deprecated
    public static final String SPECIAL_LOOT_TABLE = "specialLootTable";

    public static final String LOOT_TABLE_TYPE = "lootTableType";
    public static final String FEATURE_TYPE = "featureType";
    public static final String STRUCTURE_CATEGORY = "structureCategory";
    public static final String STRUCTURE_TYPE = "structureType";


    /**
     *
     * @param rarity
     */
    public static void registerRarity(IRarity rarity) {
        EnumRegistry.register(RARITY, rarity);
    }

    /**
     *
     * @param key
     * @return
     */
    public static Optional<IRarity> getRarity(String key) {
        IEnum ienum = EnumRegistry.get(RARITY, key);
        if (ienum == null) {
            return Optional.empty();
        }
        else {
            return Optional.of((IRarity) ienum);
        }
    }

    public static List<IRarity> getRarities() {
        List<IEnum> enums = EnumRegistry.getValues(RARITY);
        return enums.stream().map(e -> (IRarity)e).collect(Collectors.toList());
    }

    /**
     *
     * @param category
     */
    public static void registerKeyLockCategory(IKeyLockCategory category) {
        EnumRegistry.register(KEY_LOCK_CATEGORY, category);
    }

    /**
     *
     * @param key
     * @return
     */
    public static Optional<IKeyLockCategory> getKeyLockCategory(String key) {
        IEnum ienum = EnumRegistry.get(KEY_LOCK_CATEGORY, key);
        if (ienum == null) {
            return Optional.empty();
        }
        else {
            return Optional.of((IKeyLockCategory) ienum);
        }
    }
}
