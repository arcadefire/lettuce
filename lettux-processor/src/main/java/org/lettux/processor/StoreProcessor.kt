package org.lettux.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.ksp.writeTo

@OptIn(ExperimentalKotlinPoetApi::class)
class StoreProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols: Sequence<KSClassDeclaration> = resolver
            .getSymbolsWithAnnotation("org.lettux.processor.Slice")
            .filterIsInstance<KSClassDeclaration>()

        if (!symbols.iterator().hasNext()) return emptyList()

        val containerType = ClassName("org.angmarc.whale", CLASS_NAME)
        val mainStoreFunction = FunSpec
            .builder("store")
            .contextReceivers(listOf(containerType))
            .returns(
                ClassName("org.lettux.core", "Store")
                    .parameterizedBy(
                        ClassName(
                            "org.angmarc.whale.root",
                            "AppState"
                        )
                    )
            )
            .addStatement("return storeFactory.get(CoroutineScope(Dispatchers.Main.immediate))")
            .build()

        val accessorFunSpecBuilders: MutableList<FunSpec> = mutableListOf()
        val storeContainerClass =
            generateStoreContainer(containerType, accessorFunSpecBuilders, symbols)

        val fileSpec = FileSpec.builder("org.angmarc.whale", CLASS_NAME)
            .addImport("kotlinx.coroutines", "CoroutineScope")
            .addImport("kotlinx.coroutines", "Dispatchers")
            .addType(storeContainerClass.build())
            .addFunction(mainStoreFunction)

        accessorFunSpecBuilders.forEach {
            fileSpec.addFunction(it)
        }

        fileSpec.build().writeTo(
            codeGenerator,
            dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray())
        )

        return emptyList()
    }

    private fun generateStoreContainer(
        containerType: ClassName,
        accessorFunSpecBuilders: MutableList<FunSpec>,
        symbols: Sequence<KSClassDeclaration>
    ): TypeSpec.Builder {
        val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")
        val contentLambda = LambdaTypeName.get(
            parameters = listOf(
                ParameterSpec
                    .builder("", type = containerType)
                    .build()
            ),
            returnType = UNIT,
        ).copy(
            annotations = listOf(
                AnnotationSpec.builder(composableAnnotation).build()
            )
        )
        val contentFun = FunSpec.builder("Content")
            .addAnnotation(composableAnnotation)
            .contextReceivers(listOf(containerType))
            .addParameter(
                ParameterSpec
                    .builder("content", contentLambda)
                    .build()
            )
            .addStatement("content(this)")
            .build()

        val storeFactoryType = ClassName(
            "org.lettux.core",
            "StoreFactory",
        ).parameterizedBy(
            ClassName("org.angmarc.whale.root", "AppState")
        )
        val storeFactoryProperty = PropertySpec
            .builder(
                "storeFactory",
                type = storeFactoryType
            )
            .initializer("storeFactory")
            .build()

        val typeSpec = TypeSpec.classBuilder(CLASS_NAME)
        val constructorSpec = FunSpec.constructorBuilder()
            .addAnnotation(ClassName("javax.inject", "Inject"))
            .addParameter(ParameterSpec("storeFactory", storeFactoryType))

        symbols.forEach {
            it.accept(
                Visitor(
                    containerType,
                    typeSpec,
                    constructorSpec,
                    accessorFunSpecBuilders
                ),
                Unit
            )
        }

        val storeContainerClass = typeSpec
            .primaryConstructor(constructorSpec.build())
            .addProperty(storeFactoryProperty)
            .addFunction(contentFun)

        return storeContainerClass
    }

    inner class Visitor(
        private val containerType: ClassName,
        private val specBuilder: TypeSpec.Builder,
        private val constructorBuilder: FunSpec.Builder,
        private val accessorFunSpecBuilders: MutableList<FunSpec>
    ) : KSVisitorVoid() {

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            if (classDeclaration.classKind != ClassKind.CLASS) {
                logger.error("Only classes can be annotated with @Slice", classDeclaration)
                return
            }

            val annotation: KSAnnotation = classDeclaration.annotations.first {
                it.shortName.asString() == "Slice"
            }

            val parentDeclaration = annotation.parent as KSDeclaration
            val annotatedClassName = parentDeclaration.simpleName.asString()
            val propertyNameSuffix =
                annotatedClassName.first().lowercaseChar() + annotatedClassName.substring(1)
            val propertyName = "${propertyNameSuffix}Slicer"

            val slicerClassName = ClassName(
                "org.angmarc.whale",
                "Slicer"
            ).parameterizedBy(
                ClassName("org.angmarc.whale.root", "AppState"),
                ClassName(
                    parentDeclaration.packageName.asString(),
                    parentDeclaration.simpleName.asString()
                ),
            )

            constructorBuilder.addParameter(ParameterSpec(propertyName, slicerClassName))

            specBuilder.addProperty(
                PropertySpec
                    .builder(
                        propertyName,
                        type = slicerClassName
                    )
                    .initializer(propertyName)
                    .build()
            )

            accessorFunSpecBuilders.add(
                FunSpec
                    .builder("${propertyNameSuffix}Store")
                    .contextReceivers(listOf(containerType))
                    .returns(
                        ClassName("org.lettux.core", "Store")
                            .parameterizedBy(
                                ClassName(
                                    parentDeclaration.packageName.asString(),
                                    parentDeclaration.simpleName.asString(),
                                )
                            )
                    )
                    .addStatement("return ${propertyName}.provideSlice(store())")
                    .build()
            )
        }
    }

    companion object {
        const val CLASS_NAME = "StoreContainer"
    }
}