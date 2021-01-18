package c0.table;

import c0.error.AnalyzeError;
import c0.error.ErrorCode;
import c0.instruction.Instruction;
import c0.tokenizer.Token;
import c0.tokenizer.TokenType;
import c0.util.Pos;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class Table {
    private List<Function> functionTable;
    private List<SymbolEntry> symbolTable;
    private List<Instruction> instructions;
    public Table() {
        this.functionTable = new ArrayList<>();
        this.symbolTable = new ArrayList<>();
        this.instructions = new ArrayList<>();
    }
    public void init(){
        symbolTable.add(new SymbolEntry("getint",TokenType.INT,SymbolType.FUNC,0,1,true,true));
        symbolTable.add(new SymbolEntry("getdouble",TokenType.DOUBLE,SymbolType.FUNC,1,1,true,true));
        symbolTable.add(new SymbolEntry("getchar",TokenType.INT,SymbolType.FUNC,2,1,true,true));
        symbolTable.add(new SymbolEntry("putint",TokenType.VOID,SymbolType.FUNC,3,1,true,true));
        symbolTable.add(new SymbolEntry("putdouble",TokenType.VOID,SymbolType.FUNC,4,1,true,true));
        symbolTable.add(new SymbolEntry("putchar",TokenType.VOID,SymbolType.FUNC,5,1,true,true));
        symbolTable.add(new SymbolEntry("putstr",TokenType.VOID,SymbolType.FUNC,6,1,true,true));
        symbolTable.add(new SymbolEntry("putln",TokenType.VOID,SymbolType.FUNC,7,1,true,true));
    }
    public SymbolEntry searchGlobalSymbol(String name){
        for(SymbolEntry symbolEntry: this.symbolTable){
            if (symbolEntry.getName().equals(name))return symbolEntry;
        }
        return null;
    }
    public Function searchFunction(String name){
        for (Function function:this.functionTable){
            if(function.getName().equals(name))return function;
        }
        return null;
    }
    public SymbolEntry searchlocalSymbol(String name,int deep)throws AnalyzeError{
        return this.functionTable.get(this.functionTable.size()-1).searchSymbolInFunction(name,deep);
    }
    public int getFunctionID(String name){
        for(int i=0;i<this.functionTable.size();i++){
            if(this.functionTable.get(i).getName().equals(name))return i+1;
        }
        return 0;
    }
    public void addFunction(String name)throws AnalyzeError{
        for(Function function:this.functionTable){
            if(function.getName().equals(name)){
                throw new AnalyzeError(ErrorCode.DuplicateFuncName,new Pos(0,0));
            }
        }
        this.functionTable.add(new Function(name));
    }
    public void addStartFunction(Function function){
        this.functionTable.add(0,function);
    }
    public void setFunctionType(TokenType type){
        if(type!=TokenType.VOID){
            this.functionTable.get(this.functionTable.size()-1).addReturnSlot(type);
        }
        this.functionTable.get(this.functionTable.size()-1).setType(type);
    }
    public void addParam(String name, Token token)throws AnalyzeError{
        this.functionTable.get(this.functionTable.size()-1).addParam(name,token.getTokenType());
    }
    public void addSymbol(String name,TokenType tokenType,SymbolType symbolType,int deep,boolean isConstant, boolean isIitialized)throws AnalyzeError{
        if(searchGlobalSymbol(name)!=null&&searchGlobalSymbol(name).getTokenType()!=TokenType.STRING_LITERAL){
            throw new AnalyzeError(ErrorCode.DuplicateGlobalVar,new Pos(0,0));
        }
        this.symbolTable.add(new SymbolEntry(name,tokenType,symbolType,this.symbolTable.size(),deep,isConstant,isIitialized));
    }
    public void addFuctionSymbol(String name,TokenType tokenType,int deep,boolean isConstant, boolean isIitialized)throws AnalyzeError{
        this.functionTable.get(this.functionTable.size()-1).addVar(name,tokenType,deep,isConstant,isIitialized);
    }
    public int getSymbolOff(String name){
        for (SymbolEntry symbolEntry:this.symbolTable){
            if (symbolEntry.getName().equals(name))return symbolEntry.getStackOffset();
        }
        return -1;
    }
    public void addInstructionsToFunction(List<Instruction> instructions){
        Function function=this.functionTable.get(this.functionTable.size()-1);
        function.addAllInstructions(instructions);
    }
    public void addInstructionToFunc(Instruction instruction) {
        Function funcEntry = this.functionTable.get(this.functionTable.size() - 1);
        funcEntry.addInstruction(instruction);
    }
    public void addInstructions(List<Instruction> instructions){
        this.instructions.addAll(instructions);
    }
    public void addInstruction(Instruction instruction){
        this.instructions.add(instruction);
    }

    public List<Function> getFunctionTable() {
        return functionTable;
    }

    public List<SymbolEntry> getSymbolTable() {
        return symbolTable;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setFunctionTable(List<Function> functionTable) {
        this.functionTable = functionTable;
    }

    public void setSymbolTable(List<SymbolEntry> symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }
}
