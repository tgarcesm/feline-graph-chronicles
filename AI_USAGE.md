# Uso de IA en el proyecto

Este documento registra de forma honesta el uso de herramientas de inteligencia artificial durante el desarrollo de **The Feline Graph Chronicles**.

El uso de LLMs fue utilizado como apoyo para analizar requisitos, desarrollar, depurar, probar e integrar partes del proyecto. Las respuestas generadas no se asumieron automáticamente como correctas: se contrastaron con el enunciado, los contratos reales del repositorio, los samples oficiales y las pruebas automatizadas.

---

## Herramientas utilizadas

| Herramienta | Parte del proyecto | Integrante |
|---|---|---|
| ChatGPT (OpenAI) | Análisis del enunciado y de la guía, implementación y revisión de BFS, DFS, Disjoint Set y Kruskal, integración de las Misiones 1 y 4, GridRenderer, tests JUnit, Maven, Git y revisión de requisitos. | Samuel |
| PENDIENTE | Tomas debe indicar qué herramienta de IA utilizó y para qué partes. | Tomas |
| PENDIENTE | Sebastian debe indicar qué herramienta de IA utilizó y para qué partes. | Sebastian |

### Uso realizado por Samuel

ChatGPT se utilizó principalmente como apoyo durante las siguientes actividades:

- Análisis detallado del enunciado original y de la guía de trabajo.
- Planeación de la arquitectura y separación entre algoritmos, misiones y GUI.
- Implementación y revisión de `Bfs.java`.
- Implementación y revisión de `Dfs.java`.
- Implementación de `Mission1Minefield.java`.
- Implementación y revisión de `DisjointSet.java`.
- Implementación y revisión de `Kruskal.java`.
- Implementación de `Mission4Network.java`.
- Implementación de `GridRenderer.java`.
- Creación y ampliación de `Mission1Test.java` y `Mission4Test.java`.
- Diagnóstico de problemas relacionados con Maven en IntelliJ y PowerShell.

El flujo de trabajo utilizado fue:

1. Revisar primero el requisito del profesor.
2. Proponer o generar una implementación.
3. Compararla con el código real del repositorio.
4. Compilar y ejecutar tests.
5. Comparar los resultados con los samples oficiales.
6. Corregir cualquier diferencia antes de integrar el código.

---

## Prompts decisivos

### 1. Análisis completo del trabajo antes de programar

**Prompt:**

> "Quiero que la analices muy bien, que entiendas muy bien la forma de trabajar y los requisitos con los que debe cumplir la entrega."

**Por qué se necesitó:**

El trabajo tenía muchos requisitos simultáneos y era necesario convertirlos en tareas concretas antes de comenzar a programar.

El análisis permitió identificar aspectos importantes como:

- DFS no recursivo.
- Orden obligatorio del DFS: arriba, abajo, izquierda, derecha.
- Límites máximos de entrada.
- Límites obligatorios de visualización.
- Uso de `long` para costos acumulados.
- Kruskal apoyado en Union-Find.
- Path compression y union by size.
- Separación entre algoritmos y JavaFX.
- Uso de samples oficiales como tests.

**Qué se hizo con la respuesta:**

Se utilizó como plan inicial, pero posteriormente cada punto importante se volvió a verificar contra el PDF original del profesor.



---

## Casos en que la salida generada estuvo mal o fue subóptima

### 1. Se asumió inicialmente que Maven estaba disponible mediante el comando `mvn`

**Problema:**

Inicialmente se indicó ejecutar:

```text
mvn -q clean verify
```

PowerShell respondió que `mvn` no era un comando reconocido porque Maven no estaba instalado globalmente en el PATH de Windows.

**Cómo se detectó:**

La terminal mostró un `CommandNotFoundException` al intentar ejecutar `mvn`.

**Cómo se corrigió:**

Se revisó la configuración de Maven dentro de IntelliJ y se encontró la instalación Bundled Maven del IDE. Después se ejecutó directamente:

```text
C:\Program Files\JetBrains\IntelliJ IDEA 2025.1.3\plugins\maven\lib\maven3\bin\mvn.cmd
```

Con esta corrección Maven ejecutó la compilación y las pruebas correctamente y produjo `BUILD SUCCESS`.

**Aprendizaje obtenido:**

Una instrucción generada por IA puede ser conceptualmente correcta pero no necesariamente compatible con el entorno local donde se ejecutará.

### 2. El repositorio oficial se clonó inicialmente dentro del proyecto provisional

**Problema:**

El primer `git clone` se ejecutó cuando la terminal seguía ubicada dentro de la carpeta del proyecto temporal `ParcialCompi`.

