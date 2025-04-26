package lox;

class RuntimeError extends RuntimeException {
    // store token in here to keep track of line number.
    final Token token;

    RuntimeError(Token token, String message){
        super(message); 
        this.token = token;
    }
}
