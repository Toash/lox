package src.lox; 
import static src.lox.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();    

    // start and current used to get the indices of the actual lexeme
    private int start=0; // start of lexeme
    private int current=0; // character this scanner is observing

    private int line=1;

    // identifiers and keywords are "ambiguous"
    // so we will use this map to tell whether an identifer lexeme is 
    //  actually a reserved keyword.
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    Scanner(String source){
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()){
            start = current;
            scanToken();
        }

        // EOF
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }
    
    // convert String to Token.
    private void scanToken(){
        char c = advance();
        switch(c){
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;

            case '!':
                addToken(match('=') ? BANG_EQUAL:BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL:EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL:LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL:GREATER);
                break;
            case '/':
                if (match('/')){
                    // comment until end of file
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;

            case '"': string(); break;

            default:
                if(isDigit(c)){
                    number();
                } else if (isAlpha(c)){
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void identifier(){
        while(isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void number(){
        while (isDigit(peek())) advance();

        if(peek() == '.' && isDigit(peekNext())){
            // consume .
            advance();

            while(isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string(){
        while(peek() != '"' && !isAtEnd()){
            // multiline string supported
            if (peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()){
            Lox.error(line,"Unterminated string.");
            return;
        }

        // closing ".
        advance();

        // strip quotes to store the actual string lexeme.
        String value = source.substring(start+1,current-1);
        addToken(STRING, value);


    }

    // like peek except it also consumes if true.
    private boolean match(char expected){
        if (isAtEnd()) return false;
        if(source.charAt(current) != expected) return false;

        current++; 
        return true;
    }

    // lookahead
    private char peek(){
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    // second lookahead 
    private char peekNext(){
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c){
        return (c >= 'a' && c <= 'z') ||
        (c >= 'A' && c <= 'Z') ||
        c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c){
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd(){
        return current >= source.length();
    }

    private char advance(){
        return source.charAt(current++);
    }

    private void addToken(TokenType type){
        addToken(type,null);
    }
    private void addToken(TokenType type, Object literal){
        String text = source.substring(start,current);
        tokens.add(new Token(type,text,literal,line));
    }

   
        

}
