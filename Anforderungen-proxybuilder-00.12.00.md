# Anforderungen — proxybuilder 00.11.x Follow-Up / 00.12.00

Audit-Ergebnis nach Release 00.11.00. Quellen: vollständiger Durchgang
durch `BasicAnnotationProcessor`, alle Subprocessors, `DynamicProxyBuilder`,
`DynamicProxyGenerator`, `VirtualDynamicProxyInvocationHandler`, alle
`ServiceStrategyFactory*`-Implementierungen, `RapidPMMetricsRegistry`,
`AdapterBuilder`, `DynamicObjectAdapterAnnotationProcessor`, das
`proxybuilder-annotations`-Modul, Pitest-Baseline (annotations 100 %,
impl 50 %, 117 NO_COVERAGE-Mutationen).

Stand: 2026-05-28. Branch: `develop` (Head `a26c286 Release proxybuilder
00.11.00`).

Jeder Eintrag trägt eine ID, eine kurze Beschreibung, einen
Severity-Tag (`[!]` Bug, `[T]` Test-Lücke, `[A]` Architektur,
`[F]` Feature, `[P]` Polish) und — wo sinnvoll — Hinweise zur
Umsetzung und Akzeptanzkriterien.

---

## A. Korrektheits-Risiken (Bugs)

### A1. `[!]` `SecurityRule` blockiert mit `return null`

Datei: `impl/src/main/java/com/svenruppert/proxybuilder/proxy/dymamic/DynamicProxyBuilder.java:188-208`

Bei Methoden mit primitivem Returntyp (`int`, `boolean`, …) wirft das
Auto-Unboxing `NullPointerException`, statt einen sauberen Zugriffs­fehler
zu liefern. Symptom: ein blockierter `int`-Aufruf endet im NPE statt in
einer erkennbaren Security-Exception.

Empfehlung: `SecurityException` werfen oder das blockierte Verhalten
konfigurierbar machen (z. B. `addSecurityRule(SecurityRule rule,
BlockedBehavior behavior)` mit `THROW` / `RETURN_NULL` / `RETURN_DEFAULT`).
README-Eintrag „If the rule returns `false`, the invocation is blocked"
anpassen.

Akzeptanz: Regressionstest mit primitivem Returntyp, der blocked-Path
nimmt — muss eine prüfbare Exception werfen statt NPE.

### A2. `[!]` Metrics-Histogramm überlebt Exceptions nicht

Datei: `DynamicProxyBuilder.java:129-152`

`buildMetricsProxy` misst `start = System.nanoTime()`, ruft die Methode
auf, misst `stop` *nach* dem Aufruf. Wirft die proxierte Methode, wird
`histogram.update(...)` nie erreicht — Fehler-Latenzen sind in der
Statistik unsichtbar.

Empfehlung: `try { result = method.invoke(...); } finally {
histogram.update(stop - start); }` oder zwei Histogramme (`*.success`,
`*.error`).

Akzeptanz: Test mit absichtlich werfender Methode bestätigt, dass das
Histogramm trotzdem einen Eintrag bekommt (oder dass ein dediziertes
Error-Histogramm geschrieben wird).

### A3. `[!]` `DynamicProxyBuilder.build()` ist nicht idempotent

Datei: `DynamicProxyBuilder.java:95-127`

`Collections.reverse(preActionList)` und `Collections.reverse(
securityRules)` mutieren die Felder. Ein zweiter `build()`-Aufruf am
selben Builder produziert ein anderes Ergebnis und stapelt zusätzliche
Proxy-Schichten.

Empfehlung: Build defensive Kopien oder Builder zu One-Shot machen
(Flag setzen, beim zweiten Aufruf `IllegalStateException`).

Akzeptanz: Test mit zweimaligem `build()` — entweder gleich oder
explizit verboten.

### A4. `[!]` `DynamicObjectAdapterAnnotationProcessor` ignoriert die neuen Annotationen

Datei: `impl/src/main/java/com/svenruppert/proxybuilder/objectadapter/DynamicObjectAdapterAnnotationProcessor.java:79-117`

Eigener `process()`-Override umgeht `applyProxyBuilderOptions`,
`@SkipProxy`, `@ProxyBuilderOptions`, `@ProxyName`. Konsumenten von
`@DynamicObjectAdapterBuilder` bekommen die 00.11.00-Features nicht.
Vermutlich gilt dasselbe für `StaticObjectAdapterAnnotationProcessor`.

Empfehlung: gemeinsame Filter- und Optionsapplikations-Methoden in die
Basisklasse heben oder vom Adapter-Processor explizit aufrufen.

