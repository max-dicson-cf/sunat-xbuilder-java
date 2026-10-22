# sunat-xbuilder-java

Construcción, validación y firma de comprobantes XML UBL 2.1 para SUNAT (Perú).

> Código extraído del monorepo [project-openubl/xhandler](https://github.com/project-openubl/xhandler).
> Versión de origen: `5.1.2-SNAPSHOT` (POM padre `io.github.project-openubl:xhandler-parent:5.1.2-SNAPSHOT`).
> Licencia: Apache 2.0 (ver [LICENSE](LICENSE)).

## Alcance

Esta librería contiene únicamente el núcleo de **XBuilder**: modelos de contenido,
enriquecedor de reglas, renderizado de plantillas XML (Qute), firma XML y utilidades
para generar facturas, boletas, notas de crédito/débito, guías de remisión,
comunicaciones de baja, resúmenes, percepciones y retenciones.

**No incluye**: extensiones Quarkus/Spring Boot, starters, ejemplos de aplicación ni
integraciones de servidor. Es una librería Java pura instanciable directamente.

## Requisitos

* Java 17+ (el objetivo final es Java 21; el código ya usa APIs modernas).
* Apache Maven 3.8+.

> **Nota sobre JDK 21**: el entorno de trabajo actual disponible es JDK 17, por lo
> que el build fue validado con JDK 17. Antes de publicar con JDK 21 se recomienda
> ejecutar `mvn clean test` con JDK 21 y actualizar plugins/dependencias si fuera
> necesario.

## Compilación

```bash
mvn -U clean test
mvn clean package
mvn clean install
```

Resultado observado en este entorno (JDK 17, Maven 3.8.1):

```text
Tests run: 211, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## API pública principal

* `io.github.project.openubl.xbuilder.content.models.standard.general.Invoice`
* `io.github.project.openubl.xbuilder.content.models.standard.general.CreditNote`
* `io.github.project.openubl.xbuilder.content.models.standard.general.DebitNote`
* `io.github.project.openubl.xbuilder.content.models.standard.guia.DespatchAdvice`
* `io.github.project.openubl.xbuilder.content.models.sunat.baja.VoidedDocuments`
* `io.github.project.openubl.xbuilder.content.models.sunat.resumen.SummaryDocuments`
* `io.github.project.openubl.xbuilder.content.models.sunat.percepcionretencion.Percepcion`
* `io.github.project.openubl.xbuilder.content.models.sunat.percepcionretencion.Retention`
* `io.github.project.openubl.xbuilder.enricher.ContentEnricher`
* `io.github.project.openubl.xbuilder.renderer.TemplateProducer`
* `io.github.project.openubl.xbuilder.signature.XMLSigner`

## Ejemplo mínimo

```java
import io.github.project.openubl.xbuilder.content.models.common.Cliente;
import io.github.project.openubl.xbuilder.content.models.common.Proveedor;
import io.github.project.openubl.xbuilder.content.models.standard.general.DocumentoVentaDetalle;
import io.github.project.openubl.xbuilder.content.models.standard.general.Invoice;
import io.github.project.openubl.xbuilder.enricher.ContentEnricher;
import io.github.project.openubl.xbuilder.enricher.config.DateProvider;
import io.github.project.openubl.xbuilder.enricher.config.Defaults;
import io.github.project.openubl.xbuilder.renderer.TemplateProducer;
import io.quarkus.qute.Template;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Ejemplo {
    public static void main(String[] args) {
        Defaults defaults = Defaults.builder()
                .igvTasa(new BigDecimal("0.18"))
                .icbTasa(new BigDecimal("0.2"))
                .ivapTasa(new BigDecimal("0.04"))
                .build();
        DateProvider dateProvider = () -> LocalDate.of(2024, 1, 1);

        Invoice input = Invoice.builder()
                .serie("F001")
                .numero(1)
                .proveedor(Proveedor.builder()
                        .ruc("12345678901")
                        .razonSocial("Proveedor S.A.C.")
                        .build())
                .cliente(Cliente.builder()
                        .nombre("Cliente")
                        .numeroDocumentoIdentidad("12345678")
                        .tipoDocumentoIdentidad("1")
                        .build())
                .detalles(java.util.Collections.singletonList(DocumentoVentaDetalle.builder()
                        .descripcion("Producto")
                        .cantidad(new BigDecimal("1"))
                        .precio(new BigDecimal("100"))
                        .igvTipo("GRAVADO_OPERACION_ONEROSA")
                        .build()))
                .build();

        new ContentEnricher(defaults, dateProvider).enrich(input);

        Template template = TemplateProducer.getInstance().getInvoice();
        String xml = template.data(input).render();
        System.out.println(xml);
    }
}
```

## Tests

Los tests son locales y no envían documentos a SUNAT. Los tests de integración
contra SUNAT beta fueron eliminados de esta librería; pertenecen a
`sunat-xsender-java`.

## Publicación / JitPack

Coordenadas Maven:

```xml
<dependency>
    <groupId>io.github.project-openubl</groupId>
    <artifactId>sunat-xbuilder-java</artifactId>
    <version>5.1.2-SNAPSHOT</version>
</dependency>
```

Para JitPack se incluye `jitpack.yml` con JDK 21. **No ha sido probado con JDK 21
en este entorno** por falta de JDK 21 instalado; sí compila correctamente con JDK 17.

```yaml
jdk: openjdk21
install:
  - ./mvnw -B clean install -DskipTests
```

Si falla en JitPack con JDK 21, prueba primero con `openjdk17` en `jitpack.yml`.

## Atribución

* Autor original: Carlos Esteban Feria Vila y el proyecto Project OpenUBL.
* Repositorio fuente: https://github.com/project-openubl/xhandler
* Este repositorio es un fork/derivado que conserva la licencia Apache 2.0.
