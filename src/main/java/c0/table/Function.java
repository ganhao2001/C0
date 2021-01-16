package c0.table;

import c0.error.AnalyzeError;
import c0.error.ErrorCode;
import c0.instruction.Instruction;
import c0.tokenizer.TokenType;
import c0.util.Pos;

import java.util.ArrayList;
import java.util.List;

public class Function {
    private String name;
    private TokenType type;
    private List<Instruction> instructions=new ArrayList<>();
    private List<SymbolEntry> symbolTable=new ArrayList<>();
    private int paramsnum = 0;
    private int varnum = 0;

    public Function(String name){
        this.name= name;
        this.type =TokenType.VOID;
    }

    public Function(String name, TokenType type, int paramsnum, int varnum) {
        this.name = name;
        this.type = type;
        this.paramsnum = paramsnum;
        this.varnum = varnum;
    }

    public String getName() {
        return name;
    }

    public TokenType getType() {
        return type;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public List<SymbolEntry> getSymbolTable() {
        return symbolTable;
    }

    public int getParamsnum() {
        return paramsnum;
    }

    public int getVarnum() {
        return varnum;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public void setSymbolTable(List<SymbolEntry> symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void setParamsnum(int paramsnum) {
        this.paramsnum = paramsnum;
    }

    public void setVarnum(int varnum) {
        this.varnum = varnum;
    }

    public void addReturnSlot(TokenType tokenType){
        this.symbolTable.add(0,new SymbolEntry(tokenType));
        for (int i=1;i<=this.paramsnum;i++){
            this.symbolTable.get(i).setStackOffset(symbolTable.get(i).getStackOffset()+1);
        }
    }
    public void addParam(String name,TokenType tokenType)throws AnalyzeError {
        for (int i=0;i<this.paramsnum;i++){
            if(this.symbolTable.get(i).getName().equals(name)){
                throw new AnalyzeError(ErrorCode.DuplicateParamName, new Pos(0,0));
            }
        }
        this.symbolTable.add(new SymbolEntry(name, tokenType, SymbolType.PARAM, paramsnum++,2, false, true));
    }
    public SymbolEntry searchSymbol(String name) {
        for (SymbolEntry s : this.symbolTable) {
            if (s.getName().equals(name)) return s;
        }
        return null;
    }
    public void addVar(String name,TokenType tokenType,int deep,boolean isConstant,boolean isInitialized)throws AnalyzeError{
        if(searchSymbol(name)!=null&&searchSymbol(name).getDeep()==deep){
            throw new AnalyzeError(ErrorCode.DuplicateName, new Pos(0,0));
        }
        SymbolEntry symbolEntry = new SymbolEntry(name,tokenType, SymbolType.VAR,varnum++,deep,isConstant,isInitialized);
        this.symbolTable.add(symbolEntry);
    }
    public  SymbolEntry searchSymbolbyNameAndDeep(String name,int deep){
        for(SymbolEntry s:this.symbolTable){
            if(s.getName().equals(name)&&s.getDeep()==deep) return s;
        }
        return null;
    }
    public SymbolEntry searchSymbolInFunction(String name,int deep)throws AnalyzeError{
        if (deep>1){
            for (int i=deep;i>1;i--){
                SymbolEntry symbolEntry=searchSymbolbyNameAndDeep(name,i);
                if(symbolEntry!=null) return symbolEntry;
            }
            return null;
        }else throw new AnalyzeError(ErrorCode.SymbolShouldInGlobal,new Pos(0,0));
    }

    public int getSymbolOff(String name){
        for (SymbolEntry symbolEntry:this.symbolTable){
            if (symbolEntry.getName().equals(name)) return symbolEntry.getStackOffset();
        }
        return -1;
    }
    public void addAllInstructions(List<Instruction> instructions) {
        this.instructions.addAll(instructions);
    }
    public void addInstruction(Instruction instruction){this.instructions.add(instruction);}

}