Akzeptanz: `@SkipProxy`/`@ProxyBuilderOptions`/`@ProxyName`-Tests für
beide Object-Adapter-Processors grün.

### A5. `[!]` Unchecked Casts in `DynamicProxyBuilder`

Datei: `DynamicProxyBuilder.java:73, 99, 185`

`(Class<I>) original`, `(T) clazz.cast(nextProxy)`,
`dynamicProxyBuilder = new DynamicProxyBuilder()` (raw type) — keine
`@SuppressWarnings`, kein Compiler-Hinweis, semantisch fragwürdig.
Insbesondere ist das JDK-Proxy-Objekt keine Instanz von `T` (Impl-Klasse).

Empfehlung: Generic-Signaturen reparieren oder die Implementations-
Typvariable `T` entfernen, weil sie nach `Proxy.newProxyInstance` nicht
mehr inhaltlich gilt.

Akzeptanz: `mvn compile` ohne `unchecked`-Warnungen in dieser Klasse.

### A6. `[!]` `Class.newInstance()` ist deprecated

Datei: `impl/src/main/java/com/svenruppert/proxybuilder/proxy/dymamic/virtual/factory/DefaultConstructorServiceFactory.java:29`

Compile-Log zeigt: `newInstance() in java.lang.Class ist veraltet`.

Empfehlung: Auf `getDeclaredConstructor().newInstance()` umstellen,
`ReflectiveOperationException` propagieren oder in eine eigene
Exception wrappen.

Akzeptanz: Keine `[deprecation]`-Warnung mehr für diese Datei.

### A7. `[!]` `RapidPMMetricsRegistry.getMetrics()` ist nicht synchronisiert

Datei: `impl/src/main/java/com/svenruppert/proxybuilder/RapidPMMetricsRegistry.java:38-43`

Der `LOCK` wird nur in Reporter-Start/Stop benutzt. `metrics` selbst
ist `final` und damit safe published, aber der Vertrag ist nicht
dokumentiert. Falls Tests parallel Reporter starten/stoppen, kann es
weiter unten zu Race-Conditions kommen.

Empfehlung: Memoryref-Semantik dokumentieren oder `metrics` als
`volatile` und im `LOCK` zugreifen.

Akzeptanz: Klar dokumentierter Concurrency-Vertrag (Javadoc).

---

## B. Code-Qualität / Tote Stellen

### B1. `[P]` `//TODO refactoring` in `DynamicObjectAdapterAnnotationProcessor`

`DynamicObjectAdapterAnnotationProcessor.java:25`. Klasse mit
nummerierten `HolderStep001`/`HolderStep002`-Inner-Classes und
auskommentierten Zuweisungen. Refactoring überfällig.

### B2. `[P]` `//TODO Define as Lambda`

`BasicAnnotationProcessor.java:607, 621`. Beide Methoden sind bereits
Stream-basiert; das TODO ist veraltet — entfernen.

### B3. `[P]` Auskommentierte Zeilen entfernen

- `DynamicProxyBuilder.java`: `//buildPreActionProxy(preAction);`,
  `//buildPostActionProxy(postAction);`, `//.withPostActions(postActionList)`
- `VirtualDynamicProxyInvocationHandler.java`: `// invoke(obj, method, args);`
- `DynamicObjectAdapterAnnotationProcessor.java`:
  `// typeSpecBuilderForTargetClass = typeSpecBuilder;`, `// .build();`

### B4. `[P]` `//missing`-Kommentare neben implementierten Cases

`DynamicProxyGenerator.java:62, 68`. Strategien `SOME_DUPLICATES` und
`NO_DUPLICATES` sind längst implementiert — Kommentare löschen.

### B5. `[P]` Deutsche Inline-Kommentare aufräumen

- `DynamicProxyBuilder.java`: `// wo die Metriken ablegen ?`,
  `// die originalReihenfolge behalten in der die Methoden aufgerufen worden sind.`
- `DynamicObjectAdapterAnnotationProcessor.java`: `// nun alle Delegator Methods`

Empfehlung: offene Fragen in Issues überführen, erklärende
Kommentare ins Englische übersetzen oder löschen, wenn der Code
selbsterklärend ist.

### B6. `[P]` „RapidPM"-Namens-Altlast

`RapidPMMetricsRegistry` stammt aus der RapidPM-Org. Umbenennen zu
`ProxyBuilderMetricsRegistry` und einen `@Deprecated`-Alias für eine
Übergangsversion bereitstellen, falls Konsumenten das Symbol direkt
benutzen.

