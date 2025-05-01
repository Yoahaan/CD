import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class HandwrittenlexerC {

    public static class Token {
        public final int lineNumber;
        public final String lexeme;
        public final String tokenType;
        public final String tokenValue;

        public Token(int lineNumber, String lexeme, String tokenType, String tokenValue) {
            this.lineNumber = lineNumber;
            this.lexeme = lexeme;
            this.tokenType = tokenType;
            this.tokenValue = tokenValue;
        }

        @Override
        public String toString() {
            return String.format("(%d, %s, %s, %s)", lineNumber, lexeme, tokenType, tokenValue);
        }
    }

    public static class Lexer {
        // Ordered list for value lookup
        private static final List<String> TOKEN_VALUES = new ArrayList<>();
        static {
            TOKEN_VALUES.addAll(List.of(
                "WHITESPACE","OPERATOR","LBRACE","RBRACE","LPAREN","RPAREN",
                "LBRACKET","RBRACKET","SEMICOLON","COMMA","DOT",
                "STRING_LITERAL","CHAR_LITERAL","NUMBER","IDENTIFIER"
            ));
            // Then reserved words in order
            TOKEN_VALUES.addAll(List.of(
                "auto","break","case","char","const","continue","default","do","double",
                "else","enum","extern","float","for","goto","if","inline","int","long",
                "register","restrict","return","short","signed","sizeof","static","struct",
                "switch","typedef","union","unsigned","void","volatile","while","_Alignas",
                "_Alignof","_Atomic","_Bool","_Complex","_Generic","_Imaginary","_Noreturn",
                "_Static_assert","_Thread_local"
            ));
        }

        private static final Set<String> RESERVED_WORDS = new HashSet<>(
            TOKEN_VALUES.subList(15, TOKEN_VALUES.size())
        );

        private static final String TOKEN_REGEX =
            "(\\s+)|" +                       // WHITESPACE
            "(>=|<=|==|!=|\\+\\+|--|\\+=|-=|\\*=|/=|&&|\\|\\||[+\\-*/%<>=!&|^~])|" +
            "(\\{)|(\\})|(\\()|(\\))|(\\[)|(\\])|(;)|(,)|(\\.)|" +
            "(\"(\\\\.|[^\\\"\\\\])*\")|" +      // STRING_LITERAL
            "('(\\\\.|[^'\\\\])')|" +               // CHAR_LITERAL
            "(\\d+(\\.\\d+)?)|" +                     // NUMBER
            "([A-Za-z_][A-Za-z0-9_]*)";                    // IDENTIFIER

        public static List<Token> tokenize(String input) {
            List<Token> tokens = new ArrayList<>();
            Pattern p = Pattern.compile(TOKEN_REGEX);
            Matcher m = p.matcher(input);
            int line = 1;

            while (m.find()) {
                String lex = m.group();
                // Determine which group matched
                String type;
                if (m.group(1) != null)       type = "WHITESPACE";
                else if (m.group(2) != null)  type = "OPERATOR";
                else if (m.group(3) != null)  type = "LBRACE";
                else if (m.group(4) != null)  type = "RBRACE";
                else if (m.group(5) != null)  type = "LPAREN";
                else if (m.group(6) != null)  type = "RPAREN";
                else if (m.group(7) != null)  type = "LBRACKET";
                else if (m.group(8) != null)  type = "RBRACKET";
                else if (m.group(9) != null)  type = "SEMICOLON";
                else if (m.group(10) != null) type = "COMMA";
                else if (m.group(11) != null) type = "DOT";
                else if (m.group(12) != null) type = "STRING_LITERAL";
                else if (m.group(13) != null) type = "CHAR_LITERAL";
                else if (m.group(14) != null) type = "NUMBER";
                else                           type = "IDENTIFIER";

                if ("WHITESPACE".equals(type)) {
                    line += lex.chars().filter(c -> c=='\n').count();
                    continue;
                }

                // Reserved word check
                if ("IDENTIFIER".equals(type) && RESERVED_WORDS.contains(lex)) {
                    type = lex;  // e.g. "int"
                }

                // tokenValue = index in TOKEN_VALUES
                int idx = TOKEN_VALUES.indexOf(type);
                String val = idx >= 0 ? String.valueOf(idx) : "-1";

                tokens.add(new Token(line, lex, type, val));
            }
            return tokens;
        }

        public static void saveTokensToFile(List<Token> tokens, String filename)
                throws IOException {
            try (BufferedWriter w = Files.newBufferedWriter(Path.of(filename))) {
                w.write("Line\tLexeme\tToken\tTokenValue\n");
                for (Token t : tokens) {
                    w.write(String.format("%d\t%s\t%s\t%s\n",
                        t.lineNumber, t.lexeme, t.tokenType, t.tokenValue));
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            String input = Files.readString(Path.of("C:\\SEM 6\\CDLABESE\\input.txt"));
            var tokens = Lexer.tokenize(input);
            Lexer.saveTokensToFile(tokens, "output.txt");
            System.out.println("Tokenization completed. Output saved to output.txt.");
        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
        }
    }
}
