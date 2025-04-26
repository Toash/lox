package lox;
import java.util.List;
import static lox.TokenType.*;


/*
    defines methods for expanding nonterminals.
    recursive descent over tokens and produces AST

    AST consists of the objcets that we created to model the AST nodes. 

    Higher precendence are handled deeper in call stack 
    Lower precendence are handled higher up

    Precendence is enforced by grouping higher precendence operators earlier.
*/
public class Parser {
    private static class ParseError extends RuntimeException{}
    private final List<Token> tokens;
    private int current = 0; // index of current token 

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    Expr parse(){
        try {
            // parse single expression and return
            return expression();
        } catch (ParseError error){
            return null;
        }
    }

    // expression -> comma;
    private Expr expression(){
        return comma();
    }

    // comma -> equality("," equality)*;
    private Expr comma(){
        Expr expr = equality();

        while (match(COMMA)){
            Token comma = previous();
            Expr right = equality();
            expr = new Expr.Binary(expr,comma,right); 
        }
        return expr;
    }
    
    // equality -> comparison (("!=" | "==") comparison)*;
    private Expr equality(){
        Expr expr = comparison();

        while(match(BANG_EQUAL, EQUAL_EQUAL)){
            Token operator = previous();    
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // comparison -> term ((">" | ">=" | "<" | "<=" ) term )*;
    private Expr comparison() {
        Expr expr = term();

        while(match(GREATER,GREATER_EQUAL,LESS,LESS_EQUAL)){
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // term -> factor ("-" | "+") factor
    private Expr term(){
        Expr expr = factor();

        while(match(MINUS,PLUS)){
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // factor -> unary ("/" | "*") unary;
    private Expr factor(){
        Expr expr = unary();

        while(match(SLASH,STAR)){
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // unary -> ("!" | "-") unary | primary;
    private Expr unary(){
        if(match(BANG,MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator,right);
        }

        return primary();
    }

    // highest level of precendence
    // primary expressions.
    // primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ; 
    private Expr primary(){
        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE)) return new Expr.Literal(true);
        if(match(NIL)) return new Expr.Literal(null);

        // get the actual values for these
        if (match(NUMBER,STRING)){
            return new Expr.Literal(previous().literal);
        }


        if(match(LEFT_PAREN)){
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        // unknown token
        throw error(peek(), "Expect expression.");
    }



    // check if current token matches any types
    //      consume if true.
    private boolean match(TokenType... types){
        for(TokenType type: types){
            if (check(type)){
                advance();
                return true;
            }
        }
        return false;
        
    }

    // tries to consume the specified tokentype.
    //  if not, throws an error.
    private Token consume(TokenType type, String message){
        if (check(type)) return advance();

        throw error(peek(),message);
    }

    // check if it matches, does not consume.
    private boolean check(TokenType type){
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    // consume current token and returns it.
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd(){
        // EOF token
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }


    // useful for getting the previous token after successfully matching.
    private Token previous() {
        return tokens.get(current-1);
    }

    private ParseError error(Token token, String message){
        Lox.error(token, message);

        return new ParseError();
    }

    // discard token until we "might" have found a statement
    //  boundary i.e. discord tokens that would have likely
    // caused cascaded errors.
    private void synchronize() {
        advance();

        while(!isAtEnd()){
            // after semicolon
            // this could be wrong, for example in a for loop
            if(previous().type == SEMICOLON) return;

            // at a statement boundary. 
            switch(peek().type){
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();

        }
    }


}
