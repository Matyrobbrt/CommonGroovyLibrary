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

package io.github.groovymc.cgl.transform.registroid.blockitem

import io.github.groovymc.cgl.transform.TransformUtils
import io.github.groovymc.cgl.transform.registroid.RegistrationName
import io.github.groovymc.cgl.transform.registroid.RegistroidASTTransformer
import io.github.groovymc.cgl.transform.registroid.RegistroidAddon
import groovy.transform.CompileStatic
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.tools.GeneralUtils
import org.objectweb.asm.Opcodes

import java.util.function.Supplier

@CompileStatic
class BlockItemAddonTransformer implements RegistroidAddon {
    public static final ClassNode ANNOTATION_TYPE = ClassHelper.make(BlockItemAddon)
    public static final ClassNode BLOCK_ITEM_TYPE = ClassHelper.make(BlockItem)
    public static final ClassNode REGISTRATION_NAME = ClassHelper.make(RegistrationName)
    public static final ClassNode BLOCK_TYPE = ClassHelper.make(Block)
    public static final ClassNode ITEM_PROPS_TYPE = ClassHelper.make(Item.Properties)
    public static final ClassNode ITEM_TYPE = ClassHelper.make(Item)

    @Override
    void process(AnnotationNode registroidAnnotation, ClassNode targetClass, PropertyNode property, RegistroidASTTransformer transformer, Supplier<String> modId) {
        final myAnnotation = targetClass.annotations.find { it.classNode == ANNOTATION_TYPE }
        final propertyAnnotation = property.annotations.find { it.classNode == ANNOTATION_TYPE }
        if (propertyAnnotation?.members?.get('value') === null && myAnnotation.getMember('value') === null) return

        if (propertyAnnotation !== null && transformer.getMemberValue(propertyAnnotation, 'exclude')) return

        final propertyRegName = transformer.getRegName(property)
        if (targetClass.properties.any { it.type.isDerivedFrom(ITEM_TYPE) && transformer.getRegName(it) == propertyRegName }) return

        Expression creationStatement = GeneralUtils.callX(targetClass, 'makeBlockItem', GeneralUtils.callX(property.declaringClass, property.getterNameOrDefault))
        final propertySpecificProps = propertyAnnotation?.members?.get('value') as ClosureExpression
        if (propertySpecificProps !== null) {
            if (propertySpecificProps.parameters.size() === 1) {
                creationStatement = GeneralUtils.castX(BLOCK_ITEM_TYPE, GeneralUtils.callX(propertySpecificProps,
                        'call', GeneralUtils.callX(property.declaringClass, property.getterNameOrDefault)))
            } else {
                creationStatement = GeneralUtils.ctorX(BLOCK_ITEM_TYPE, GeneralUtils.args(
                        GeneralUtils.callX(property.declaringClass, property.getterNameOrDefault),
                        GeneralUtils.castX(ITEM_PROPS_TYPE, GeneralUtils.callX(propertySpecificProps, 'call'))
                ))
            }
        }

        targetClass.methods.find {
            it.name == 'makeBlockItem' && it.returnType == BLOCK_ITEM_TYPE && it.parameters.size() == 1
        } ?: TransformUtils.addMethod(
                targetClassNode: targetClass,
                methodName: 'makeBlockItem',
                modifiers: Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC,
                returnType: BLOCK_ITEM_TYPE,
                parameters: new Parameter[] {
                    GeneralUtils.param(BLOCK_TYPE, 'block')
                },
                conditionalCode: {
                    final clos = myAnnotation.members['value'] as ClosureExpression
                    if (clos.parameters.size() === 1) {
                        return GeneralUtils.stmt(GeneralUtils.castX(BLOCK_ITEM_TYPE, GeneralUtils.callX(
                                clos, 'call', GeneralUtils.varX('block', BLOCK_TYPE)
                        )))
                    } else {
                        return GeneralUtils.stmt(GeneralUtils.ctorX(BLOCK_ITEM_TYPE, GeneralUtils.args(
                                GeneralUtils.varX('block', BLOCK_TYPE),
                                GeneralUtils.castX(ITEM_PROPS_TYPE, GeneralUtils.callX(clos, 'call'))
                        )))
                    }
                }
        )
        final prop = property.declaringClass.addProperty(
                "\$BLOCK_ITEM_${property.name}",
                TransformUtils.CONSTANT_MODIFIERS,
                BLOCK_ITEM_TYPE,
                creationStatement,
                null, null
        )
        final regNameAnn = new AnnotationNode(REGISTRATION_NAME)
        regNameAnn.setMember('value', GeneralUtils.constX(transformer.getInitialRegName(property)))
        prop.addAnnotation(regNameAnn)
    }

    @Override
    List<ClassNode> getSupportedTypes() {
        return List.of(BLOCK_TYPE)
    }

    @Override
    List<PropertyExpression> getRequiredRegistries() {
        return [registryKeyProperty('ITEM')]
    }
}
