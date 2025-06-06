package lox;

public class AstPrinter implements Expr.Visitor<String>{

    String print(Expr expr){
        // call the implented method in the visitor
        return expr.accept(this);
    }

    // implement the visitor interface (methods for each of the types)
    // the implements what each ast node prints out.
    
    @Override
    public String visitBinaryExpr(Expr.Binary expr){
        return parenthesize(expr.operator.lexeme,
        expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr){
        return parenthesize("group",expr.expression);
    }
    
    @Override
    public String visitLiteralExpr(Expr.Literal expr){
        if(expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr){
        return parenthesize(expr.operator.lexeme,expr.right);
    }

    // calls print method of expressions, and 
    //      group expressions in paranthesis.
    // makes ordering more explicit.
    private String parenthesize(String name, Expr... exprs){
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs){
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

}
