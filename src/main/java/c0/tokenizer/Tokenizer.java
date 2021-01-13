
package c0.tokenizer;

import c0.error.TokenizeError;
import c0.error.ErrorCode;
import c0.util.Pos;

import java.util.HashMap;

public class Tokenizer {

    private c0.tokenizer.StringIter it;
    private HashMap<String,TokenType> keywords;
    public Tokenizer(c0.tokenizer.StringIter it) {
        this.it = it;
        keywords = new HashMap<String,TokenType>();
        keywords.put("fn", TokenType.FN_KW);
        keywords.put("let", TokenType.LET_KW);
        keywords.put("const", TokenType.CONST_KW);
        keywords.put("as", TokenType.AS_KW);
        keywords.put("while", TokenType.WHILE_KW);
        keywords.put("if", TokenType.IF_KW);
        keywords.put("else", TokenType.ELSE_KW);
        keywords.put("return", TokenType.RETURN_KW);
        keywords.put("break", TokenType.BREAK_KW);
        keywords.put("continue", TokenType.CONTINUE_KW);
        keywords.put("int", TokenType.INT);
        keywords.put("void", TokenType.VOID);
        keywords.put("double", TokenType.DOUBLE);
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
    /**
     * 获取下一个 Token
     * 
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public c0.tokenizer.Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();
        if (it.isEOF()) {
            return new c0.tokenizer.Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        if (Character.isDigit(peek)) {
            return lexUIntOrDouble();
        } else if (Character.isAlphabetic(peek)||peek=='_') {
            return lexIdentOrKeyword();
        }else if (peek=='\"') {
            return lexStringLiteral();
        }else if (peek=='\'') {
            return lexCharLiteral();
        }else {
            return lexOperatorOrUnknown();
        }
    }
    /*读数字*/
    private c0.tokenizer.Token lexUIntOrDouble() throws TokenizeError {
        Pos startPos,endPos;
        startPos =new Pos(it.currentPos().row,it.currentPos().col);
        String num =new String();
        char peek = it.peekChar();
        while(Character.isDigit(peek)){
            it.nextChar();
            num+=peek;
            peek = it.peekChar();
        }
        /*Double*/
        if(peek=='.'){
            num+=peek;
            it.nextChar();
            peek=it.peekChar();

            boolean isS =false;//科学计数
            boolean isSigned=false;//符号
            if(Character.isDigit(peek)){
                while(Character.isDigit(peek)||peek=='E'||peek=='e'||
                        peek=='+'||peek=='-'){
                    if(peek=='e'||peek=='E'){
                        if(!isS) isS=true;
                        else throw new TokenizeError(ErrorCode.InvalidDouble,it.currentPos());
                    }
                    if(peek=='+'||peek=='-'){
                        if(!isS){
                            if(!isSigned){
                                isSigned=true;
                            }else throw new TokenizeError(ErrorCode.InvalidDouble,it.currentPos());
                        }else throw new TokenizeError(ErrorCode.InvalidDouble,it.currentPos());
                    }
                    num+=peek;
                    it.nextChar();
                    peek=it.peekChar();
                }
            }else throw new TokenizeError(ErrorCode.InvalidDouble,it.currentPos());
            endPos =new Pos(it.currentPos().row,it.currentPos().col);
            Double doublenum=Double.parseDouble(num);
            c0.tokenizer.Token token=new c0.tokenizer.Token(TokenType.DOUBLE_LITERAL,doublenum,startPos,endPos);
            return token;
        }
        /**Uint*/
        else {
            endPos=new Pos(it.currentPos().row,it.currentPos().col);
            Integer intnum=Integer.parseInt(num);
            c0.tokenizer.Token token=new c0.tokenizer.Token(TokenType.UINT_LITERAL,intnum,startPos,endPos);
            return token;
        }
    }

    private c0.tokenizer.Token lexIdentOrKeyword() throws TokenizeError {
        Pos startPos,endPos;
        String IdentOrKY= new String();
        boolean isident=false;
        char peek = it.peekChar();
        if(peek=='_')isident=true;
        IdentOrKY+=peek;
        startPos=new Pos(it.currentPos().row,it.currentPos().col);
        it.nextChar();
        peek=it.peekChar();

        if(Character.isDigit(peek)&&isident==true){
            throw  new TokenizeError(ErrorCode.InvalidIdentifier,it.previousPos());
        }
        while(true){
            if(Character.isLetter(peek)){
                IdentOrKY+=peek;
                it.nextChar();
                peek=it.peekChar();
            }else if(Character.isDigit(peek)||(peek=='_')){
                isident=true;
                IdentOrKY+=peek;
                it.nextChar();
                peek=it.peekChar();
            }else break;
        }
        endPos=new Pos(it.currentPos().row,it.currentPos().col);
        if(isident){
            c0.tokenizer.Token token=new c0.tokenizer.Token(TokenType.IDENT,IdentOrKY,startPos,endPos);
            return token;
        }else {
            if(keywords.containsKey(IdentOrKY)){
                c0.tokenizer.Token token=new c0.tokenizer.Token(keywords.get(IdentOrKY),IdentOrKY,startPos,endPos);
                return token;
            }
            c0.tokenizer.Token token=new c0.tokenizer.Token(TokenType.IDENT,IdentOrKY,startPos,endPos);
            return token;
        }
    }

    private c0.tokenizer.Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.nextChar()) {
            case '+':
                return new c0.tokenizer.Token(TokenType.PLUS, "+", it.previousPos(), it.currentPos());
            case '-':
                if (it.peekChar() == '>') {
                    it.nextChar();
                    return new c0.tokenizer.Token(TokenType.ARROW, "->", it.previousPos(), it.currentPos());
                } else {
                    return new c0.tokenizer.Token(TokenType.MINUS, "-", it.previousPos(), it.currentPos());
                }
            case '*':
                return new c0.tokenizer.Token(TokenType.MUL, "*", it.previousPos(), it.currentPos());
            case '=':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new c0.tokenizer.Token(TokenType.EQ, "==", it.previousPos(), it.currentPos());
                } else {
                    return new c0.tokenizer.Token(TokenType.ASSIGN, "=", it.previousPos(), it.currentPos());
                }
            case '/':
                if (it.peekChar() == '/') {
                    while (it.peekChar()!='\n'){
                        it.nextChar();
                    }
                    it.nextChar();
                    return null;
                } else {
                    return new c0.tokenizer.Token(TokenType.DIV, "/", it.previousPos(), it.currentPos());
                }
            case '!':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new c0.tokenizer.Token(TokenType.NEQ, "!=", it.previousPos(), it.currentPos());
                } else {
                    throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
                }
            case '<':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new c0.tokenizer.Token(TokenType.LE, "<=", it.previousPos(), it.currentPos());
                } else {
                    return new c0.tokenizer.Token(TokenType.LT, "<", it.previousPos(), it.currentPos());
                }
            case '>':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new c0.tokenizer.Token(TokenType.GE, ">=", it.previousPos(), it.currentPos());
                } else {
                    return new c0.tokenizer.Token(TokenType.GT, ">", it.previousPos(), it.currentPos());
                }
            case '(':
                return new c0.tokenizer.Token(TokenType.L_PAREN, "(", it.previousPos(), it.currentPos());
            case ')':
                return new c0.tokenizer.Token(TokenType.R_PAREN, ")", it.previousPos(), it.currentPos());
            case '{':
                return new c0.tokenizer.Token(TokenType.L_BRACE, "{", it.previousPos(), it.currentPos());
            case '}':
                return new c0.tokenizer.Token(TokenType.R_BRACE, "}", it.previousPos(), it.currentPos());
            case ',':
                return new c0.tokenizer.Token(TokenType.COMMA, ",", it.previousPos(), it.currentPos());
            case ':':
                return new c0.tokenizer.Token(TokenType.COLON, ":", it.previousPos(), it.currentPos());
            case ';':
                return new c0.tokenizer.Token(TokenType.SEMICOLON, ";", it.previousPos(), it.currentPos());
            default:
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

