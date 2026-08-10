# Axiom Sovereign

The `axiom-sovereign` engine provides the high-performance parser for the Axiom ecosystem. It is a zero-dependency, structural assembly line designed for direct conversion of raw streams into persistent data structures.

## 🏛️ Integration

Summon the Sovereign engine into your project:

**Maven**

```xml
<dependency>
    <groupId>com.ensemblu</groupId>
    <artifactId>axiom-sovereign</artifactId>
    <version>2.0.0</version>
</dependency>
```

**Gradle**

```groovy
implementation("com.ensemblu:axiom-sovereign:2.0.0")
```

## ⚖️ Sovereign Law

This engine operates on the foundational principles of Data-Oriented Programming.

-   **Normalization**: All input streams processed by `AxiomDopParser` must be pre-validated for structural compliance.

-   **Immutability**: The output is a `PersistentMap`—a locked, immutable structural node. Any further mutation must be handled via the Axiom command cycle.

-   **Zero-Heap**: The parser is optimized for low-latency traversal. Do not attempt to re-instantiate the engine; use the static entry point.


## ⚡ Operational Entry

Access the assembly line via the static `parse` gate:


```java
import com.ensemblu.axiom.sovereign.parser.AxiomDopParser;

// Direct execution
final var result = AxiomDopParser.take(input).openBuffer().parse();
```
## 📜 Legal

This project is governed by the principles of immutable software architecture. See `LICENSE.md` for the specific terms of use.
