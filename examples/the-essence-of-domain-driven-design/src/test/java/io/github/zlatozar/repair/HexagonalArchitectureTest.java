package io.github.zlatozar.repair;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Entity;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;

/** Inside every module, dependencies point inward: infrastructure → application → domain. */
@AnalyzeClasses(
    packagesOf = InstrumentRepairApplication.class,
    importOptions = DoNotIncludeTests.class)
class HexagonalArchitectureTest {

  private static final String DOMAIN = "..internal.domain..";
  private static final String APPLICATION = "..internal.application..";
  private static final String PORTS = "..internal.application.port..";
  private static final String OUT_PORTS = "..internal.application.port.out..";
  private static final String SERVICES = "..internal.application.service..";
  private static final String INFRASTRUCTURE = "..internal.infrastructure..";
  private static final String WEB = "..internal.infrastructure.web..";
  private static final String SHARED_KERNEL = "..sharedkernel..";
  private static final String PUBLIC_EVENTS = "..events..";

  @ArchTest
  static final ArchRule domainIsPlainJava =
      classes()
          .that()
          .resideInAnyPackage(DOMAIN, SHARED_KERNEL)
          .and()
          .doNotHaveSimpleName("package-info")
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage("java..", DOMAIN, SHARED_KERNEL, "org.jmolecules..", "org.jspecify..")
          .because("the domain knows nothing about Spring, HTTP or the database");

  @ArchTest
  static final ArchRule applicationKnowsOnlyTheDomain =
      classes()
          .that()
          .resideInAPackage(APPLICATION)
          .and()
          .doNotHaveSimpleName("package-info")
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage(
              "java..",
              APPLICATION,
              DOMAIN,
              SHARED_KERNEL,
              "org.jmolecules..",
              "org.jspecify..",
              "org.springframework.stereotype..",
              "org.springframework.transaction.annotation..")
          .because(
              "use cases know nothing about HTTP, JSON or SQL; only @Service and @Transactional");

  @ArchTest
  static final ArchRule portsAreContracts =
      classes()
          .that()
          .resideInAPackage(PORTS)
          .and()
          .doNotHaveSimpleName("package-info")
          .should()
          .beInterfaces()
          .orShould()
          .beRecords()
          .because("a port is a contract: behaviour is an interface, data is a record");

  @ArchTest
  static final ArchRule publicEventsHidePrivateTypes =
      classes()
          .that()
          .resideInAPackage(PUBLIC_EVENTS)
          .and()
          .doNotHaveSimpleName("package-info")
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage(
              "java..", PUBLIC_EVENTS, SHARED_KERNEL, "org.jmolecules..", "org.jspecify..")
          .because("other modules read these events, so they must not leak the internal model");

  @ArchTest
  static final ArchRule webCallsUseCasesOnly =
      noClasses()
          .that()
          .resideInAPackage(WEB)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(OUT_PORTS, SERVICES)
          .orShould()
          .dependOnClassesThat()
          .areAnnotatedWith(AggregateRoot.class)
          .orShould()
          .dependOnClassesThat()
          .areAnnotatedWith(Entity.class)
          .because(
              "the web adapter calls use cases; it never loads or changes an aggregate itself");

  @ArchTest
  static final ArchRule modulesListenAfterCommit =
      methods()
          .that()
          .areMetaAnnotatedWith(EventListener.class)
          .should()
          .beAnnotatedWith(ApplicationModuleListener.class)
          .because(
              "a listener runs after the publisher commits, in its own transaction, and a failed"
                  + " event is kept");

  @ArchTest
  static final ArchRule dependenciesPointInward =
      layeredArchitecture()
          .consideringOnlyDependenciesInLayers()
          .layer("Domain")
          .definedBy(DOMAIN)
          .layer("Application")
          .definedBy(APPLICATION)
          .layer("Infrastructure")
          .definedBy(INFRASTRUCTURE)
          .whereLayer("Domain")
          .mayNotAccessAnyLayer()
          .whereLayer("Application")
          .mayOnlyAccessLayers("Domain")
          .whereLayer("Infrastructure")
          .mayOnlyAccessLayers("Application", "Domain");
}
