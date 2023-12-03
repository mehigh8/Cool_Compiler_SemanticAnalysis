package cool.compiler;

import cool.structures.*;

import java.util.Map;

public class VerificationPassVisitor implements ASTVisitor<Void> {
    private Scope currentScope;
    @Override
    public Void visit(Program program) {
        currentScope = SymbolTable.globals;
        program.classes.forEach(cls -> cls.accept(this));
        return null;
    }

    @Override
    public Void visit(Class classs) {
        if (classs.symbol == null)
            return null;

        ClassSymbol current = classs.symbol.getInheritedClass();
        while (current != null && current != classs.symbol)
            current = current.getInheritedClass();

        if (current != null) {
            SymbolTable.error(classs.ctx, classs.classId, "Inheritance cycle for class " + classs.classId.getText());
            return null;
        }

        currentScope = classs.symbol;

        classs.features.forEach(feature -> feature.accept(this));

        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public Void visit(FuncFeature funcFeature) {
        if (funcFeature.symbol == null)
            return null;

        ClassSymbol classSymbol = (ClassSymbol) currentScope;
        if (classSymbol.getInheritedClass() != null) {
            FunctionSymbol inherited = (FunctionSymbol) classSymbol.getInheritedClass().lookupMethod(funcFeature.funcId.getText());
            if (inherited != null) {
                if (inherited.getSymbols().size() != funcFeature.formals.size()) {
                    SymbolTable.error(funcFeature.ctx, funcFeature.funcId, "Class " + funcFeature.parentClass + " overrides method " + funcFeature.funcId.getText() + " with different number of formal parameters");
                    return null;
                }

                if (!inherited.getType().getName().equals(funcFeature.funcType.getText()))
                    SymbolTable.error(funcFeature.ctx, funcFeature.funcType, "Class " + funcFeature.parentClass + " overrides method " + funcFeature.funcId.getText() + " but changes return type from " + inherited.getType().getName() + " to " + funcFeature.funcType.getText());

                int i = 0;
                for (Map.Entry<String, Symbol> symbolEntry : inherited.getSymbols().entrySet()) {
                    Formal formal = funcFeature.formals.get(i);
                    IdSymbol inheritedFormal = (IdSymbol) symbolEntry.getValue();
                    if (!inheritedFormal.getType().getName().equals(formal.formalType.getText()))
                        SymbolTable.error(funcFeature.ctx, formal.formalType, "Class " + funcFeature.parentClass + " overrides method " + funcFeature.funcId.getText() + " but changes type of formal parameter " + formal.formalId.getText() + " from " + inheritedFormal.getType().getName() + " to " + formal.formalType.getText());
                    i++;
                }
            }
        }

        currentScope = funcFeature.symbol;

        funcFeature.formals.forEach(formal -> formal.accept(this));
        funcFeature.e.accept(this);

        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public Void visit(VarFeature varFeature) {
        if (varFeature.symbol == null)
            return null;

        ClassSymbol classSymbol = (ClassSymbol) currentScope;
        if (classSymbol.getInheritedClass() != null) {
            if (classSymbol.getInheritedClass().lookupAttribute(varFeature.varId.getText()) != null)
                SymbolTable.error(varFeature.ctx, varFeature.varId, "Class " + varFeature.parentClass + " redefines inherited attribute " + varFeature.varId.getText());
        }

        if (varFeature.e != null)
            varFeature.e.accept(this);

        return null;
    }

    @Override
    public Void visit(Formal formal) {
        return null;
    }

    @Override
    public Void visit(ExplicitDispatch explicitDispatch) {
        explicitDispatch.obj.accept(this);
        explicitDispatch.dispatch.accept(this);
        return null;
    }

    @Override
    public Void visit(ImplicitDispatch implicitDispatch) {
        implicitDispatch.funcParams.forEach(param -> param.accept(this));
        return null;
    }

    @Override
    public Void visit(If iff) {
        iff.cond.accept(this);
        iff.thenBranch.accept(this);
        iff.elseBranch.accept(this);
        return null;
    }

    @Override
    public Void visit(While whilee) {
        whilee.cond.accept(this);
        whilee.content.accept(this);
        return null;
    }

    @Override
    public Void visit(Block block) {
        block.exprs.forEach(expr -> expr.accept(this));
        return null;
    }

    @Override
    public Void visit(Local local) {
        if (local.symbol == null)
            return null;

        if (local.varExpr != null)
            local.varExpr.accept(this);

        currentScope = local.symbol;
        return null;
    }

    @Override
    public Void visit(Let let) {
        Scope beforeLet = currentScope;

        let.localList.forEach(local -> local.accept(this));
        let.letContent.accept(this);

        currentScope = beforeLet;
        return null;
    }

    @Override
    public Void visit(CaseBranch caseBranch) {
        if (caseBranch.symbol == null)
            return null;

        currentScope = caseBranch.symbol;

        caseBranch.branchExpr.accept(this);

        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public Void visit(Case casee) {
        casee.caseExpr.accept(this);
        casee.caseBranches.forEach(branch -> branch.accept(this));
        return null;
    }

    @Override
    public Void visit(New neww) {
        return null;
    }

    @Override
    public Void visit(Compl compl) {
        compl.e.accept(this);
        return null;
    }

    @Override
    public Void visit(Isvoid isvoid) {
        isvoid.e.accept(this);
        return null;
    }

    @Override
    public Void visit(MultDiv multDiv) {
        multDiv.left.accept(this);
        multDiv.right.accept(this);
        return null;
    }

    @Override
    public Void visit(PlusMinus plusMinus) {
        plusMinus.left.accept(this);
        plusMinus.right.accept(this);
        return null;
    }

    @Override
    public Void visit(Relational relational) {
        relational.left.accept(this);
        relational.right.accept(this);
        return null;
    }

    @Override
    public Void visit(Not not) {
        not.e.accept(this);
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        if (!assign.varId.getText().equals("self")) {
            Symbol idSymbol = currentScope.lookup("1" + assign.varId.getText());
            if (idSymbol == null)
                SymbolTable.error(assign.ctx, assign.varId, "Undefined identifier " + assign.varId.getText());
            else
                assign.setSymbol((IdSymbol) idSymbol);
        }

        assign.e.accept(this);
        return null;
    }

    @Override
    public Void visit(Paren paren) {
        paren.e.accept(this);
        return null;
    }

    @Override
    public Void visit(Id id) {
        if (!id.varId.getText().equals("self")) {
            Symbol idSymbol = currentScope.lookup("1" + id.varId.getText());
            if (idSymbol == null)
                SymbolTable.error(id.ctx, id.varId, "Undefined identifier " + id.varId.getText());
            else
                id.setSymbol((IdSymbol) idSymbol);
        }

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
