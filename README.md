
# ⚔️ Axiom Sovereign

![Version](https://img.shields.io/badge/version-2.0.0-blue)
![Java](https://img.shields.io/badge/Java-26-orange)
![Depends](https://img.shields.io/badge/depends%20on-axiom-informational)
![License](https://img.shields.io/badge/license-Limited%20Commercial-red)

**High-performance, byte-native structural parser for the Axiom ecosystem.**

`axiom-sovereign` converts raw UTF-8 byte streams into immutable `PersistentMap` structures with zero intermediate `String` allocations where possible. It is the assembly line every schema-aware and language-level component feeds from.

---

## What it does

- Parses Axiom DOP (Data-Oriented Protocol) documents **natively from `byte[]`**
- Emits locked, immutable `PersistentMap` / nested structures
- Enforces structural compliance at the boundary
- Zero reflection, zero annotations, zero third-party JSON libraries

---

## Requirements

- **Java 26**
- [`axiom`](https://github.com/ensemblu-corp/axiom) `2.0.0` (pulled transitively)

---

## Installation

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

---

## Quick start (2.0.0 API)

```java
import com.ensemblu.axiom.sovereign.parser.AxiomDopParser;
import java.nio.charset.StandardCharsets;

byte[] raw = """
    {
      name: "Ada"
      age: 36
    }
    """.getBytes(StandardCharsets.UTF_8);

var data = AxiomDopParser.take(raw)
    .openBuffer()
    .parse();
```

> [!IMPORTANT]
> **Breaking change from 1.0.0**  
> `AxiomDopParser.take(String)` has been replaced by `take(byte[])`.  
> Always pass UTF-8 bytes. Do not re-instantiate the engine — use the static entry point.

---

## Sovereign law

| Principle | Meaning |
|-----------|---------|
| **Normalization** | Input must be structurally sound; the parser validates as it walks |
| **Immutability** | Output is a frozen `PersistentMap` — further change goes through the Axiom command cycle |
| **Byte-native** | Operates on raw bytes to avoid intermediate string allocations |
| **Static entry** | Single static gate: `AxiomDopParser.take(byte[])` |

---

## Package structure

```
com.ensemblu.axiom.sovereign
└── parser
    └── AxiomDopParser.java    // take(byte[]) → Initial → parse()
```

---

## How it fits in the stack

```text
raw bytes
    │
    ▼
AxiomDopParser.take(byte[])     ← this module
    │
    ▼
PersistentMap
    │
    ├── SchemaGuard (axiom-language)
    ├── application logic (axiom)
    └── materializers / binders (axiom-spec)
```

---

## Design notes

- String materialisation happens only when a value is actually needed (`new String(src, start, len, UTF_8)`).
- Peek / next operate on `byte`; structural tokens are compared as `(byte) '{'`, etc.
- Empty content and missing opening `{` are rejected early with clear structural failures.

---

## Related modules

| Module | Relationship |
|--------|----------------|
| `axiom` | Provides `PersistentMap`, `Dop`, `Result` |
| `axiom-language` | Consumes this parser via `SchemaGuard` |
| `axiom-spec` | Sibling parsers for CSV / JSON / SQL |

---

## Legal

Limited Commercial License — free for evaluation, testing, and non-commercial development.  
Commercial or production use requires a paid annual contract from Ensemblu Corp.

See `LICENSE.md`. Contact: **contact@ensemblu.com**
