
package c0.tokenizer;

import c0.error.TokenizeError;
import c0.error.ErrorCode;
import c0.util.Pos;

import java.util.HashMap;

public class Tokenizer {

    private StringIter it;
    private HashMap<String,TokenType> keywords;
    public Tokenizer(StringIter it) {
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
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();
        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
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
            Token token =lexOperatorOrUnknown();
            if (token==null) return nextToken();
            return token;
        }
    }
    /*读数字*/
    private Token lexUIntOrDouble() throws TokenizeError {
        Pos startPos,endPos;
        startPos =new Pos(it.currentPos().row,it.currentPos().col);
        String num =new String();
        num+=it.peekChar();
        it.nextChar();
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
                        else {
                            System.out.println("111");
                            throw new TokenizeError(ErrorCode.InvalidDouble,it.currentPos());
                        }
                    }
                    if(peek=='+'||peek=='-'){
                        if(!isS){
                            if(!isSigned){
                                isSigned=true;
                            }else {
                                System.out.println("222");
                                throw new TokenizeError(ErrorCode.InvalidDouble,it.currentPos());
                            }
                        }else {
                            System.out.println("333");
                            throw new TokenizeError(ErrorCode.InvalidDouble,it.currentPos());
                        }
                    }
                    num+=peek;
                    it.nextChar();
                    peek=it.peekChar();
                }
            }else {
                System.out.println("444");
                throw new TokenizeError(ErrorCode.InvalidDouble,it.currentPos());
            }
            endPos =new Pos(it.currentPos().row,it.currentPos().col);
            Double doublenum=Double.parseDouble(num);
            Token token=new Token(TokenType.DOUBLE_LITERAL,doublenum,startPos,endPos);
            return token;
        }
        /**Uint*/
        else {
            endPos=new Pos(it.currentPos().row,it.currentPos().col);
            Integer intnum=Integer.parseInt(num);
            Token token=new Token(TokenType.UINT_LITERAL,intnum,startPos,endPos);
            return token;
        }
    }

    private Token lexIdentOrKeyword() throws TokenizeError {
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
            Token token=new Token(TokenType.IDENT,IdentOrKY,startPos,endPos);
            return token;
        }else {
            if(keywords.containsKey(IdentOrKY)){
                Token token=new Token(keywords.get(IdentOrKY),IdentOrKY,startPos,endPos);
                return token;
            }
            Token token=new Token(TokenType.IDENT,IdentOrKY,startPos,endPos);
            return token;
        }
    }

    private Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.PLUS, "+", it.previousPos(), it.currentPos());
            case '-':
                if (it.peekChar() == '>') {
                    it.nextChar();
                    return new Token(TokenType.ARROW, "->", it.previousPos(), it.currentPos());
                } else {
                    return new Token(TokenType.MINUS, "-", it.previousPos(), it.currentPos());
                }
            case '*':
                return new Token(TokenType.MUL, "*", it.previousPos(), it.currentPos());
            case '=':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.EQ, "==", it.previousPos(), it.currentPos());
                } else {
                    return new Token(TokenType.ASSIGN, "=", it.previousPos(), it.currentPos());
                }
            case '/':
                if (it.peekChar() == '/') {
                    it.nextChar();
                    while (it.peekChar()!='\n'){
                        it.nextChar();
                    }
                    return null;
                } else {
                    return new Token(TokenType.DIV, "/", it.previousPos(), it.currentPos());
                }
            case '!':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.NEQ, "!=", it.previousPos(), it.currentPos());
                } else {
                    throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
                }
            case '<':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.LE, "<=", it.previousPos(), it.currentPos());
                } else {
                    return new Token(TokenType.LT, "<", it.previousPos(), it.currentPos());
                }
            case '>':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.GE, ">=", it.previousPos(), it.currentPos());
                } else {
                    return new Token(TokenType.GT, ">", it.previousPos(), it.currentPos());
                }
            case '(':
                return new Token(TokenType.L_PAREN, "(", it.previousPos(), it.currentPos());
            case ')':
                return new Token(TokenType.R_PAREN, ")", it.previousPos(), it.currentPos());
            case '{':
                return new Token(TokenType.L_BRACE, "{", it.previousPos(), it.currentPos());
            case '}':
                return new Token(TokenType.R_BRACE, "}", it.previousPos(), it.currentPos());
            case ',':
                return new Token(TokenType.COMMA, ",", it.previousPos(), it.currentPos());
            case ':':
                return new Token(TokenType.COLON, ":", it.previousPos(), it.currentPos());
            case ';':
                return new Token(TokenType.SEMICOLON, ";", it.previousPos(), it.currentPos());
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
    private Token lexStringLiteral() throws TokenizeError {
        Pos startPos, endPos;
        startPos = new Pos(it.currentPos().row, it.currentPos().col);

        it.nextChar();
        char nextCH;
        String storage = new String();

        while ((nextCH = it.peekChar()) != '"') {
            // 字符串常量的两头引号对不上号
            if (it.isEOF()) {
                throw new TokenizeError(ErrorCode.IncompleteString, it.previousPos());
            }

            // 判断转义序列 escape_sequence -> '\' [\\"'nrt]
            if (nextCH == '\\') {
                it.nextChar();

                if ((nextCH = it.peekChar()) == '\\') {

                    storage += '\\';
                }
                else if (nextCH == '\'') storage += '\'';
                else if (nextCH == '\"') storage += '\"';
                else if (nextCH == 'n') storage += '\n';
                else if (nextCH == 't') storage += '\t';
                else if (nextCH == 'r') storage += '\r';
                else throw new TokenizeError(ErrorCode.InvalidEscapeSequence, it.previousPos());
            } else {
                storage += nextCH;
            }

            it.nextChar();
        }

        it.nextChar();
        endPos = new Pos(it.currentPos().row, it.currentPos().col);

        return new Token(TokenType.STRING_LITERAL, storage, startPos, endPos);
    }

    private Token lexCharLiteral() throws TokenizeError {
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
            return new Token(TokenType.CHAR_LITERAL, storage, startPos, endPos);
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
