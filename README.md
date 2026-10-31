# sunat-xbuilder-java

Construcción, validación y firma de comprobantes XML UBL 2.1 para SUNAT (Perú).

> Este proyecto es un fork mantenido independientemente del módulo XBuilder de
> [Project OpenUBL / xhandler](https://github.com/project-openubl/xhandler).
> Su base inicial corresponde a `5.1.2-SNAPSHOT` (POM padre
> `io.github.project-openubl:xhandler-parent:5.1.2-SNAPSHOT`). Agradecemos a
> Carlos Esteban Feria Vila y a Project OpenUBL por el trabajo original que hizo
> posible este punto de partida.
>
> El fork conserva la licencia Apache 2.0 (ver [LICENSE](LICENSE)), mantiene su
> código fuente abierto y continuará evolucionando de forma independiente. No es
> un proyecto afiliado ni respaldado oficialmente por Project OpenUBL. La
> atribución distribuible se conserva también en [NOTICE](NOTICE).

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

La primera distribución debe ser un snapshot de una rama o commit, no un release.
JitPack compila el repositorio Git remoto bajo demanda; antes de solicitarlo, el
commit debe estar publicado en GitHub y el repositorio debe ser público (o usar la
configuración de repositorios privados de JitPack).

`jitpack.yml` usa Java 17, genera Maven Wrapper 3.9.9 en el entorno efímero de
JitPack y ejecuta:

```bash
mvn -N wrapper:wrapper -Dmaven=3.9.9
./mvnw -B -U clean install -Ppublication -DskipTests
```

El perfil `publication` adjunta los JAR de fuentes y Javadoc. JitPack no requiere
una librería, plugin de despliegue, claves GPG ni `distributionManagement`.

Para probar un snapshot sin crear un release, abre el repositorio en JitPack y
solicita el commit corto o la rama:

```text
https://jitpack.io/#max-dicson-cf/sunat-xbuilder-java
```

Usa un hash de commit para una prueba inmutable o `main-SNAPSHOT` para seguir la
rama principal. JitPack mostrará las coordenadas Maven definitivas del build; usa
esas coordenadas en los consumidores en vez de asumir las de una futura release.

Todo snapshot debe superar localmente antes:

```bash
mvn -U clean test
mvn -U clean install -Ppublication
```

### Consumo desde JitPack

Agrega el repositorio:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

## Atribución

* Autor original: Carlos Esteban Feria Vila y el proyecto Project OpenUBL.
* Repositorio fuente: https://github.com/project-openubl/xhandler
* Este repositorio es un fork/derivado que conserva la licencia Apache 2.0 y da
  mantenimiento independiente al módulo XBuilder.
