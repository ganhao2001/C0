package c0.analyser;

import c0.error.AnalyzeError;
import c0.error.CompileError;
import c0.error.ErrorCode;
import c0.error.ExpectedTokenError;
import c0.error.TokenizeError;
import c0.instruction.Instruction;
import c0.instruction.Operation;
import c0.table.Function;
import c0.table.SymbolEntry;
import c0.table.SymbolType;
import c0.table.Table;
import c0.tokenizer.Token;
import c0.tokenizer.TokenType;
import c0.tokenizer.Tokenizer;
import c0.util.Jump;
import c0.util.Pos;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;

import java.util.*;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    boolean hasReturnValue =false;
    /** 当前偷看的 token */
    Token peekedToken = null;

    /** 符号表 */
    Table table=new Table();
    public Table getTable(){return this.table;}
    /** 下一个变量的栈偏移 */
    int nextOffset = 0;
    int deep =1;
    int retdeep=1;
    int breakdeep=1;
    int breakOff[]=new int[10];
    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        table.init();
        analyseProgram();
        next();
        return instructions;
    }

    /**
     * 查看下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            Token next= tokenizer.nextToken();
            return next;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     *
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    private boolean checkExpr()throws TokenizeError{
        if(check(TokenType.UINT_LITERAL) ||
                check(TokenType.DOUBLE_LITERAL) ||
                check(TokenType.STRING_LITERAL) ||
                check(TokenType.CHAR_LITERAL) ||
                check(TokenType.IDENT) ||
                check(TokenType.MINUS) ||
                check(TokenType.L_PAREN)){
            return true;
        }
        return false;
    }
    private boolean checkStmt()throws TokenizeError{
        if(check(TokenType.LET_KW) ||
                check(TokenType.CONST_KW) ||
                check(TokenType.IF_KW) ||
                check(TokenType.WHILE_KW) ||
                check(TokenType.RETURN_KW) ||
                check(TokenType.L_BRACE) ||
                check(TokenType.SEMICOLON) ||
                checkExpr()){
            return true;
        }
        return false;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     *
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     *
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    private Token expect(List<TokenType> tokenTypes)throws CompileError{
        var token = peek();
        for (TokenType tt : tokenTypes) {
            if (token.getTokenType() == tt) {
                return next();
            }
        }
        throw new ExpectedTokenError(tokenTypes, token);
    }

    private TokenType isComparer(Token token){
        if (token.getTokenType() == TokenType.EQ ||
                token.getTokenType() == TokenType.NEQ ||
                token.getTokenType() == TokenType.LT ||
                token.getTokenType() == TokenType.GT ||
                token.getTokenType() == TokenType.LE ||
                token.getTokenType() == TokenType.GE) {
            return token.getTokenType();
        }
        return null;
    }

    private Value analyseExpr(Token front)throws CompileError{
        Value value=new Value();
        List<Instruction> instructions=new ArrayList<>();
        if (front==null){
            if(peek().getTokenType()==TokenType.IDENT){
                front=next();
                if(peek().getTokenType()==TokenType.ASSIGN){
                    next();
                    boolean isGlobal=false;
                    SymbolEntry symbolEntry=table.searchlocalSymbol((String)front.getValue(),this.deep);
                    if (symbolEntry==null) {
                        symbolEntry= table.searchGlobalSymbol((String)front.getValue());
                        isGlobal=true;
                    }
                    if (symbolEntry==null)throw new AnalyzeError(ErrorCode.NotDeclared, front.getStartPos());
                    if(symbolEntry.isConstant()) throw new AnalyzeError(ErrorCode.AssignToConstant, front.getStartPos());
                    if(isGlobal) instructions.add(new Instruction(Operation.GLOBA,symbolEntry.getStackOffset()));
                    else {
                        if(symbolEntry.getSymbolType()==SymbolType.PARAM){
                            instructions.add(new Instruction(Operation.ARGA,symbolEntry.getStackOffset()));
                        }else instructions.add(new Instruction(Operation.LOCA,symbolEntry.getStackOffset()));
                    }
                    Value right=analyseExpr(peek());
                    if(symbolEntry.getTokenType()!=right.tokenType) throw new AnalyzeError(ErrorCode.TypeMisMatch, peek().getStartPos());
                    instructions.addAll(right.instructions);
                    instructions.add(new Instruction(Operation.STORE_64));
                    value.setInstructions(instructions);
                    value.setConstant(false);
                    value.setTokenType(TokenType.VOID);
                }
                else {
                    Value left=analyseCE(front);
                    Value right = analyseBC(left);
                    instructions.addAll(left.instructions);
                    if(right!=null){
                        instructions.addAll(right.instructions);
                    }
                    value.setTokenType(left.tokenType);
                    value.setInstructions(instructions);
                    value.setConstant(true);
                }
            }
            else {
                Value left=analyseCE(peek());
                Value right = analyseBC(left);
                instructions.addAll(left.instructions);
                if(right!=null){
                    instructions.addAll(right.instructions);
                }
                value.setTokenType(left.tokenType);
                value.setInstructions(instructions);
                value.setConstant(true);
            }
        }
        else {
            Value left=analyseCE(front);
            Value right = analyseBC(left);
            instructions.addAll(left.instructions);
            if(right!=null){
                instructions.addAll(right.instructions);
            }
            value.setTokenType(left.tokenType);
            value.setInstructions(instructions);
            value.setConstant(true);
        }
        return value;
    }

    private Value analyseCE(Token front)throws CompileError{
        Value value=new Value();
        List<Instruction> instructions =new ArrayList<>();

        Value left =analyseTE(front);
        instructions.addAll(left.instructions);
        while(peek().getTokenType()==TokenType.PLUS||peek().getTokenType()==TokenType.MINUS){
            Token op=next();
            Value right=analyseTE(peek());
            instructions.addAll(right.instructions);

            if (left.tokenType==TokenType.VOID)
                throw new AnalyzeError(ErrorCode.TypeMisMatch, peek().getStartPos());
            if((left.tokenType==TokenType.INT||left.tokenType==TokenType.UINT_LITERAL)&& (right.tokenType!=TokenType.INT&&right.tokenType!=TokenType.UINT_LITERAL)){
                throw new AnalyzeError(ErrorCode.TypeMisMatch ,peek().getStartPos());
            }
            if((left.tokenType==TokenType.DOUBLE||left.tokenType==TokenType.DOUBLE_LITERAL)&& (right.tokenType!=TokenType.DOUBLE&&right.tokenType!=TokenType.DOUBLE_LITERAL)){
                throw new AnalyzeError(ErrorCode.TypeMisMatch ,peek().getStartPos());
            }
            if (op.getTokenType()==TokenType.PLUS){
                if (left.tokenType==TokenType.INT||left.tokenType==TokenType.UINT_LITERAL){
                    instructions.add(new Instruction(Operation.ADD_I));
                }else if (left.tokenType==TokenType.DOUBLE||left.tokenType==TokenType.DOUBLE_LITERAL){
                    instructions.add(new Instruction(Operation.ADD_F));
                }else throw new AnalyzeError(ErrorCode.ShouldNotBeExist, peek().getStartPos());
            }else {
                if (left.tokenType==TokenType.INT||left.tokenType==TokenType.UINT_LITERAL){
                    instructions.add(new Instruction(Operation.SUB_I));
                }else if (left.tokenType==TokenType.DOUBLE||left.tokenType==TokenType.DOUBLE_LITERAL){
                    instructions.add(new Instruction(Operation.SUB_F));
                }else throw new AnalyzeError(ErrorCode.ShouldNotBeExist, peek().getStartPos());
            }
        }
        value.setTokenType(left.tokenType);
        value.setInstructions(instructions);
        value.setConstant(true);
        return value;

    }

    private Value analyseBC(Value left)throws CompileError{
        Value value=new Value();
        List<Instruction> instructions=new ArrayList<>();
        if(isComparer(peek())!=null){
            Token op =next();

            Value right =analyseCE(peek());
            instructions.addAll(right.instructions);
            if(left.tokenType==TokenType.VOID){
                throw new AnalyzeError(ErrorCode.TypeMisMatch ,peek().getStartPos());
            }
            if((left.tokenType==TokenType.INT||left.tokenType==TokenType.UINT_LITERAL)&& (right.tokenType!=TokenType.INT&&right.tokenType!=TokenType.UINT_LITERAL)){
                throw new AnalyzeError(ErrorCode.TypeMisMatch ,peek().getStartPos());
            }
            if((left.tokenType==TokenType.DOUBLE||left.tokenType==TokenType.DOUBLE_LITERAL)&& (right.tokenType!=TokenType.DOUBLE&&right.tokenType!=TokenType.DOUBLE_LITERAL)){
                throw new AnalyzeError(ErrorCode.TypeMisMatch ,peek().getStartPos());
            }
            if(left.tokenType==TokenType.INT||left.tokenType==TokenType.UINT_LITERAL){
                instructions.add(new Instruction(Operation.CMP_I));
            }else if(left.tokenType==TokenType.DOUBLE||left.tokenType==TokenType.DOUBLE_LITERAL){
                instructions.add(new Instruction(Operation.CMP_F));
            }else {
                throw new AnalyzeError(ErrorCode.ShouldNotBeExist ,new Pos(0,0));
            }
            switch (op.getTokenType()){
                case EQ:instructions.add(new Instruction(Operation.NOT));
                    break;
                case NEQ:break;
                case LT:instructions.add(new Instruction(Operation.SET_LT));
                    break;
                case GT:instructions.add(new Instruction(Operation.SET_GT));
                    break;
                case LE:instructions.add(new Instruction(Operation.SET_GT));
                    instructions.add(new Instruction(Operation.NOT));
                    break;
                case GE:instructions.add(new Instruction(Operation.SET_LT));
                    instructions.add(new Instruction(Operation.NOT));
                    break;
                default:break;
            }
            value.setTokenType(TokenType.BOOL);
            value.setInstructions(instructions);
            value.setConstant(true);
            return value;
        }
        return null;

    }

    private Value analyseTE(Token front)throws CompileError{
        Value value=new Value();
        List<Instruction>instructions=new ArrayList<>();

        Value left =analyseFE(front);
        instructions.addAll(left.instructions);
        while (peek().getTokenType()==TokenType.MUL||peek().getTokenType()==TokenType.DIV){
            Token op =next();
            Value right =analyseFE(peek());
            instructions.addAll(right.instructions);
            if(left.tokenType==TokenType.VOID){
                throw new AnalyzeError(ErrorCode.TypeMisMatch ,new Pos(0,0));
            }
            if((left.tokenType==TokenType.INT||left.tokenType==TokenType.UINT_LITERAL)&& (right.tokenType!=TokenType.INT&&right.tokenType!=TokenType.UINT_LITERAL)){
                throw new AnalyzeError(ErrorCode.TypeMisMatch ,peek().getStartPos());
            }
            if((left.tokenType==TokenType.DOUBLE||left.tokenType==TokenType.DOUBLE_LITERAL)&& (right.tokenType!=TokenType.DOUBLE&&right.tokenType!=TokenType.DOUBLE_LITERAL)){
                throw new AnalyzeError(ErrorCode.TypeMisMatch ,peek().getStartPos());
            }
            if (op.getTokenType()==TokenType.MUL){
                if (left.tokenType==TokenType.INT||left.tokenType==TokenType.UINT_LITERAL){
                    instructions.add(new Instruction(Operation.MUL_I));
                }else if (left.tokenType==TokenType.DOUBLE||left.tokenType==TokenType.DOUBLE_LITERAL){
                    instructions.add(new Instruction(Operation.MUL_F));
                }else throw new AnalyzeError(ErrorCode.ShouldNotBeExist, peek().getStartPos());
            }else {
                if (left.tokenType==TokenType.INT||left.tokenType==TokenType.UINT_LITERAL){
                    instructions.add(new Instruction(Operation.DIV_I));
                }else if (left.tokenType==TokenType.DOUBLE||left.tokenType==TokenType.DOUBLE_LITERAL){
                    instructions.add(new Instruction(Operation.DIV_F));
                }else throw new AnalyzeError(ErrorCode.ShouldNotBeExist, peek().getStartPos());
            }
        }
        value.setConstant(true);
        value.setInstructions(instructions);
        value.setTokenType(left.tokenType);
        return value;
    }

    private Value analyseFE(Token front)throws CompileError{
        Value value=new Value();
        List<Instruction> instructions=new ArrayList<>();

        Value left=analyseAE(front);
        instructions.addAll(left.instructions);
        TokenType needType=left.tokenType;
        while (peek().getTokenType()==TokenType.AS_KW){
            next();
            if (peek().getTokenType()==TokenType.INT){
                if (needType==TokenType.DOUBLE||needType==TokenType.DOUBLE_LITERAL){
                    needType=TokenType.INT;
                    instructions.add(new Instruction(Operation.FTOI));
                }
                next();
            }else if(peek().getTokenType()==TokenType.DOUBLE){
                if (needType==TokenType.INT||needType==TokenType.UINT_LITERAL){
                    needType=TokenType.DOUBLE;
                    instructions.add(new Instruction(Operation.ITOF));
                }
                next();
            }else {
                throw new AnalyzeError(ErrorCode.ASERROR,peek().getStartPos());
            }
        }
        value.setTokenType(needType);
        value.setInstructions(instructions);
        value.setConstant(true);
        return value;
    }

    private Value analyseAE(Token front)throws CompileError{
        Value value=new Value();
        List<Instruction> instructions=new ArrayList<>();
        int notnum=0;
        while (front.getTokenType()==TokenType.MINUS){
            next();
            notnum++;
            front=peek();
        }
        Value IE=analyseIE(front);
        instructions.addAll(IE.instructions);
        if(notnum%2==1){
            if(IE.tokenType==TokenType.VOID){
                throw new AnalyzeError(ErrorCode.TypeMisMatch ,new Pos(0,0));
            }else {
                if(IE.tokenType==TokenType.INT||IE.tokenType==TokenType.UINT_LITERAL||IE.tokenType==TokenType.STRING_LITERAL||IE.tokenType==TokenType.CHAR_LITERAL){
                    instructions.add(new Instruction(Operation.NEG_I));
                }else if(IE.tokenType==TokenType.DOUBLE||IE.tokenType==TokenType.DOUBLE_LITERAL){
                    instructions.add(new Instruction(Operation.NEG_F));
                }else throw new AnalyzeError(ErrorCode.ShouldNotBeExist ,new Pos(0,0));
            }
        }
        value.setConstant(true);
        value.setInstructions(instructions);
        value.setTokenType(IE.tokenType);
        return value;
    }

    private Value analyseIE(Token front)throws CompileError{
        Value value=new Value();
        List<Instruction>instructions =new ArrayList<>();
        if (front.getTokenType()==TokenType.IDENT)
        {
            Token nexttoken=next();
            if (nexttoken.getTokenType()==TokenType.L_PAREN||peek().getTokenType()==TokenType.L_PAREN){
                if(peek().getTokenType()==TokenType.L_PAREN){
                    next();
                }
                int s=getStandardFunctionID((String)front.getValue());
                if (s!=-1){
                    List<Instruction> instructions1 =new ArrayList<>();
                    switch (s){
                        case 0:
                            expect(TokenType.R_PAREN);
                            instructions1.add(new Instruction(Operation.STACKALLOC,1));
                            instructions1.add(new Instruction(Operation.CALLNAME,0));
                            value.setTokenType(TokenType.INT);
                            value.setInstructions(instructions1);
                            value.setConstant(true);
                            break;
                        case 1:
                            expect(TokenType.R_PAREN);
                            instructions1.add(new Instruction(Operation.STACKALLOC, 1));
                            instructions1.add(new Instruction(Operation.CALLNAME,1));
                            value.setTokenType(TokenType.DOUBLE);
                            value.setInstructions(instructions1);
                            value.setConstant(true);
                            break;
                        case 2:
                            expect(TokenType.R_PAREN);
                            instructions1.add(new Instruction(Operation.STACKALLOC,1));
                            instructions1.add(new Instruction(Operation.CALLNAME,2));
                            value.setTokenType(TokenType.INT);
                            value.setInstructions(instructions1);
                            value.setConstant(true);
                            break;
                        case 3:
                            Value Int =analyseExpr(peek());
                            if(Int.tokenType!=TokenType.INT){
                                throw new AnalyzeError(ErrorCode.TypeMisMatch, peek().getStartPos());
                            }
                            expect(TokenType.R_PAREN);
                            instructions1.add(new Instruction(Operation.STACKALLOC,0));
                            instructions1.addAll(Int.instructions);
                            instructions1.add(new Instruction(Operation.CALLNAME,3));
                            value.setTokenType(TokenType.VOID);
                            value.setInstructions(instructions1);
                            value.setConstant(true);
                            break;
                        case 4:
                            Value ADouble =analyseExpr(peek());
                            if(ADouble.tokenType!=TokenType.DOUBLE){
                                throw new AnalyzeError(ErrorCode.TypeMisMatch, peek().getStartPos());
                            }
                            expect(TokenType.R_PAREN);
                            instructions1.add(new Instruction(Operation.STACKALLOC,0));
                            instructions1.addAll(ADouble.instructions);
                            instructions1.add(new Instruction(Operation.CALLNAME,4));
                            value.setTokenType(TokenType.VOID);
                            value.setInstructions(instructions1);
                            value.setConstant(true);
                            break;
                        case 5:
                            Value AChar =analyseExpr(peek());
                            if(AChar.tokenType!=TokenType.CHAR_LITERAL&&AChar.tokenType!=TokenType.INT){
                                throw new AnalyzeError(ErrorCode.TypeMisMatch, peek().getStartPos());
                            }
                            expect(TokenType.R_PAREN);
                            instructions1.add(new Instruction(Operation.STACKALLOC,0));
                            instructions1.addAll(AChar.instructions);
                            instructions1.add(new Instruction(Operation.CALLNAME,5));
                            value.setTokenType(TokenType.VOID);
                            value.setInstructions(instructions1);
                            value.setConstant(true);
                            break;
                        case 6:
                            Value string =analyseExpr(peek());
                            if(string.tokenType!=TokenType.STRING_LITERAL&&string.tokenType!=TokenType.INT){
                                throw new AnalyzeError(ErrorCode.TypeMisMatch, peek().getStartPos());
                            }
                            expect(TokenType.R_PAREN);
                            instructions1.add(new Instruction(Operation.STACKALLOC,0));
                            instructions1.addAll(string.instructions);
                            instructions1.add(new Instruction(Operation.CALLNAME, 6));
                            value.setTokenType(TokenType.VOID);
                            value.setInstructions(instructions1);
                            value.setConstant(true);
                            break;
                        case 7:

                            expect(TokenType.R_PAREN);
                            instructions1.add(new Instruction(Operation.STACKALLOC,0));
                            instructions1.add(new Instruction(Operation.CALLNAME, 7));
                            value.setTokenType(TokenType.VOID);
                            value.setInstructions(instructions1);
                            value.setConstant(true);
                            break;
                        default:throw new AnalyzeError(ErrorCode.ShouldNotBeExist, peek().getStartPos());
                    }
                }
                else {
                    Function function=table.searchFunction((String) front.getValue());
                    if(function==null){
                        throw new AnalyzeError(ErrorCode.NotDeclared, front.getStartPos());
                    }
                    if(function.getType()!=TokenType.VOID){
                        instructions.add(new Instruction(Operation.STACKALLOC,1));
                    }else {
                        instructions.add(new Instruction(Operation.STACKALLOC,0));
                    }
                    if(peek().getTokenType()!=TokenType.R_PAREN){
                        List<Value> params=new ArrayList<>();
                        do{
                            Value param=analyseExpr(peek());
                            params.add(param);
                            instructions.addAll(param.instructions);
                        }while (nextIf(TokenType.COMMA)!=null);
                        if(function.getParamsnum()!=params.size()){
                            throw new AnalyzeError(ErrorCode.FuncParamsMisMatch, peek().getStartPos());
                        }
                        int hasreturn =0;
                        if(function.getType()!=TokenType.VOID){
                            hasreturn=1;
                        }
                        for(int i=0;i<params.size();i++){
                            if(params.get(i).tokenType!=function.getSymbolTable().get(i+hasreturn).getTokenType()){
                               throw new AnalyzeError(ErrorCode.FuncParamsMisMatch, peek().getStartPos());
                            }
                        }
                        expect(TokenType.R_PAREN);
                    }else {
                        expect(TokenType.R_PAREN);
                    }
                    int ID=table.getFunctionID((String)front.getValue());
                    instructions.add(new Instruction(Operation.CALL,ID));
                    value.setTokenType(function.getType());
                    value.setInstructions(instructions);
                    value.setConstant(true);
                }
            }
            else if(peek().getTokenType()!=TokenType.L_PAREN){
                boolean isGlobal=false;
                SymbolEntry symbolEntry=table.searchlocalSymbol((String)front.getValue(),this.deep);
                if (symbolEntry==null) {
                    symbolEntry= table.searchGlobalSymbol((String)front.getValue());
                    isGlobal=true;
                }
                if(symbolEntry==null) throw new AnalyzeError(ErrorCode.NotDeclared, front.getStartPos());
                if(!symbolEntry.isInitialized()){
                    throw new AnalyzeError(ErrorCode.NotInitialized, front.getStartPos());
                }
                if(isGlobal){
                    instructions.add(new Instruction(Operation.GLOBA,symbolEntry.getStackOffset()));
                }
                else {
                    if(symbolEntry.getSymbolType()==SymbolType.PARAM)
                        instructions.add(new Instruction(Operation.ARGA,symbolEntry.getStackOffset()));
                    else instructions.add(new Instruction(Operation.LOCA,symbolEntry.getStackOffset()));
                }
                instructions.add(new Instruction(Operation.LOAD_64));
                TokenType ctype =symbolEntry.getTokenType();
                if(peek().getTokenType()==TokenType.AS_KW){
                    next();
                    Token token=expect(List.of(TokenType.INT,TokenType.DOUBLE));
                    if(symbolEntry.getTokenType()==TokenType.INT&&token.getTokenType()==TokenType.DOUBLE){
                        ctype=TokenType.DOUBLE;
                        instructions.add(new Instruction(Operation.ITOF));
                    }
                    if(symbolEntry.getTokenType()==TokenType.DOUBLE&&token.getTokenType()==TokenType.INT){
                        ctype=TokenType.INT;
                        instructions.add(new Instruction(Operation.FTOI));
                    }
                }
                value.setConstant(symbolEntry.isConstant());
                value.setInstructions(instructions);
                value.setTokenType(ctype);
            }
        }else if (front.getTokenType()==TokenType.UINT_LITERAL)
        {
            Token Uint=expect(TokenType.UINT_LITERAL);
            Integer i=(Integer) Uint.getValue();
            long it01=i;
            instructions.add(new Instruction(Operation.PUSH,it01));
            value.setTokenType(TokenType.INT);
            value.setConstant(true);
            value.setInstructions(instructions);
        }else if (front.getTokenType()==TokenType.DOUBLE_LITERAL)
        {
            Token dtoken=expect(TokenType.DOUBLE_LITERAL);
            Double d=(Double) dtoken.getValue();
            long li=Double.doubleToLongBits(d);
            instructions.add(new Instruction(Operation.PUSH,li));
            value.setTokenType(TokenType.DOUBLE_LITERAL);
            value.setConstant(true);
            value.setInstructions(instructions);
        }else if(front.getTokenType()==TokenType.STRING_LITERAL)
        {
            Token string=next();
            table.addSymbol((String)string.getValue(),TokenType.STRING_LITERAL, SymbolType.STRING,1,true,true);
            long off=table.getSymbolOff((String) front.getValue());
            instructions.add(new Instruction(Operation.PUSH,off));
            value.setInstructions(instructions);
            value.setConstant(true);
            value.setTokenType(TokenType.INT);

        }else if(front.getTokenType()==TokenType.CHAR_LITERAL)
        {
            instructions.add(new Instruction(Operation.PUSH,(char) front.getValue()));
            next();
            value.setTokenType(TokenType.INT);
            value.setConstant(true);
            value.setInstructions(instructions);
        }else if(front.getTokenType()==TokenType.L_PAREN)
        {
            next();
            value=analyseExpr(peek());
            expect(TokenType.R_PAREN);
        }else {
            System.out.println(peek().toString());
            System.out.println(front.toString());
            throw new AnalyzeError(ErrorCode.ExprERROR, peek().getEndPos());
        }
        return value;
    }
    public int getStandardFunctionID(String name){
        if (name.equals("getint")) return 0;
        if (name.equals("getdouble")) return 1;
        if (name.equals("getchar")) return 2;
        if (name.equals("putint")) return 3;
        if (name.equals("putdouble")) return 4;
        if (name.equals("putchar")) return 5;
        if (name.equals("putstr")) return 6;
        if (name.equals("putln")) return 7;
        return -1;
    }

    private List<Instruction> analyseStmt()throws CompileError{
        List<Instruction> instructions=new ArrayList<>();

        if(check(TokenType.ELSE_KW)){
            throw new AnalyzeError(ErrorCode.IfElseNotMatch, peek().getStartPos());
        }
        if (check(TokenType.BREAK_KW)&&breakdeep==1) throw new AnalyzeError(ErrorCode.BreakERROR,new Pos(0,0));
        while(checkStmt()){
            if (check(TokenType.LET_KW) || check(TokenType.CONST_KW)) instructions.addAll(analyseDeclStmt(false));
            else if (check(TokenType.IF_KW)) instructions.addAll(analyseIfStmt());
            else if (check(TokenType.WHILE_KW)) instructions.addAll(analyseWhileStmt());
            else if (check(TokenType.RETURN_KW)) instructions.addAll(analyseReturnStmt());
            else if (check(TokenType.L_BRACE)) instructions.addAll(analyseBlockStmt());
            else if (check(TokenType.SEMICOLON)) analyseEmptyStmt();
            else if (check(TokenType.BREAK_KW)) break;
            else if (check(TokenType.CONTINUE_KW)) break;
            else instructions.addAll(analyseExprStmt());
        }

        return instructions;
    }
    private List<Instruction> analyseLetDeclStmt(boolean isGlobal)throws CompileError{
        expect(TokenType.LET_KW);
        Token ident=expect(TokenType.IDENT);
        expect(TokenType.COLON);
        List<Instruction> instructions=new ArrayList<>();
        List<Instruction> analysis=new ArrayList<>();

        Token type =expect(List.of(TokenType.INT,TokenType.DOUBLE));
        boolean isInit =false;
        if(!check(TokenType.SEMICOLON)){
            expect(TokenType.ASSIGN);
            Value right =analyseExpr(peek());
            analysis.addAll(right.instructions);
            if(type.getTokenType()==TokenType.INT){
                if (right.tokenType!=TokenType.UINT_LITERAL&&right.tokenType!=TokenType.INT)
                    throw new  AnalyzeError(ErrorCode.TypeMisMatch, peek().getStartPos());
            }else if(type.getTokenType()==TokenType.DOUBLE){
                if(right.tokenType!=TokenType.DOUBLE_LITERAL&&right.tokenType!=TokenType.DOUBLE){
                    throw new  AnalyzeError(ErrorCode.TypeMisMatch, peek().getStartPos());
                }
            }
            isInit =true;
        }
        if(isGlobal){
            table.addSymbol((String) ident.getValue(),type.getTokenType(),SymbolType.VAR,deep,false,true);
            if(isInit){
                long Off =table.getSymbolOff((String) ident.getValue());
                table.getInstructions().add(new Instruction(Operation.GLOBA,Off));
                table.getInstructions().addAll(analysis);
                table.getInstructions().add(new Instruction(Operation.STORE_64));
            }
        }else {
            table.addFuctionSymbol((String)ident.getValue(),type.getTokenType(),deep,false,true);
            if(isInit){
                long Off =table.getFunctionTable().get(table.getFunctionTable().size()-1).getSymbolOff((String) ident.getValue(),deep);

                instructions.add(new Instruction(Operation.LOCA,Off));
                instructions.addAll(analysis);
                instructions.add(new Instruction(Operation.STORE_64));
            }
        }
        expect(TokenType.SEMICOLON);
        return instructions;
    }
    private List<Instruction> analyseConstDeclStmt(boolean isGlobal)throws CompileError{
        expect(TokenType.CONST_KW);
        Token ident = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        Token type = expect(List.of(TokenType.INT, TokenType.DOUBLE));
        expect(TokenType.ASSIGN);
        List<Instruction> instructions=new ArrayList<>();
        List<Instruction> analysis=new ArrayList<>();
        Value right=analyseExpr(peek());
        analysis.addAll(right.instructions);
        if(type.getTokenType()==TokenType.INT){
            if (right.tokenType!=TokenType.UINT_LITERAL&&right.tokenType!=TokenType.INT)
                throw new  AnalyzeError(ErrorCode.TypeMisMatch, peek().getStartPos());
        }else if(type.getTokenType()==TokenType.DOUBLE){
            if(right.tokenType!=TokenType.DOUBLE_LITERAL&&right.tokenType!=TokenType.DOUBLE){
                throw new  AnalyzeError(ErrorCode.TypeMisMatch, peek().getStartPos());
            }
        }

        if(isGlobal){
            table.addSymbol((String) ident.getValue(),type.getTokenType(),SymbolType.VAR,deep,true,true);
            long Off =table.getSymbolOff((String) ident.getValue());
            table.getInstructions().add(new Instruction(Operation.GLOBA,Off));
            table.getInstructions().addAll(analysis);
            table.getInstructions().add(new Instruction(Operation.STORE_64));
        }else {
            table.addFuctionSymbol((String)ident.getValue(),type.getTokenType(),deep,true,true);
            long Off =table.getFunctionTable().get(table.getFunctionTable().size()-1).getSymbolOff((String) ident.getValue(),deep);
            instructions.add(new Instruction(Operation.LOCA,Off));
            instructions.addAll(analysis);
            instructions.add(new Instruction(Operation.STORE_64));
        }
        expect(TokenType.SEMICOLON);
        return instructions;
    }
    private List<Instruction> analyseDeclStmt(boolean isGlobal)throws CompileError{
        List<Instruction> instructions=new ArrayList<>();
        if(check(TokenType.LET_KW)){
            instructions.addAll(analyseLetDeclStmt(isGlobal));
        }else if(check(TokenType.CONST_KW)){
            instructions.addAll(analyseConstDeclStmt(isGlobal));
        }
        return instructions;
    }
    private List<Instruction> analyseIfStmt()throws CompileError{
        List<Instruction> instructions=new ArrayList<>();
        List<Jump> jumps=new ArrayList<>();
        expect(TokenType.IF_KW);
        Value jumpcondition =analyseExpr(peek());
        List<Instruction> block =analyseBlockStmt();
        boolean hasRet=false;
        if(block.size()>0&&block.get(block.size()-1).getOpt()==Operation.RET){
            hasRet=true;
        }
        Jump ifJump=new Jump();
        ifJump.setBlock(block);
        ifJump.setJumpcon(jumpcondition.instructions);
        ifJump.br.add(new Instruction(Operation.BR_TRUE,1));
        ifJump.br.add(new Instruction(Operation.BR,(long)block.size()+(hasRet?0:1)));
        ifJump.setHasRet(hasRet);
        jumps.add(ifJump);
        hasRet=false;
        while(check(TokenType.ELSE_KW)){
            next();
            if (check(TokenType.IF_KW)){
                next();
                jumpcondition=analyseExpr(peek());
                block=analyseBlockStmt();
                if(block.size()>0&&block.get(block.size()-1).getOpt()==Operation.RET){
                    hasRet=true;
                }
                Jump ifJump2=new Jump();
                ifJump2.setBlock(block);
                ifJump2.setJumpcon(jumpcondition.instructions);
                ifJump2.br.add(new Instruction(Operation.BR_TRUE,1));
                ifJump2.br.add(new Instruction(Operation.BR,(long)block.size()+(hasRet?0:1)));
                ifJump2.setHasRet(hasRet);
                jumps.add(ifJump2);
                hasRet=false;
            }
            else {
                block=analyseBlockStmt();
                if(block.size()>0&&block.get(block.size()-1).getOpt()==Operation.RET){
                    hasRet=true;
                    hasReturnValue =true;
                }
                Jump elseJump =new Jump();
                elseJump.setBlock(block);
                elseJump.setHasRet(hasRet);
                jumps.add(elseJump);
                break;
            }
        }
        int len=0;
        for(int i=jumps.size()-1;i>0;i--){
            int ajumplen=jumps.get(i).getlen();
            len+=ajumplen;
            if(!jumps.get(i-1).isHasRet()){
                jumps.get(i-1).setJumpOff(len);
            }
        }
        for(int i=-0;i<jumps.size();i++){
            instructions.addAll(jumps.get(i).getAllInstruction());
        }
        return instructions;
    }
    private List<Instruction> analyseWhileStmt()throws CompileError{
        List<Instruction> instructions=new ArrayList<>();
        expect(TokenType.WHILE_KW);
        boolean hasRet=false;
        breakdeep++;
        Value whilecondition =analyseExpr(peek());
        List<Instruction> block =analyseBlockStmt(whilecondition.instructions.size());
        instructions.addAll(whilecondition.instructions);
        instructions.add(new Instruction(Operation.BR_TRUE,1));
        instructions.add(new Instruction(Operation.BR,block.size()+1));
        instructions.addAll(block);
        instructions.add(new Instruction(Operation.BR,(-(whilecondition.instructions.size()+block.size()+3))));
        breakdeep--;
        return instructions;
    }

private List<Instruction> analyseBlockStmt(int len)throws CompileError{
    expect(TokenType.L_BRACE);
    List<Instruction> instructions=new ArrayList<>();
    this.deep++;
    int whileOff=0;
    while (!check(TokenType.R_BRACE)){
        if(check(TokenType.EOF)) throw new AnalyzeError(ErrorCode.EOF,peek().getStartPos());
        List<Instruction> S=analyseStmt();
        whileOff+=S.size();
        instructions.addAll(S);
        if(check(TokenType.CONTINUE_KW)){
            instructions.add(new Instruction(Operation.BR,-(len+whileOff+3)));
            next();
        }
        if (check(TokenType.BREAK_KW)){
            breakOff[breakdeep]=whileOff;
            Instruction breakIn=new Instruction(Operation.BR,0);
            instructions.add(breakIn);
            next();
        }
    }
    instructions.get(breakOff[breakdeep]).setOff(instructions.size()-breakOff[breakdeep]);
    expect(TokenType.R_BRACE);
    if(deep==2){
        if(table.getFunctionTable().get(table.getFunctionTable().size()-1).getType()!=TokenType.VOID&&!hasReturnValue){
            throw new AnalyzeError(ErrorCode.ShouldReturn, peek().getStartPos());
        }
    }
    this.deep--;
    return instructions;

}
    private List<Instruction> analyseBlockStmt()throws CompileError{
        expect(TokenType.L_BRACE);
        if (peek().getTokenType().ordinal() == 40)
            throw new AnalyzeError(ErrorCode.NotComplete, peek().getStartPos());
        List<Instruction> instructions=new ArrayList<>();
        this.deep++;
        int brace=1;
        while (!check(TokenType.R_BRACE)){
            List<Instruction> S=analyseStmt();
            instructions.addAll(S);
            if (check(TokenType.BREAK_KW)||check(TokenType.CONTINUE_KW)){
                throw new AnalyzeError(ErrorCode.BreakERROR,peek().getStartPos());
            }

            if(check(TokenType.EOF)) throw new AnalyzeError(ErrorCode.EOF,peek().getStartPos());
        }
        if(brace==1)expect(TokenType.R_BRACE);
        if(deep==2){
            if(table.getFunctionTable().get(table.getFunctionTable().size()-1).getType()!=TokenType.VOID&&!hasReturnValue){
                throw new AnalyzeError(ErrorCode.ShouldReturn, peek().getStartPos());
            }
        }
        this.deep--;
        return instructions;

    }

    private List<Instruction> analyseReturnStmt()throws CompileError{
        expect(TokenType.RETURN_KW);
        if(this.deep==2) hasReturnValue=true;
        List<Instruction> instructions=new ArrayList<>();
        TokenType type=table.getFunctionTable().get(table.getFunctionTable().size()-1).getType();
        if(type==TokenType.VOID){
            if(peek().getTokenType()!=TokenType.SEMICOLON){
                throw new AnalyzeError(ErrorCode.ShouldNotReturn, peek().getStartPos());
            }
        }
        else {
            instructions.add(new Instruction(Operation.ARGA,0));
            if(peek().getTokenType()==TokenType.SEMICOLON){
                throw new AnalyzeError(ErrorCode.ShouldReturn, peek().getStartPos());
            }
            Value right =analyseExpr(peek());
            if(type==TokenType.INT&&right.tokenType!=TokenType.INT&&right.tokenType!=TokenType.UINT_LITERAL){
                throw new AnalyzeError(ErrorCode.TypeMisMatch, peek().getStartPos());
            }
            if(type==TokenType.DOUBLE&&right.tokenType!=TokenType.DOUBLE&&right.tokenType!=TokenType.DOUBLE_LITERAL){
                throw new AnalyzeError(ErrorCode.TypeMisMatch, peek().getStartPos());
            }
            instructions.addAll(right.instructions);
            instructions.add(new Instruction(Operation.STORE_64));
        }
        expect(TokenType.SEMICOLON);
        instructions.add(new Instruction(Operation.RET));
        return instructions;
    }

    private void analyseEmptyStmt()throws CompileError{
        expect(TokenType.SEMICOLON);
    }

    private List<Instruction> analyseExprStmt()throws CompileError{
        List<Instruction> instructions=new ArrayList<>();
        instructions.addAll(analyseExpr(null).instructions);
        expect(TokenType.SEMICOLON);
        return instructions;
    }

    private void analyseFunctionParam()throws CompileError{
        if(check(TokenType.CONST_KW)){
            next();
        }
        Token param =expect(TokenType.IDENT);
        expect(TokenType.COLON);
        Token type=expect(List.of(TokenType.VOID,TokenType.INT,TokenType.DOUBLE));
        table.addParam((String)param.getValue(),type);
    }

    private void analyseFunctionParamList()throws CompileError{
        do{
            analyseFunctionParam();
        }while (nextIf(TokenType.COMMA)!=null);
    }

    private void analyseFunction()throws CompileError{
        List<Instruction>instructions=new ArrayList<>();
        expect(TokenType.FN_KW);
        Token function=expect(TokenType.IDENT);
        expect(TokenType.L_PAREN);
        table.addFunction((String)function.getValue());
        if(!check(TokenType.R_PAREN)) analyseFunctionParamList();
        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);
        Token type=expect(List.of(TokenType.VOID,TokenType.INT,TokenType.DOUBLE));
        if(type.getTokenType()!=TokenType.VOID){
            table.setFunctionType(type.getTokenType());
        }
        instructions.addAll(analyseBlockStmt());
        hasReturnValue=false;
        if(type.getTokenType()==TokenType.VOID){
            instructions.add(new Instruction(Operation.RET));
        }
        table.addInstructionsToFunction(instructions);
    }


    private void analyseProgram() throws CompileError {
        while(check(TokenType.LET_KW)||check(TokenType.CONST_KW)||check(TokenType.FN_KW)){
            while (check(TokenType.LET_KW)||check(TokenType.CONST_KW)){
                table.addInstructions(analyseDeclStmt(true));
            }
            while (check(TokenType.FN_KW)){
                analyseFunction();
            }
        }
        Function main =table.searchFunction("main");
        if(main==null)throw new AnalyzeError(ErrorCode.WithOutMain, new Pos(0,0));
        else {
            if(main.getType()==TokenType.VOID){
                table.addInstruction(new Instruction(Operation.CALL,table.getFunctionID("main")));
            }else {
                table.addInstruction(new Instruction(Operation.STACKALLOC,1));
                table.addInstruction(new Instruction(Operation.CALL,table.getFunctionID("main")));
            }
        }
        Function _start =new Function("_start",TokenType.VOID,0,0);
        _start.setInstructions(table.getInstructions());
        table.addStartFunction(_start);
        table.addSymbol("main",main.getType(),SymbolType.FUNC,1,false,true);
        table.addSymbol("_start",_start.getType(),SymbolType.FUNC,1,false,true);
    }

}
