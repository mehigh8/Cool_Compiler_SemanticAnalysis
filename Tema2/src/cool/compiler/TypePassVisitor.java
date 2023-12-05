package cool.compiler;

import cool.structures.*;

import java.util.Map;

public class TypePassVisitor implements ASTVisitor<ClassSymbol> {
    private Scope currentScope;
    private ClassSymbol dispatchClass;
    private ClassSymbol returnTypeDispatchSelfType = null;

    @Override
    public ClassSymbol visit(Program program) {
        currentScope = SymbolTable.globals;
        program.classes.forEach(cls -> cls.accept(this));
        return null;
    }

    @Override
    public ClassSymbol visit(Class classs) {
        if (classs.symbol == null)
            return null;

        currentScope = classs.symbol;

        classs.features.forEach(feature -> feature.accept(this));

        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public ClassSymbol visit(FuncFeature funcFeature) {
        if (funcFeature.symbol == null)
            return null;

        currentScope = funcFeature.symbol;
        funcFeature.formals.forEach(formal -> formal.accept(this));

        ClassSymbol bodyType = funcFeature.e.accept(this);
        if (bodyType != null) {
            if (funcFeature.symbol.getType() == ClassSymbol.SELF_TYPE && bodyType == ClassSymbol.SELF_TYPE) {
                currentScope = currentScope.getParent();
                return null;
            }

            if (!ClassSymbol.isChildOf(ClassSymbol.translateClass(bodyType, currentScope, null), funcFeature.symbol.getType()))
                SymbolTable.error(funcFeature.ctx, funcFeature.e.start, "Type " + bodyType.getName() + " of the body of method " + funcFeature.funcId.getText() + " is incompatible with declared return type " + funcFeature.funcType.getText());
        }
        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public ClassSymbol visit(VarFeature varFeature) {
        if (varFeature.symbol == null)
            return null;

        if (varFeature.e != null) {
            ClassSymbol initType = varFeature.e.accept(this);
            if (initType != null) {
                if (!ClassSymbol.isChildOf(ClassSymbol.translateClass(initType, currentScope, null), ClassSymbol.translateClass(varFeature.symbol.getType(), currentScope, null)))
                    SymbolTable.error(varFeature.ctx, varFeature.e.start, "Type " + initType.getName() + " of initialization expression of attribute " + varFeature.varId.getText() + " is incompatible with declared type " + varFeature.varType.getText());
            }
        }
        return null;
    }

    @Override
    public ClassSymbol visit(Formal formal) {
        return null;
    }

    @Override
    public ClassSymbol visit(ExplicitDispatch explicitDispatch) {
        if (explicitDispatch.parentType != null && (explicitDispatch.parentType.getText().equals("SELF_TYPE") || explicitDispatch.parentSymbol == null))
            return null;

        dispatchClass = explicitDispatch.obj.accept(this);
        if (dispatchClass == null)
            return null;

        returnTypeDispatchSelfType = dispatchClass;
        dispatchClass = ClassSymbol.translateClass(dispatchClass, currentScope, null);

        if (explicitDispatch.parentSymbol != null) {
            if (!ClassSymbol.isChildOf(dispatchClass, explicitDispatch.parentSymbol)) {
                SymbolTable.error(explicitDispatch.ctx, explicitDispatch.parentType, "Type " + explicitDispatch.parentSymbol.getName() + " of static dispatch is not a superclass of type " + dispatchClass.getName());
                return null;
            }
            dispatchClass = explicitDispatch.parentSymbol;
        }

        return explicitDispatch.dispatch.accept(this);
    }

    @Override
    public ClassSymbol visit(ImplicitDispatch implicitDispatch) {
        if (!implicitDispatch.fromExplicit) {
            Scope scope = currentScope;
            while (!(scope instanceof ClassSymbol))
                scope = scope.getParent();

            dispatchClass = (ClassSymbol) scope;

            returnTypeDispatchSelfType = ClassSymbol.SELF_TYPE;
        }

        FunctionSymbol functionSymbol = (FunctionSymbol) dispatchClass.lookupMethod(implicitDispatch.funcId.getText());
        if (functionSymbol == null) {
            SymbolTable.error(implicitDispatch.ctx, implicitDispatch.funcId, "Undefined method " + implicitDispatch.funcId.getText() + " in class " + dispatchClass.getName());
            return null;
        }

        if (functionSymbol.getSymbols().size() != implicitDispatch.funcParams.size()) {
            SymbolTable.error(implicitDispatch.ctx, implicitDispatch.funcId, "Method " + implicitDispatch.funcId.getText() + " of class " + dispatchClass.getName() + " is applied to wrong number of arguments");
            return  null;
        }

        int i = 0;
        for (Map.Entry<String, Symbol> entrySymbol : functionSymbol.getSymbols().entrySet()) {
            ClassSymbol formalType = ((IdSymbol) entrySymbol.getValue()).getType();
            ClassSymbol actualType = implicitDispatch.funcParams.get(i).accept(this);

            if (actualType != null) {
                if (!ClassSymbol.isChildOf(ClassSymbol.translateClass(actualType, currentScope, null), formalType))
                    SymbolTable.error(implicitDispatch.ctx, implicitDispatch.funcParams.get(i).start, "In call to method " + functionSymbol.getName() + " of class " + dispatchClass.getName() + ", actual type " + actualType.getName() + " of formal parameter " + entrySymbol.getKey() + " is incompatible with declared type " + formalType.getName());
            }

            i++;
        }
        return ClassSymbol.translateClass(functionSymbol.getType(), currentScope, returnTypeDispatchSelfType);
    }

    @Override
    public ClassSymbol visit(If iff) {
        ClassSymbol condType = iff.cond.accept(this);
        if (condType == null)
            return null;

        if (!condType.getName().equals("Bool"))
            SymbolTable.error(iff.ctx, iff.cond.start, "If condition has type " + condType.getName() + " instead of Bool");

        ClassSymbol thenBranchType = iff.thenBranch.accept(this);
        ClassSymbol elseBranchType = iff.elseBranch.accept(this);

        if (thenBranchType == ClassSymbol.SELF_TYPE && elseBranchType == ClassSymbol.SELF_TYPE)
            return ClassSymbol.SELF_TYPE;
        return ClassSymbol.leastCommonAncestor(ClassSymbol.translateClass(thenBranchType, currentScope, null), ClassSymbol.translateClass(elseBranchType, currentScope, null));
    }

    @Override
    public ClassSymbol visit(While whilee) {
        ClassSymbol condType = whilee.cond.accept(this);
        if (condType == null)
            return null;

        if (!condType.getName().equals("Bool"))
            SymbolTable.error(whilee.ctx, whilee.cond.start, "While condition has type " + condType.getName() + " instead of Bool");

        whilee.content.accept(this);
        return (ClassSymbol) SymbolTable.globals.lookup("Object");
    }

    @Override
    public ClassSymbol visit(Block block) {
        ClassSymbol returnType = null;
        for (Expression expr : block.exprs)
            returnType = expr.accept(this);

        return returnType;
    }

    @Override
    public ClassSymbol visit(Local local) {
        if (local.symbol == null || local.symbol.getType() == null)
            return null;

        if (local.varExpr != null) {
            ClassSymbol exprType = local.varExpr.accept(this);
            if (exprType != null) {
                if (!ClassSymbol.isChildOf(ClassSymbol.translateClass(exprType, currentScope, null), ClassSymbol.translateClass(local.symbol.getType(), currentScope, null)))
                    SymbolTable.error(local.ctx, local.varExpr.start, "Type " + exprType.getName() + " of initialization expression of identifier " + local.varId.getText() + " is incompatible with declared type " + local.varType.getText());
            }
        }

        currentScope = local.symbol;
        return null;
    }

    @Override
    public ClassSymbol visit(Let let) {
        Scope beforeLet = currentScope;

        let.localList.forEach(local -> local.accept(this));
        ClassSymbol returnType = let.letContent.accept(this);

        currentScope = beforeLet;
        return returnType;
    }

    @Override
    public ClassSymbol visit(CaseBranch caseBranch) {
        if (caseBranch.symbol == null)
            return null;

        currentScope = caseBranch.symbol;

        ClassSymbol returnType = caseBranch.branchExpr.accept(this);

        currentScope = currentScope.getParent();

        return ClassSymbol.translateClass(returnType, currentScope, null);
    }

    @Override
    public ClassSymbol visit(Case casee) {
        casee.caseExpr.accept(this);

        ClassSymbol returnType = casee.caseBranches.getFirst().accept(this);
        for (int i = 1; i < casee.caseBranches.size(); i++)
            returnType = ClassSymbol.leastCommonAncestor(returnType, casee.caseBranches.get(i).accept(this));

        return returnType;
    }

    @Override
    public ClassSymbol visit(New neww) {
        if (neww.symbol != null)
            return neww.symbol;

        return null;
    }

    @Override
    public ClassSymbol visit(Compl compl) {
        ClassSymbol operandType = compl.e.accept(this);
        if (operandType != null && !operandType.getName().equals("Int")) {
            SymbolTable.error(compl.ctx, compl.e.start, "Operand of ~ has type " + operandType.getName() + " instead of Int");
            return null;
        }
        return operandType;
    }

    @Override
    public ClassSymbol visit(Isvoid isvoid) {
        return (ClassSymbol) SymbolTable.globals.lookup("Bool");
    }

    @Override
    public ClassSymbol visit(MultDiv multDiv) {
        ClassSymbol leftType = multDiv.left.accept(this);
        ClassSymbol rightType = multDiv.right.accept(this);

        if (leftType != null && !leftType.getName().equals("Int")) {
            SymbolTable.error(multDiv.ctx, multDiv.left.start, "Operand of " + multDiv.op.getText() + " has type " + leftType.getName() + " instead of Int");
            return null;
        }

        if (rightType != null && !rightType.getName().equals("Int")) {
            SymbolTable.error(multDiv.ctx, multDiv.right.start, "Operand of " + multDiv.op.getText() + " has type " + rightType.getName() + " instead of Int");
            return null;
        }

        if (leftType != null && rightType != null)
            return leftType;

        return null;
    }

    @Override
    public ClassSymbol visit(PlusMinus plusMinus) {
        ClassSymbol leftType = plusMinus.left.accept(this);
        ClassSymbol rightType = plusMinus.right.accept(this);

        if (leftType != null && !leftType.getName().equals("Int")) {
            SymbolTable.error(plusMinus.ctx, plusMinus.left.start, "Operand of " + plusMinus.op.getText() + " has type " + leftType.getName() + " instead of Int");
            return null;
        }

        if (rightType != null && !rightType.getName().equals("Int")) {
            SymbolTable.error(plusMinus.ctx, plusMinus.right.start, "Operand of " + plusMinus.op.getText() + " has type " + rightType.getName() + " instead of Int");
            return null;
        }

        if (leftType != null && rightType != null)
            return leftType;

        return null;
    }

    @Override
    public ClassSymbol visit(Relational relational) {
        ClassSymbol leftType = relational.left.accept(this);
        ClassSymbol rightType = relational.right.accept(this);

        if (relational.op.getText().equals("<") || relational.op.getText().equals("<=")) {
            if (leftType != null && !leftType.getName().equals("Int")) {
                SymbolTable.error(relational.ctx, relational.left.start, "Operand of " + relational.op.getText() + " has type " + leftType.getName() + " instead of Int");
                return null;
            }

            if (rightType != null && !rightType.getName().equals("Int")) {
                SymbolTable.error(relational.ctx, relational.right.start, "Operand of " + relational.op.getText() + " has type " + rightType.getName() + " instead of Int");
                return null;
            }

            if (leftType != null && rightType != null)
                return (ClassSymbol) SymbolTable.globals.lookup("Bool");

        } else {
            if (leftType == null || rightType == null)
                return null;

            if (leftType.getName().equals("Int") && !rightType.getName().equals("Int")) {
                SymbolTable.error(relational.ctx, relational.op, "Cannot compare " + leftType.getName() + " with " + rightType.getName());
                return null;
            }

            if (rightType.getName().equals("Int") && !leftType.getName().equals("Int")) {
                SymbolTable.error(relational.ctx, relational.op, "Cannot compare " + leftType.getName() + " with " + rightType.getName());
                return null;
            }

            if (leftType.getName().equals("Bool") && !rightType.getName().equals("Bool")) {
                SymbolTable.error(relational.ctx, relational.op, "Cannot compare " + leftType.getName() + " with " + rightType.getName());
                return null;
            }

            if (rightType.getName().equals("Bool") && !leftType.getName().equals("Bool")) {
                SymbolTable.error(relational.ctx, relational.op, "Cannot compare " + leftType.getName() + " with " + rightType.getName());
                return null;
            }

            if (leftType.getName().equals("String") && !rightType.getName().equals("String")) {
                SymbolTable.error(relational.ctx, relational.op, "Cannot compare " + leftType.getName() + " with " + rightType.getName());
                return null;
            }

            if (rightType.getName().equals("String") && !leftType.getName().equals("String")) {
                SymbolTable.error(relational.ctx, relational.op, "Cannot compare " + leftType.getName() + " with " + rightType.getName());
                return null;
            }

            return (ClassSymbol) SymbolTable.globals.lookup("Bool");
        }
        return null;
    }

    @Override
    public ClassSymbol visit(Not not) {
        ClassSymbol operandType = not.e.accept(this);
        if (operandType != null && !operandType.getName().equals("Bool")) {
            SymbolTable.error(not.ctx, not.e.start, "Operand of not has type " + operandType.getName() + " instead of Bool");
            return null;
        }
        return operandType;
    }

    @Override
    public ClassSymbol visit(Assign assign) {
        if (assign.symbol == null)
            return null;

        ClassSymbol initType = assign.e.accept(this);
        if (initType != null) {
            if (assign.symbol.getType() == ClassSymbol.SELF_TYPE && initType == ClassSymbol.SELF_TYPE)
                return ClassSymbol.SELF_TYPE;

            if (!ClassSymbol.isChildOf(ClassSymbol.translateClass(initType, currentScope, null), assign.symbol.getType())) {
                SymbolTable.error(assign.ctx, assign.e.start, "Type " + initType.getName() + " of assigned expression is incompatible with declared type " + assign.symbol.getType() + " of identifier " + assign.varId.getText());
                return null;
            }
        }

        return initType;
    }

    @Override
    public ClassSymbol visit(Paren paren) {
        return paren.e.accept(this);
    }

    @Override
    public ClassSymbol visit(Id id) {
        if (id.varId.getText().equals("self"))
            return ClassSymbol.SELF_TYPE;

        if (id.symbol == null)
            return null;

        return id.symbol.getType();
    }

    @Override
    public ClassSymbol visit(IntegerExpr integerExpr) {
        return (ClassSymbol) SymbolTable.globals.lookup("Int");
    }

    @Override
    public ClassSymbol visit(StringExpr stringExpr) {
        return (ClassSymbol) SymbolTable.globals.lookup("String");
    }

    @Override
    public ClassSymbol visit(BoolExpr boolExpr) {
        return (ClassSymbol) SymbolTable.globals.lookup("Bool");
    }
}
