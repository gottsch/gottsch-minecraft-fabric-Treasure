package mod.gottsch.fabric.treasure2.core.setup;

import mod.gottsch.fabric.treasure2.api.TreasureApi;
import mod.gottsch.fabric.treasure2.core.Treasure;
import mod.gottsch.fabric.treasure2.core.config.ConfigModel;
import mod.gottsch.fabric.treasure2.core.item.KeyLockCategory;
import mod.gottsch.fabric.treasure2.core.rarity.Rarity;
import mod.gottsch.fabric.treasure2.core.rarity.SpecialRarity;
import mod.gottsch.fabric.treasure2.core.rarity.WishableExtraRarity;

/**
 * Created by Mark Gottschling on 5/18/2023
 */
public class Registration {

    /**
     *
     */
    public static void register() {
        // setup rolling file appender
        ConfigModel.instance.addRollingFileAppender(Treasure.MOD_ID);

        /**
         * Most resources in Treasure2 are associated with a Rarity. Register rarities
         * to enable them in other features. The registry, in conjunction with
         * the IRarity interface allows extensibility with addon mods.
         * Always perform a check against the registry to determine if
         * the rarity is allowed.
         */
        // register rarities
        TreasureApi.registerRarity(Rarity.COMMON);
        TreasureApi.registerRarity(Rarity.UNCOMMON);
        TreasureApi.registerRarity(Rarity.SCARCE);
        TreasureApi.registerRarity(Rarity.RARE);
        TreasureApi.registerRarity(Rarity.EPIC);
        TreasureApi.registerRarity(Rarity.LEGENDARY);
        TreasureApi.registerRarity(Rarity.MYTHICAL);
        TreasureApi.registerRarity(Rarity.SKULL);
        TreasureApi.registerRarity(SpecialRarity.GOLD_SKULL);
        TreasureApi.registerRarity(SpecialRarity.CRYSTAL_SKULL);
        TreasureApi.registerRarity(SpecialRarity.CAULDRON);
        TreasureApi.registerRarity(SpecialRarity.WITHER);
        TreasureApi.registerRarity(WishableExtraRarity.WHITE_PEARL);
        TreasureApi.registerRarity(WishableExtraRarity.BLACK_PEARL);

        // register the key/lock categories
        TreasureApi.registerKeyLockCategory(KeyLockCategory.ELEMENTAL);
        TreasureApi.registerKeyLockCategory(KeyLockCategory.METALS);
        TreasureApi.registerKeyLockCategory(KeyLockCategory.GEMS);
        TreasureApi.registerKeyLockCategory(KeyLockCategory.MOB);
        TreasureApi.registerKeyLockCategory(KeyLockCategory.WITHER);
    }
}
