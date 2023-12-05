package cool.compiler;

import cool.structures.ClassSymbol;
import cool.structures.Scope;
import cool.structures.SymbolTable;

public class ConnectionPassVisitor implements ASTVisitor<Void> {
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

        if (classs.parentClassId != null && !classs.parentClassId.getText().equals("SELF_TYPE")) {
            var parentClass = currentScope.lookup(classs.parentClassId.getText());
            if (parentClass == null) {
                SymbolTable.error(classs.ctx, classs.parentClassId, "Class " + classs.classId.getText() + " has undefined parent " + classs.parentClassId.getText());
                return null;
            }

            classs.symbol.setInheritedClass((ClassSymbol) parentClass);
        }

        if (classs.symbol.getInheritedClass() == null)
            classs.symbol.setInheritedClass((ClassSymbol) SymbolTable.globals.lookup("Object"));

        currentScope = classs.symbol;

        classs.features.forEach(feature -> feature.accept(this));

        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public Void visit(FuncFeature funcFeature) {
        if (funcFeature.symbol == null)
            return null;

        if (!funcFeature.funcType.getText().equals("SELF_TYPE")) {
            var type = SymbolTable.globals.lookup(funcFeature.funcType.getText());
            if (type == null) {
                SymbolTable.error(funcFeature.ctx, funcFeature.funcType, "Class " + funcFeature.parentClass + " has method " + funcFeature.funcId.getText() + " with undefined return type " + funcFeature.funcType.getText());
                return null;
            }

            funcFeature.symbol.setType((ClassSymbol) type);
        } else {
            funcFeature.symbol.setType(ClassSymbol.SELF_TYPE);
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

        if (!varFeature.varType.getText().equals("SELF_TYPE")) {
            var type = SymbolTable.globals.lookup(varFeature.varType.getText());
            if (type == null) {
                SymbolTable.error(varFeature.ctx, varFeature.varType, "Class " + varFeature.parentClass + " has attribute " + varFeature.varId.getText() + " with undefined type " + varFeature.varType.getText());
                return null;
            }

            varFeature.symbol.setType((ClassSymbol) type);
        } else {
            varFeature.symbol.setType(ClassSymbol.SELF_TYPE);
        }
        if (varFeature.e != null)
            varFeature.e.accept(this);

        return null;
    }

    @Override
    public Void visit(Formal formal) {
        if (formal.symbol == null)
            return null;

        if (!formal.formalType.getText().equals("SELF_TYPE")) {
            var type = SymbolTable.globals.lookup(formal.formalType.getText());
            if (type == null) {
                SymbolTable.error(formal.ctx, formal.formalType, "Method " + formal.parentMethod + " of class " + formal.parentClass + " has formal parameter " + formal.formalId.getText() + " with undefined type " + formal.formalType.getText());
                return null;
            }

            formal.symbol.setType((ClassSymbol) type);
        }
        return null;
    }

    @Override
    public Void visit(ExplicitDispatch explicitDispatch) {
        if (explicitDispatch.parentType != null && !explicitDispatch.parentType.getText().equals("SELF_TYPE")) {
            var type = SymbolTable.globals.lookup(explicitDispatch.parentType.getText());
            if (type == null) {
                SymbolTable.error(explicitDispatch.ctx, explicitDispatch.parentType, "Type " + explicitDispatch.parentType.getText() + " of static dispatch is undefined");
                return null;
            }

            explicitDispatch.setParentSymbol((ClassSymbol) type);
        }

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

        if (!local.varType.getText().equals("SELF_TYPE")) {
            var type = SymbolTable.globals.lookup(local.varType.getText());
            if (type == null) {
                SymbolTable.error(local.ctx, local.varType, "Let variable " + local.varId.getText() + " has undefined type " + local.varType.getText());
                return null;
            }

            local.symbol.setType((ClassSymbol) type);
        } else {
            local.symbol.setType(ClassSymbol.SELF_TYPE);
        }

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

        if (!caseBranch.varType.getText().equals("SELF_TYPE")) {
            var type = SymbolTable.globals.lookup(caseBranch.varType.getText());
            if (type == null) {
                SymbolTable.error(caseBranch.ctx, caseBranch.varType, "Case variable " + caseBranch.varId.getText() + " has undefined type " + caseBranch.varType.getText());
                return null;
            }

            caseBranch.symbol.setType((ClassSymbol) type);
        }
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
        if (!neww.initType.getText().equals("SELF_TYPE")) {
            var type = SymbolTable.globals.lookup(neww.initType.getText());
            if (type == null) {
                SymbolTable.error(neww.ctx, neww.initType, "new is used with undefined type " + neww.initType.getText());
            }

            neww.setSymbol((ClassSymbol) type);
        } else {
            neww.setSymbol(ClassSymbol.SELF_TYPE);
        }
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