Esto produjo una estructura equivalente a:

```text
ParcialCompi/
    feline-graph-chronicles/
```

lo que dejaba un repositorio dentro de otro proyecto y podía generar confusión con IntelliJ, ramas y commits.

**Cómo se detectó:**

Se revisó la ruta mostrada por PowerShell y se observó:

```text
C:\Users\samue\IdeaProjects\ParcialCompi\feline-graph-chronicles
```

**Cómo se corrigió:**

El clon recién creado se eliminó y se volvió a clonar desde:

```text
C:\Users\samue\IdeaProjects
```

quedando finalmente:

```text
C:\Users\samue\IdeaProjects\feline-graph-chronicles
```

Después se verificó el repositorio con `git status` antes de seguir trabajando.

### 3. Trabajar inicialmente sin el repositorio real hacía subóptimo asumir contratos

**Problema:**

Al comienzo Samuel todavía no tenía acceso al repositorio oficial. Se creó un proyecto temporal para adelantar BFS, DFS, Disjoint Set y Kruskal.

Esto era útil para los algoritmos independientes, pero crear las clases completas de misión en ese momento habría requerido inventar o asumir interfaces pertenecientes al trabajo de otros integrantes.

**Cómo se detectó:**

La guía indicaba que clases como `Tokenizer`, `Mission`, `MissionResult`, `CaseResult`, `GridDrawing` y `GraphDrawing` tenían contratos compartidos que debían respetarse.

**Cómo se corrigió:**

Se decidió no crear versiones alternativas de esos contratos.

Cuando el repositorio estuvo disponible:

1. Se clonó el proyecto oficial.
2. Se revisaron las clases reales.
3. Se conservaron los algoritmos independientes ya probados.
4. Se implementaron las clases de misión directamente contra los contratos reales.

Esto evitó mantener dos arquitecturas incompatibles.

---

## Qué aprendió cada integrante

### Samuel

Durante este proyecto aprendí y reforcé varios conceptos:

- Por qué BFS garantiza un camino mínimo en grafos no ponderados.
- Por qué DFS puede producir un camino válido pero no necesariamente óptimo.
- Por qué un DFS sobre una grilla de hasta un millón de celdas debe ser iterativo para evitar `StackOverflowError`.
- Cómo el funcionamiento LIFO de una pila obliga a insertar vecinos en orden inverso para obtener un orden de exploración específico.
- Cómo el momento exacto en que un nodo se marca como visitado puede modificar el árbol generado por DFS.
- Cómo representar una grilla mediante un arreglo plano usando `row * cols + col`.
- Cómo funciona Disjoint Set o Union-Find.
- Qué hacen path compression y union by size y cómo mejoran el costo amortizado.
- Cómo Kruskal utiliza Union-Find para construir un Minimum Spanning Tree.
- Por qué los costos acumulados del proyecto deben manejarse con `long`.
- Cómo separar un núcleo algorítmico de la GUI para permitir pruebas automatizadas independientes de JavaFX.
- Cómo utilizar Maven para validar un proyecto completo.
- Cómo trabajar con ramas y Pull Requests en un proyecto colaborativo.
- Cómo comprobar código generado o sugerido por IA contra requisitos, tests y resultados oficiales en lugar de asumir que es correcto.

### Tomas

**PENDIENTE:** Tomas debe documentar qué aprendió durante el proyecto y que no sabía previamente.

### Sebastian

**PENDIENTE:** Sebastian debe documentar qué aprendió durante el proyecto y que no sabía previamente.

---

## Verificación del trabajo apoyado por IA

Para la parte desarrollada por Samuel se realizaron verificaciones concretas antes de integrarla:

- Se utilizaron los samples oficiales como resultados esperados.
- La Misión 1 produjo exactamente `Case #1: BFS 18 DFS 32`.
- La Misión 4 produjo exactamente `Case #1: 55`.
- Se probaron inputs inválidos y casos borde.
- Se probó una grilla de hasta `1000 x 1000` para validar el DFS iterativo.
- Se comprobaron situaciones con lazos y cables repetidos en Kruskal.
- Se comprobó el uso de `long` en costos acumulados.
- Se ejecutaron los tests mediante Maven.
- Las ramas de Samuel se integraron localmente y se volvió a ejecutar la batería de pruebas antes de crear los Pull Requests.

El uso de IA se tomó como una herramienta de apoyo para acelerar el análisis, generar propuestas y detectar problemas. La salida generada se consideró una propuesta que debía verificarse antes de formar parte del proyecto.

---


