package c0.analyser;

import c0.error.AnalyzeError;
import c0.error.CompileError;
import c0.error.ErrorCode;
import c0.error.ExpectedTokenError;
import c0.error.TokenizeError;
import c0.instruction.Instruction;
import c0.table.SymbolEntry;
import c0.table.Table;
import c0.tokenizer.Token;
import c0.tokenizer.TokenType;
import c0.tokenizer.Tokenizer;
import c0.util.Pos;

import java.util.*;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    boolean hasReturnValue =false;
    /** 当前偷看的 token */
    Token peekedToken = null;

    /** 符号表 */
    Table table=new Table();
    public Table getTable(){return this.table};
    /** 下一个变量的栈偏移 */
    int nextOffset = 0;
    int deep =1;
    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        table.init();
        analyseProgram();
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
            return tokenizer.nextToken();
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

    private Value analysisExpr(Token left)throws CompileError{
        Value value=new Value();
        List<Instruction> instructions=new ArrayList<>();
        if(left!=null){

        }
        return value;
    }

    private Value analysis





    /**
     * 获取变量是否是常量
     *
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 是否为常量
     * @throws AnalyzeError
     */
    private boolean isConstant(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.isConstant();
        }
    }

    /**
     * <程序> ::= 'begin'<主过程>'end'
     */
    private void analyseProgram() throws CompileError {
//        // 示例函数，示例如何调用子程序
//        // 'begin'
//        expect(TokenType.Begin);
//
//        analyseMain();
//
//        // 'end'
//        expect(TokenType.End);
//        expect(TokenType.EOF);
    }

}
