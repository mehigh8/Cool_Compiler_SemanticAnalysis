package cool.compiler;

import cool.structures.Scope;
import cool.structures.SymbolTable;

public class ConnectionPassVisitor implements ASTVisitor<Void> {
    Scope currentScope = SymbolTable.globals;
    @Override
    public Void visit(Program program) {
        return null;
    }

    @Override
    public Void visit(Class classs) {
        return null;
    }

    @Override
    public Void visit(FuncFeature funcFeature) {
        return null;
    }

    @Override
    public Void visit(VarFeature varFeature) {
        return null;
    }

    @Override
    public Void visit(Formal formal) {
        return null;
    }

    @Override
    public Void visit(ExplicitDispatch explicitDispatch) {
        return null;
    }

    @Override
    public Void visit(ImplicitDispatch implicitDispatch) {
        return null;
    }

    @Override
    public Void visit(If iff) {
        return null;
    }

    @Override
    public Void visit(While whilee) {
        return null;
    }

    @Override
    public Void visit(Block block) {
        return null;
    }

    @Override
    public Void visit(Local local) {
        return null;
    }

    @Override
    public Void visit(Let let) {
        return null;
    }

    @Override
    public Void visit(CaseBranch caseBranch) {
        return null;
    }

    @Override
    public Void visit(Case casee) {
        return null;
    }

    @Override
    public Void visit(New neww) {
        return null;
    }

    @Override
    public Void visit(Compl compl) {
        return null;
    }

    @Override
    public Void visit(Isvoid isvoid) {
        return null;
    }

    @Override
    public Void visit(MultDiv multDiv) {
        return null;
    }

    @Override
    public Void visit(PlusMinus plusMinus) {
        return null;
    }

    @Override
    public Void visit(Relational relational) {
        return null;
    }

    @Override
    public Void visit(Not not) {
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        return null;
    }

    @Override
    public Void visit(Paren paren) {
        return null;
    }

    @Override
    public Void visit(Id id) {
        return null;
    }

    @Override
    public Void visit(IntegerExpr integerExpr) {
        return null;
    }

    @Override
    public Void visit(StringExpr stringExpr) {
        return null;
    }

    @Override
    public Void visit(BoolExpr boolExpr) {
        return null;
    }
}