### B7. `[P]` Doppelte `MethodIdentifier`-Klasse

- `com.svenruppert.proxybuilder.MethodIdentifier` (Record, jetzt `@Internal`)
- `com.svenruppert.proxybuilder.objectadapter.dynamic.MethodIdentifier`

Klären, ob das wirklich zwei verschiedene Konzepte sind. Wenn nicht,
deduplizieren.

### B8. `[P]` `@Override` fehlt

`VirtualDynamicProxyInvocationHandler.invoke(...)` (Zeile 34)
implementiert `InvocationHandler.invoke`, hat aber keine
`@Override`-Annotation.

---

## C. Test-Lücken (Pitest NO_COVERAGE = 117)

### C1. `[T]` `DynamicProxyBuilder` ungetestet

Min. ein Test pro Feature: Pre-Action, Post-Action, SecurityRule
(allow/deny + Primitive-Return-Bug aus A1), Logging, Metrics,
Builder-Reihenfolge. Spezifisch wichtig: Test für A1 (Block-Verhalten
bei primitivem Returntyp) und A2 (Metrics-bei-Exception).

### C2. `[T]` Strategy-Factories ungetestet

Alle fünf `ServiceStrategyFactory*` ohne Tests. Insbesondere:
- `ServiceStrategyFactoryNoDuplicates` (Double-Checked-Locking) — Concurrency-Test.
- `ServiceStrategyFactorySomeDuplicates` (AtomicReference CAS) — Concurrency-Test.
- `ServiceStrategyFactorySynchronized`, `Method­Scoped`, `NotThreadSafe` — funktionaler Singleton-Test.

### C3. `[T]` `VirtualDynamicProxyInvocationHandler` ungetestet

Mindestens: Methode wird korrekt deleg­iert, Exception-Pfad
(`InvocationTargetException → cause`).

### C4. `[T]` `DynamicProxyGenerator.createStrategyFactory()` ungetestet

Fünf Switch-Cases, kein Test. Mapping `CreationStrategy → Factory-Class`
verifizieren.

### C5. `[T]` `DefaultConstructorServiceFactory.createInstance()` ungetestet

Im Zuge der Migration aus A6 mittesten.

### C6. `[T]` `RapidPMMetricsRegistry` ungetestet

JMX/Console-Reporter Start/Stop, Idempotenz (zweimaliges Start ist
no-op), Re-Start nach Stop, korrektes `close()`.

### C7. `[T]` `AdapterBuilder.buildForTarget()` ungetestet

Test: Proxy delegiert an den hinterlegten InvocationHandler.

### C8. `[T]` `DynamicObjectAdapterAnnotationProcessor` nur oberflächlich

Existierender Test prüft nur `contains`-Substring. Generierte
Builder-Semantik (newBuilder, setOriginal, withX) verifizieren.

### C9. `[T]` `@SkipProxy` auf vererbten Methoden

Kein Test. Verhalten heute: `Element.getAnnotation(SkipProxy.class)`
sieht keine vererbten Annotationen → Methode wird trotzdem generiert.
Test sollte den Status-quo dokumentieren (oder bestätigt eine
gewünschte Verhaltens­änderung — siehe D3).

### C10. `[T]` Überlebende Mutationen analysieren

55 SURVIVED-Mutationen in `BasicAnnotationProcessor` und Co. Jeden
einzelnen anschauen, jeweils entscheiden:
- Test ergänzen → Mutation killen,
- als äquivalent markieren (Pitest-`ignoredMutations`),
- ist tatsächlich ein Bug.

---

## D. Architektur-Lücken

### D1. `[A]` Trigger- und Marker-Annotationen sind noch im `proxybuilder`-JAR

Datei: `impl/src/main/java/com/svenruppert/proxybuilder/proxy/generated/annotations/`

Betroffen: `@StaticLoggingProxy`, `@StaticMetricsProxy`,
`@StaticVirtualProxy`, `@StaticObjectAdapter`, `@DynamicObjectAdapterBuilder`,
`@IsGeneratedProxy`, `@IsLoggingProxy`, `@IsMetricsProxy`,
`@IsVirtualProxy`.

Konsumenten der eingebauten Trigger brauchen `proxybuilder` deshalb
weiterhin auf dem Compile-Classpath — der `DownstreamSmokeTest` deckt
nur den Custom-Processor-Fall.

Empfehlung: Annotationen ins `proxybuilder-annotations`-Modul
verschieben. `impl/module-info.java` exportiert das Package dann nicht
mehr; `proxybuilder-annotations/module-info.java` exportiert es.