//    private Token lexStirngLiteral() throws TokenizeError{
//        Pos startPos,endPos;
//        startPos =new Pos(it.currentPos().row,it.currentPos().col);
//        it.nextChar();
//        char peek=it.peekChar();
//        String string =new String();
//        while (peek!='"'){
//
//        }
//    }
    private c0.tokenizer.Token lexStringLiteral() throws TokenizeError {
        Pos startPos, endPos;
        startPos = new Pos(it.currentPos().row, it.currentPos().col);

        it.nextChar();
        char peek;
        peek=it.peekChar();
        String storage = new String();

        while (peek != '"') {
            // 字符串常量的两头引号对不上号
            if (it.isEOF()) {
                throw new TokenizeError(ErrorCode.IncompleteString, it.previousPos());
            }

            // 判断转义序列 escape_sequence -> '\' [\\"'nrt]
            if (peek == '\\') {
                it.nextChar();
                peek = it.peekChar();
                if (peek == '\\') {

                    storage += '\\';
                }
                else if (peek == '\'') storage += '\'';
                else if (peek == '\"') storage += '\"';
                else if (peek == 'n') storage += '\n';
                else if (peek == 't') storage += '\t';
                else if (peek == 'r') storage += '\r';
                else throw new TokenizeError(ErrorCode.InvalidEscapeSequence, it.previousPos());
            } else {
                storage += peek;
            }

            it.nextChar();
        }

        it.nextChar();
        endPos = new Pos(it.currentPos().row, it.currentPos().col);

        return new c0.tokenizer.Token(TokenType.STRING_LITERAL, storage, startPos, endPos);
    }

    private c0.tokenizer.Token lexCharLiteral() throws TokenizeError {
        Pos startPos, endPos;
        startPos = new Pos(it.currentPos().row, it.currentPos().col);

        it.nextChar();
        Character storage = null;

        char peek;
        peek=it.peekChar();
        if (peek == '\\') {
            it.nextChar();
            peek=it.peekChar();

            if (peek == '\\') storage = '\\';
            else if (peek == '\'') storage = '\'';
            else if (peek == '\"') storage = '\"';
            else if (peek == 'n') storage = '\n';
            else if (peek == 't') storage = '\t';
            else if (peek == 'r') storage = '\r';
            else throw new TokenizeError(ErrorCode.InvalidEscapeSequence, startPos);

            it.nextChar();
            peek=it.peekChar();
        } else if (peek != '\'') {
            storage = it.peekChar();
            it.nextChar();
            peek=it.peekChar();
        } else throw new TokenizeError(ErrorCode.InvalidChar, it.previousPos());

        if (it.peekChar() == '\'') {
            it.nextChar();
            peek=it.peekChar();
            endPos = new Pos(it.currentPos().row, it.currentPos().col);
            return new c0.tokenizer.Token(TokenType.CHAR_LITERAL, storage, startPos, endPos);
        } else {
            throw new TokenizeError(ErrorCode.IncompleteChar, it.previousPos());
        }
    }
    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
}
