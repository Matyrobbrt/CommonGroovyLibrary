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

package io.github.groovymc.cgl.forge

import com.matyrobbrt.gml.GModContainer
import groovy.transform.CompileStatic
import io.github.groovymc.cgl.reg.forge.ForgeBusGetter
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModContainer

@CompileStatic
class GMLBusGetter implements ForgeBusGetter {
    @Override
    IEventBus getModEventBus(ModContainer modContainer) {
        if (modContainer instanceof GModContainer) {
            return modContainer.modBus
        }
        return null
    }
}
