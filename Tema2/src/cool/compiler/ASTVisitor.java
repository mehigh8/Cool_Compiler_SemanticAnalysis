package cool.compiler;

public interface ASTVisitor<T> {
    T visit(Program program);
    T visit(Class classs);
    T visit(FuncFeature funcFeature);
    T visit(VarFeature varFeature);
    T visit(Formal formal);
    T visit(ExplicitDispatch explicitDispatch);
    T visit(ImplicitDispatch implicitDispatch);
    T visit(If iff);
    T visit(While whilee);
    T visit(Block block);
    T visit(Local local);
    T visit(Let let);
    T visit(CaseBranch caseBranch);
    T visit(Case casee);
    T visit(New neww);
    T visit(Compl compl);
    T visit(Isvoid isvoid);
    T visit(MultDiv multDiv);
    T visit(PlusMinus plusMinus);
    T visit(Relational relational);
    T visit(Not not);
    T visit(Assign assign);
    T visit(Paren paren);
    T visit(Id id);
    T visit(IntegerExpr integerExpr);
    T visit(StringExpr stringExpr);
    T visit(BoolExpr boolExpr);
}
