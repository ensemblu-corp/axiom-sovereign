package com.ensemblu.axiom.sovereign.parser;

import com.ensemblu.axiom.api.Axiom;
import com.ensemblu.axiom.core.data_structure.list.PersistentList;
import com.ensemblu.axiom.core.data_structure.map.PersistentMap;

import static com.ensemblu.axiom.core.foundation.Dop.normalize;

/**
 * <h1>🛡️ AxiomDopParser</h1>
 * <p>
 * <b>⚖️ Structural Law:</b><br>
 * A zero-dependency, persistent-only assembly line. Operates on the premise
 * that the input stream is pre-validated for DOP structural compliance.
 * </p>
 */
public final class AxiomDopParser {

    private AxiomDopParser() {
        throw new AssertionError("Sovereign Engine: The constructor is sealed; structural integrity must be maintained.");
    }

    /**
     * ⚡ Entry Point.
     * Initiates the parsing sequence for a raw input stream.
     */
    public static Initial take(String content) {
        return () -> () -> NativeEngine.parse(content.trim());
    }

    public interface Initial {
        Final openBuffer();
    }

    public interface Final {
        PersistentMap<String, Object> parse();
    }

    private static final class NativeEngine {
        private final String src;
        private int pos = 0;

        private NativeEngine(String src) {
            this.src = src;
        }

        static PersistentMap<String, Object> parse(String src) {
            final var engine = new NativeEngine(src);
            engine.skipWhitespace();

            if (engine.hasNext() && engine.peek() == '{') {
                engine.consume('{');
                final var map = engine.parseObjectBody('}');
                engine.skipWhitespace();
                if (engine.hasNext() && engine.peek() == '}') {
                    engine.consume('}');
                }
                return map;
            }

            return engine.parseObjectBody(' ');
        }

        private PersistentMap<String, Object> parseObject() {
            consume('{');
            final var map = parseObjectBody('}');
            consume('}');

            return map;
        }

        private PersistentMap<String, Object> parseObjectBody(char endChar) {
            var map = Axiom.Data.<String, Object> emptyMap();

            while (hasNext() && peek() != endChar) {
                skipWhitespace();
                if (!hasNext() || peek() == endChar) break;

                final var key = parseSymbol();
                if (key.isEmpty()) break;

                skipWhitespace();
                consume(':');

                map = map.put(key, parseValue());

                skipWhitespace();
                if (hasNext() && peek() == ',') {
                    consume(',');
                    skipWhitespace();
                }
            }

            return map;
        }

        private String parseSymbol() {
            skipWhitespace();
            final var start = pos;

            while (hasNext()) {
                final var c = peek();

                if (":{},[]".indexOf(c) != -1) break;

                if (c == '\n' || c == '\r') break;

                pos++;
            }

            return src.substring(start, pos).trim();
        }

        private Object parseValue() {
            skipWhitespace();
            final var c = peek();
            return switch (c) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseQuotedString();
                default -> interpretSymbol(parseSymbol());
            };
        }

        private String parseQuotedString() {
            consume('"');
            var start = pos;

            while (hasNext() && peek() != '"') {
                pos++;
            }

            final var val = src.substring(start, pos);
            consume('"');

            return val;
        }

        private PersistentList<Object> parseArray() {
            var list = Axiom.Data.emptyList();
            consume('[');
            skipWhitespace();

            while (hasNext() && peek() != ']') {
                list = list.append(parseValue());
                skipWhitespace();
                if (hasNext() && peek() == ',') {
                    consume(',');
                    skipWhitespace();
                }
            }
            consume(']');

            return list;
        }

        private Object interpretSymbol(String s) {
            if (s.equalsIgnoreCase("true")) return true;
            if (s.equalsIgnoreCase("false")) return false;
            if (s.equalsIgnoreCase("null")) return null;
            if (s.equalsIgnoreCase("SUCCESS")) return "SUCCESS";
            if (s.equalsIgnoreCase("FAILURE")) return "FAILURE";

            try {
                final var d = Double.parseDouble(s);
                return normalize(d);
            } catch (NumberFormatException e) {
                return s;
            }
        }

        private void skipWhitespace() {
            while (hasNext() && Character.isWhitespace(peek())) pos++;
        }

        private char peek() { return src.charAt(pos); }

        private boolean hasNext() { return pos < src.length(); }

        private void consume(char c) {
            if (!hasNext()) throw new RuntimeException("Expected '" + c + "' but reached end");
            if (peek() != c) {
                throw new RuntimeException("Expected '" + c + "' but found '" + peek() + "' at pos " + pos +
                        ". Context: " + src.substring(Math.max(0, pos-15), Math.min(src.length(), pos+15)));
            }
            pos++;
        }
    }
}