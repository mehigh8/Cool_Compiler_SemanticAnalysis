# Cool Language Compiler - Semantic Analysis

## What This Does

Validates the semantic correctness of Cool programs by analyzing type relationships, checking method signatures, managing symbol scopes, and ensuring all language rules are followed beyond syntax.

## How It Works

### Type System Implementation
- **Bottom-Up Type Inference**: Analyzes expressions from leaves to root, propagating types upward through the AST
- **Subtyping Relationships**: Implements Cool's inheritance-based type system where every user type inherits from Object
- **Method Dispatch Resolution**: Determines correct method implementations based on static types and inheritance hierarchy
- **Type Environment Management**: Maintains type bindings for variables, methods, and classes throughout analysis

### Symbol Table Strategy
- **Hierarchical Scoping**: Implements nested scopes (global → class → method → block) with proper shadowing rules
- **Lazy Binding**: Resolves symbols on-demand to handle forward references and circular dependencies
- **Inheritance Chain Walking**: Searches method/attribute definitions up the inheritance hierarchy
- **Self-Type Handling**: Special treatment for `SELF_TYPE` which behaves as the actual runtime type

### Semantic Validation Process
- **Three-Pass Analysis**:
  1. **Inheritance Graph Construction**: Builds class hierarchy, detects cycles, validates inheritance rules
  2. **Symbol Collection**: Gathers all class features (methods/attributes) with signature validation
  3. **Expression Type Checking**: Validates all expressions conform to type system rules

## Key Technical Decisions

### Why Three-Pass Analysis
- **Dependency Resolution**: Forward references in Cool require multiple passes to resolve correctly
- **Error Isolation**: Separates different categories of semantic errors for clearer diagnostics
- **Performance**: Early detection of structural errors prevents unnecessary expression analysis

### Type Environment Design
- **Immutable Scopes**: Each scope level is immutable, preventing accidental mutations during analysis
- **Copy-on-Write**: Efficient scope extension without affecting parent scopes
- **Type Caching**: Memoizes type computations for repeated expressions

## Input/Output

**Input**: Syntactically correct AST from lexical/syntactic analysis phase
**Output**: 
- Type-annotated AST with complete semantic information
- Symbol tables for all scopes and class hierarchies
- Detailed semantic error reports with context

## Compiler Pipeline Integration

This is the **second phase** of the complete Cool compiler:

**Previous Phase**: [Lexical & Syntactic Analysis](https://github.com/mehigh8/Cool_Compiler_LexicalSyntacticAnalysis) - Provides the input AST
**Next Phase**: [Code Generation](https://github.com/mehigh8/Cool_Compiler_CodeGeneration) - Uses type-annotated AST and symbol tables to generate executable code

The semantic analysis ensures that only type-safe, well-formed programs proceed to code generation, catching logical errors before runtime.