Akzeptanz: Eine neue Variante von `DownstreamSmokeTest`, die mit dem
eingebauten `@StaticLoggingProxy` arbeitet, generiert Wrapper ohne
`com.svenruppert.proxybuilder.*`-Import außerhalb `.annotations.*`.
Bricht für Konsumenten — Major-Bump (00.12.00 oder breaking-Note in
einer Minor).

### D2. `[A]` Object-Adapter-Processors inkonsistent mit Static-Proxy-Familie

Verknüpft mit A4: nicht nur die neuen Annotationen werden ignoriert,
auch die alte `validateMethodForProxyGeneration`-Logik läuft nicht.
Adapter-Processors leben strukturell parallel zur restlichen
Processor-Hierarchie.

Empfehlung: Gemeinsame Basis vereinheitlichen (`Basic*Processor` →
gemeinsame `process()`-Schleife mit Hooks für Validation und
Code-Generierung).

### D3. `[A]` `@SkipProxy` greift nicht ererbungsweise

Heute prüft der Processor nur `methodElement.getAnnotation(SkipProxy.class)`,
was vererbte Annotationen ignoriert. Entscheidung nötig:
- Soll `@SkipProxy` ererbungsweise wirken (semantisch ähnlich zu
  `@Inherited`)?
- Oder ist die heutige Per-Element-Semantik gewollt?

Wenn ererbungsweise: über die existierenden Vererbungs-Walks (`getSuperclass`,
`getInterfaces`) propagieren.

### D4. `[A]` `@ProxyName(value)` wird nicht validiert

Empfehlung:
- leerer Wert → Compile-Error,
- fehlender `{Original}`-Platzhalter → optional Warnung (literal-Name
  ist evtl. gewollt),
- Kollisionen (zwei Typen → gleicher Wrapper-Name) → Compile-Error.

Akzeptanz: Tests pro Fall, Diagnostics über `Messager`.

### D5. `[A]` `@WrappedBy(X)` nicht zur Compile-Time geprüft

Heute reines Runtime-Risiko: `X` muss den annotierten Typ erweitern,
sonst wirft `ProxyEnforcement.requireWrapped` erst spät.

Empfehlung: Eigener kleiner Annotation-Processor in
`proxybuilder-annotations`, der `@WrappedBy(X)`-Annotation auf Typ `T`
prüft: `X` muss Subtyp von `T` sein.

Akzeptanz: Compile-Time-Fehler bei falscher Verwendung.

### D6. `[A]` `RapidPMMetricsRegistry` Singleton ohne DI-Punkt

Test-Isolation schwer; mehrere `DynamicProxyBuilder`-Instanzen teilen
denselben Registry. Empfehlung: optionaler Konstruktor-Parameter
`DynamicProxyBuilder.withMetricRegistry(MetricRegistry)`; Default
bleibt der Singleton.

### D7. `[A]` Pre/Post-Action-Fehlerverhalten undokumentiert

Heute: wirft eine `PreAction`, wird die Methode nicht aufgerufen. Wirft
eine `PostAction`, geht das Original-Ergebnis verloren. Vertrag
dokumentieren — und evtl. konfigurierbar machen
(`OnActionFailure.PROPAGATE` vs. `OnActionFailure.SWALLOW`).

---

## E. Feature-Ideen

### E1. `[F]` `@ProxyBuilderOptions.includeMethodNames`

Whitelist-Gegenstück zu `excludeMethodNames`. Bei großen Klassen, wo
nur wenige Methoden proxiert werden sollen.

### E2. `[F]` `@ProxyBuilderOptions.targetPackage`

Wrapper in ein eigenes Sub-Package (`<orig>.generated`) statt das
Source-Package zu belegen.

### E3. `[F]` `@ProxyBuilderOptions.walkSuperclasses` / `walkInterfaces`

Opt-out aus der heutigen unbedingten Vererbungs-Suche.

### E4. `[F]` `ProxyEnforcement.wrapperOf(Class<?>)`

Umkehr-Lookup zu `@WrappedBy`.

### E5. `[F]` Compile-Time `@WrappedBy`-Check

Siehe D5.

### E6. `[F]` `@DelegatesTo` mit Exceptions/Returntype

Erweitertes Format: `"FQN#name(params) throws Foo,Bar : ReturnType"`
für besseres Tooling.

### E7. `[F]` `@SkipProxy(reason)` als generated Comment

Heute landet der Grund nur im verbose NOTE. In den Wrapper als `//`
Comment oder als `@GeneratedSource(skipped=…)` mit aufnehmen.

