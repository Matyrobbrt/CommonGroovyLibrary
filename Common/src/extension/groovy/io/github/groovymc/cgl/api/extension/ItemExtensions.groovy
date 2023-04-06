/*
 * Copyright (C) 2022 GroovyMC and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package io.github.groovymc.cgl.api.extension

import groovy.transform.CompileStatic
import net.minecraft.nbt.CompoundTag
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike

@CompileStatic
class ItemExtensions {
    static ItemStack count(ItemLike self, int count) {
        new ItemStack(self, count)
    }

    static ItemStack multiply(ItemLike self, int count) {
        new ItemStack(self, count)
    }

    static ItemStack multiply(Integer count, ItemLike item) {
        new ItemStack(item, count)
    }

    static ItemStack count(ItemStack self, int count) {
        self.setCount(count)
        return self
    }

    static ItemStack tag(ItemStack self, CompoundTag tag) {
        self.setTag(tag)
        return self
    }

    static ItemStack tag(ItemStack self, Object tag) {
        self.setTag(StaticNBTExtensions.from(null, tag) as CompoundTag)
        return self
    }

    static Ingredient ingredient(ItemLike item) {
        return Ingredient.of(item)
    }

    static Ingredient ingredient(TagKey<Item> tag) {
        return Ingredient.of(tag)
    }
}