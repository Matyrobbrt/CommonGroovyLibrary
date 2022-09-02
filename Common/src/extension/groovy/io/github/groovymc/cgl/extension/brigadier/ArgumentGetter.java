/*
 * Copyright (C) 2022 GroovyMC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <https://www.gnu.org/licenses/>.
 */

package io.github.groovymc.cgl.extension.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import groovy.lang.Closure;

import java.util.function.Function;

/**
 * An interface used by {@linkplain CommandExtensions#argument(ArgumentBuilder, String, ArgumentType, Function, Closure)} and the likes
 * in order to provide an easy way of retrieving arguments.
 *
 * @param <S> the type of the source
 * @param <Z> the type of the argument
 */
@FunctionalInterface
interface ArgumentGetter<S, Z> {
    /**
     * Gets the argument from the given {@code context}.
     *
     * @param context the context to retrieve the argument for
     * @return the argument
     */
    Z call(CommandContext<S> context);
}