### E8. `[F]` `@ProxyEntry` mit semantischer Bedeutung

z. B. „Bootstrap before this method": Subprocessor liest die Annotation
und fügt CodeBlocks vor dem Delegator-Call ein. Heute reines NOTE.

### E9. `[F]` `@ProxyName` mit mehreren Platzhaltern

`{Original}`, `{Package}`, `{Suffix}`, `{Hash}` …

### E10. `[F]` `DynamicProxyBuilder` als immutable record-basierter Builder

Idempotent, defensive Kopien, neuer Builder pro `with*`-Aufruf.

### E11. `[F]` JaCoCo-Report zusätzlich zu Pitest

Schnelles Feedback ohne kompletten Mutationslauf; `pl impl,proxybuilder-annotations
jacoco:report`.

### E12. `[F]` GitHub-Actions-Workflow

Build + Test + Pitest auf PR/Push. Bereits in 00.10.00 als „not part of
this release" angekündigt.

### E13. `[F]` `ProxyEnforcement.metadataOf(Class<?>)`

Reflection-API: gibt `GeneratedByProxyBuilder` zurück, plus
`delegatesToOf(Method)` → JLS-Ref. Spart Konsumenten-Boilerplate.

### E14. `[F]` SpotBugs/Errorprone-Plugin für `@Internal`

Heute reiner Doku-Marker. Konsument-Code, der `@Internal`-markierte
APIs benutzt, sollte eine Compiler-Warnung kriegen.

### E15. `[F]` `@ProxyBuilderOptions.copyAnnotations`

Trigger-Klassen-Annotationen automatisch in den Wrapper übernehmen
(z. B. `@Singleton`, `@Path`).

---

## F. Dokumentation / Polish

### F1. `[P]` Migrations-Guide 00.10.x → 00.11.00

Vor allem die `GeneratedByProxyBuilder`-Verschiebung
(`com.svenruppert.proxybuilder` → `com.svenruppert.proxybuilder.annotations`)
und die Änderungen am Member-Schema (`value()` weg, neue Members da).

### F2. `[P]` `proxybuilder-annotations/README.md` Copy-Paste-Snippets

Tabelle hat Target/Retention/Tier; ein Code-Block pro Annotation
würde Konsumenten direkt zum Einsatz führen.

### F3. `[P]` Architekturdiagramm

Module + Konsumenten-Integration als ASCII-/Mermaid-Diagramm im
Haupt-README.

### F4. `[P]` Subprocessor-Spielanleitung erweitern

`beforeDelegation`/`aroundDelegation`/`afterDelegation` ist im README
knapp; ein vollständiges Beispiel (Security-Wrapper mit drei
Decorator-Annotationen) wäre wertvoll.

---

## Priorisierungs-Vorschlag (Minor-Bump 00.12.00)

1. **Korrektheit zuerst:** A1, A2, A3, A4, A6 (klare Bugs mit nutzer-
   sichtbarem Impact). A5 / A7 / D6 als Begleit-Aufräumung.
2. **Architektur als roten Faden:** D1 (Trigger-Annotationen ins
   annotations-Modul) — macht den Module-Split ehrlich. D2 (Adapter-
   Processors angleichen) hängt eng an A4.
3. **Test-Schuld abbauen:** C1, C2, C3 — kombiniert hebt das die
   Pitest-Score auf 75 %+. C10 als laufende Aufgabe.
4. **Features in 00.12.00 oder später:** E1, E2, E11, E12 sind
   billig und nützlich. E5/E13/E14 sind eigene größere Stories.
5. **Polish parallel:** B-Serie und F-Serie als „Begleit-Cleanup" in
   jedem PR mit­ziehen.

## Verifikations-Checkliste pro Anforderung

Für jeden umgesetzten Punkt:

- [ ] Tests existieren und decken den gefixten Pfad ab.
- [ ] Pitest läuft grün auf den geänderten Klassen
      (`./mvnw -pl impl org.pitest:pitest-maven:mutationCoverage`).
- [ ] README / Release-Notes erwähnen Verhaltens­änderungen.
- [ ] Bei Breaking Changes: Eintrag im Migrations-Guide F1.

---

## Annahmen / Constraints

- Kein CGLIB, ByteBuddy oder anderes Bytecode-Tooling — JDK-only bleibt.
- `proxybuilder-annotations` bleibt dependency-frei (außer
  `spotbugs-annotations:provided`).
- JDK 26 als Compile-Target.
- Maven 4 via Wrapper.
- Kein `Co-Authored-By:`-Footer in Commits.
