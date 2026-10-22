# AGENTS.md — sunat-xbuilder-java

Contexto para agentes de OpenCode que trabajen en este repositorio.

## Propósito

Librería Java pura para construir, validar y firmar comprobantes XML UBL 2.1 para SUNAT (Perú).
Código extraído del monorepo `project-openubl/xhandler` (versión 5.1.2-SNAPSHOT).

## Cómo construir y probar

```bash
mvn -U clean test   # tests unitarios locales, sin red
mvn clean package
mvn clean install
```

Resultado esperado en JDK 17:

```text
Tests run: 211, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

> El entorno actual solo tiene JDK 17. Antes de publicar tags, validar con JDK 21.

## Estructura del proyecto

```text
src/main/java/io/github/project/openubl/xbuilder/
├── content/        # Modelos de datos de entrada y modelos JAXB de salida
│   ├── models/     # POJOs públicos: Invoice, CreditNote, DebitNote, etc.
│   └── jaxb/       # Clases JAXB generadas/mapeadas para deserializar el XML renderizado
├── enricher/       # Reglas de negocio que completan/corrigen datos antes de renderizar
│   └── kie/        # Mini motor de reglas interno (RuleUnit, RuleFactory, etc.)
├── renderer/       # Motor de plantillas Qute (TemplateProducer, EngineProducer)
└── signature/      # Firma XML con certificado digital (XMLSigner, CertificateDetails)

src/main/resources/templates/   # Plantillas Qute XML (UBL) por tipo de documento
src/test/java/e2e/              # Tests de renderer y enriquecedor
src/test/resources/             # Snapshots XML y esquemas XSD
```

## Convenciones importantes

* **Paquetes y namespaces**: no cambiar los paquetes Java (`io.github.project.openubl.xbuilder.**`) salvo necesidad demostrada. Romperían compatibilidad con consumidores existentes.
* **Modelos públicos**: las clases en `content.models.*` son la API pública. Mantener estabilidad de campos y builders.
* **Plantillas XML**: están en `src/main/resources/templates/`. Usan sintaxis Qute (`{objeto.campo}`, `{#each}`, `{#if}`).
* **Snapshots de tests**: los tests comparan el XML generado contra archivos en `src/test/resources/**`. Para regenerarlos:
  `mvn clean test -Dxbuilder.snapshot.update=true`

## Dependencias clave

* `io.quarkus.qute:qute-core` — motor de plantillas. **No es Quarkus runtime**, solo la librería de templates. Es imprescindible para renderizar XML.
* `org.mapstruct:mapstruct` — mapeo entre modelos JAXB y modelos de entrada.
* `org.projectlombok:lombok` — generación de builders/getters (scope provided).
* `jakarta.xml.bind:jakarta.xml.bind-api` + `com.sun.xml.bind:jaxb-impl` — JAXB.
* `com.fasterxml.jackson.core:jackson-databind` — JSON.

## Qué NO cambiar sin consultar

* Nombres de paquetes de la API pública.
* Estructura de los modelos `Invoice`, `CreditNote`, `DebitNote`, `DespatchAdvice`, `VoidedDocuments`, `SummaryDocuments`, `Perception`, `Retention`.
* Nombres de las plantillas en `TemplateProducer` (`getInvoice()`, `getCreditNote()`, etc.).
* Algoritmo de firma XML en `signature.XMLSigner`.

## Tests

* Tests locales, sin red. No envían documentos a SUNAT.
* Los tests de integración contra SUNAT fueron eliminados de este proyecto; pertenecen a `sunat-xsender-java`.

## Notas para el agente

* Si necesitas reemplazar Qute por otro motor de plantillas, es un cambio grande. Propón plan antes de ejecutar.
* Los warnings de MapStruct sobre propiedades no mapeadas son esperados y no bloquean el build.
* No agregues dependencias de Spring Boot, Quarkus runtime ni starters en el artefacto publicado.
* No uses certificados ni claves SOL reales en los tests.
