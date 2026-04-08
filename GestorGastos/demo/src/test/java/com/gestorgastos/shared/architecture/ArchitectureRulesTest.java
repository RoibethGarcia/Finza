package com.gestorgastos.shared.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.gestorgastos", importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeJars.class})
class ArchitectureRulesTest {

	@ArchTest
	static final ArchRule domainShouldNotDependOnSpring = noClasses()
		.that().resideInAnyPackage("..domain..")
		.should().dependOnClassesThat().resideInAnyPackage("org.springframework..");

	@ArchTest
	static final ArchRule apiShouldNotDependOnInfrastructure = noClasses()
		.that().resideInAnyPackage("..api..")
		.should().dependOnClassesThat().resideInAnyPackage("..infrastructure..");

	@ArchTest
	static final ArchRule topLevelModulesShouldBeFreeOfCycles = slices()
		.matching("com.gestorgastos.(*)..")
		.should().beFreeOfCycles();
}
