package c0.table;

import c0.tokenizer.TokenType;

public class SymbolEntry {
    private String name;
    private TokenType tokenType;
    private SymbolType symbolType;
    private int stackOffset=-1;
    private int deep;
    private boolean isConstant;
    private boolean isInitialized;



    public SymbolEntry(String name, TokenType type, SymbolType symbolType, int deep, boolean isConstant, boolean isInitialized) {
        this.name = name;
        this.tokenType = type;
        this.symbolType = symbolType;
        this.deep = deep;
        this.isConstant = isConstant;
        this.isInitialized = isInitialized;
    }

    public SymbolEntry(String name, TokenType tokenType, SymbolType symbolType, int stackOffset, int deep, boolean isConstant, boolean isInitialized) {
        this.name = name;
        this.tokenType = tokenType;
        this.symbolType = symbolType;
        this.stackOffset = stackOffset;
        this.deep = deep;
        this.isConstant = isConstant;
        this.isInitialized = isInitialized;
    }
    public SymbolEntry(TokenType type) {
        this.name = "*Return*";
        this.tokenType = type;
        this.symbolType = SymbolType.RET;
        this.deep = 2;
        this.stackOffset = 0;
        this.isConstant = false;
        this.isInitialized = false;
    }

    public String getName() {
        return name;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public int getStackOffset() {
        return stackOffset;
    }

    public int getDeep() {
        return deep;
    }

    public boolean isConstant() {
        return isConstant;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public void setSymbolType(SymbolType symbolType) {
        this.symbolType = symbolType;
    }

    public void setStackOffset(int stackOffset) {
        this.stackOffset = stackOffset;
    }

    public void setDeep(int deep) {
        this.deep = deep;
    }

    public void setConstant(boolean constant) {
        isConstant = constant;
    }

    public void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }
}
