package lox;
// Token emitted by scanner
public class Token {
   final TokenType type; 
   final String lexeme; // specific instance of Token 
   final Object literal; 
   final int line;

   Token(TokenType type, String lexeme, Object literal, int line){
        this.type = type;
        this.lexeme= lexeme;
        this.literal= literal;
        this.line= line;
   }

   public String toString(){
        return type + " " + lexeme + " " + literal;
      //   return String.format("TokenType: %s\t\t\t lexeme: %s\t\t\t literal: %s",type,lexeme,literal);
   }
}
