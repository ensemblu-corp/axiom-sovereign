![Version](https://img.shields.io/badge/version-3.0.0-blue)
![Java](https://img.shields.io/badge/Java-26-orange)
![Depends](https://img.shields.io/badge/depends%20on-axiom-informational)
![License](https://img.shields.io/badge/license-Limited%20Commercial-red)

# Axiom Sovereign (`axiom-sovereign`)

> The sovereign assembly line: a zero-dependency, off-heap parser for Axiom's DOP (Data-Oriented Programming)
> notation — the `.axiom` format used for schemas, journeys and declarative configuration.

`axiom-sovereign` turns raw bytes held in a Panama `MemorySegment` into an immutable
`PersistentMap<String, Object>`. It never builds an intermediate heap `String` of the whole document and never uses
reflection. It is the grammar layer that `axiom-language` (schemas) sits on.

---

## Architecture overview

```
 .axiom file / bytes
        │  (memory-mapped or copied into an Arena)
        ▼
 MemorySegment ──► AxiomDopParser.take(segment)      ── Initial
                       .openBuffer()                  ── Final
                       .parse()                       ──► PersistentMap<String, Object>
                                                           (values normalised via Dop.normalize)
```

| Layer | Responsibility |
|---|---|
| `AxiomDopParser` (public) | Curried entry point: `take → openBuffer → parse`. |
| `NativeEngine` (private) | Single-pass cursor over the segment. Reads bytes with `ValueLayout.JAVA_BYTE`, only materialising the small slices that become keys and values. |

Dependency graph:

```
axiom  ◄──  axiom-sovereign  ◄──  axiom-language
```

---

## Installation

**Maven**

```xml
<dependency>
    <groupId>com.ensemblu</groupId>
    <artifactId>axiom-sovereign</artifactId>
    <version>3.0.0</version>
</dependency>
```

This transitively brings in `com.ensemblu:axiom:3.0.0`.

**Gradle (Kotlin DSL)**

```kotlin
implementation("com.ensemblu:axiom-sovereign:3.0.0")
```

JPMS automatic module name: `com.ensemblu.axiom.sovereign`

Runtime: JDK 26 with `--enable-preview`.

---

## The DOP notation

A DOP document is a braced object of `key: value` pairs. It is deliberately looser than JSON so that
hand-written schemas stay readable.

| Construct | Example | Parsed as |
|---|---|---|
| Object | `{ a: 1, b: 2 }` | `PersistentMap<String, Object>` |
| Array | `[ACTIVE, SUSPENDED]` | `PersistentList<Object>` |
| Bare symbol | `ACTIVE` | `String` |
| Quoted string | `"^.+@.+[.].+$"` | `String` (content taken verbatim, no escape processing) |
| Triple-quoted string | `"""multi-line SQL"""` | `String` (verbatim, may span lines) |
| Number | `320.0`, `5`, `-3` | Normalised number via `Dop.normalize` |
| Boolean / null | `true`, `false`, `null` | `Boolean` / `null` (case-insensitive) |

Rules worth knowing:

- **Keys are bare symbols** — no quotes needed: `minLength: 5`.
- **Commas are optional between entries on separate lines**: a value ends at `,`, `}`, `]` or a line break.
- Symbols are trimmed; anything that is not a number, boolean or `null` stays a `String`.
- Use quotes (or triple quotes) for values containing `:`, `,`, braces or brackets.

Example:

```
{
  strict: true
  required: [status, limit]
  properties: {
    status: { type: string, enum: [ACTIVE, INACTIVE, SUSPENDED] },
    limit:  { type: integer, min: 1, max: 1000 }
  }
  query: """
      SELECT * FROM accounts
      WHERE status = :java.status
  """
}
```

---

## Usage examples

### Parse a classpath resource

```java
import com.ensemblu.axiom.core.data_structure.map.PersistentMap;
import com.ensemblu.axiom.sovereign.parser.AxiomDopParser;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

try (var in = getClass().getClassLoader().getResourceAsStream("schemas/account.axiom");
     var arena = Arena.ofConfined()) {

    byte[] bytes = in.readAllBytes();
    MemorySegment segment = arena.allocate(bytes.length);
    segment.copyFrom(MemorySegment.ofArray(bytes));

    PersistentMap<String, Object> doc = AxiomDopParser.take(segment).openBuffer().parse();

    boolean strict = doc.targetKey("strict").toBooleanResult().getOrElse(false);
}
```

### Memory-map a file on disk (zero copy)

```java
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

try (var channel = FileChannel.open(Path.of("config/rules.axiom"), StandardOpenOption.READ);
     var arena = Arena.ofConfined()) {

    var segment = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size(), arena);
    var rules = AxiomDopParser.take(segment).openBuffer().parse();
}
```

### Parse an in-memory string

```java
var segment = MemorySegment.ofArray("{ mode: fast, retries: 3 }".getBytes(StandardCharsets.UTF_8));
var cfg = AxiomDopParser.take(segment).openBuffer().parse();
int retries = cfg.targetKey("retries").toIntVal();
```

> **Arena lifetime:** the returned map holds its own copies of keys and values, so it remains valid after the
> arena that held the source bytes is closed.

---

## Configuration guide

`axiom-sovereign` has no configuration of its own. Behaviour to be aware of:

| Situation | Behaviour |
|---|---|
| Empty input | Returns an empty map |
| Input whose first non-whitespace byte is not `{` | Returns an empty map |
| Structural error (missing `:` / `]` etc.) | Throws `RuntimeException` with byte position and a 30-byte context window |
| Encoding | UTF-8 |

If you want structural errors as values, wrap the call:

```java
Result<PersistentMap<String, Object>> doc =
        Axiom.Check.attempt(() -> AxiomDopParser.take(segment).openBuffer().parse());
```

For validating **data against** a DOP schema, use [`axiom-language`](../axiom-language) (`SchemaGuard`).

---

## License

**Limited Commercial License** — free for evaluation, testing, and non-commercial development.
Commercial or production use requires a paid annual contract.
See [LICENSE](https://raw.githubusercontent.com/ensemblu-corp/axiom/main/LICENSE) · Contact: contact@ensemblu.com

© Ensemblu Corp
