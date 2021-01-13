
package c0.tokenizer;

public enum TokenType {
    /**空**/
    None,



    /**  无符号整数*/
    UINT_LITERAL,
    /**  字符串常量*/
    STRING_LITERAL,
    /**  浮点数常量*/
    DOUBLE_LITERAL,
    /**  字符常量*/
    CHAR_LITERAL,

    /**标识符*/
    IDENT,

    /** 关键字 */
    /** fn */
    FN_KW,
    /** let */
    LET_KW,
    /** const */
    CONST_KW,
    /** as */
    AS_KW,
    /** while */
    WHILE_KW,
    /** if */
    IF_KW ,
    /** else */
    ELSE_KW,
    /** return */
    RETURN_KW,
    /** break */
    BREAK_KW,
    /** continue */
    CONTINUE_KW,

    /** 加号 */
    PLUS,
    /** 减号 */
    MINUS,
    /** 乘号 */
    MUL,
    /** 除号 */
    DIV,
    /** 等号 */
    EQ,
    /** 赋值 */
    ASSIGN,
    /** 不等号 */
    NEQ,
    /** 小于号 */
    LT,
    /** 大于号 */
    GT,
    /** 小于等于号 */
    LE,
    /** 大于等于号 */
    GE,
    /** 左括号 */
    L_PAREN,
    /** 右括号 */
    R_PAREN,
    /** 左大括号 */
    L_BRACE,
    /** 右大括号 */
    R_BRACE,
    /** 箭头指针 */
    ARROW,
    /** 逗号 */
    COMMA,
    /** 冒号 */
    COLON,
    /** 分号 */
    SEMICOLON,

    /**注释*/
    COMMENT,


    /**类型系统*/
    INT,
    VOID,
    DOUBLE,
    BOOL,

    /** 文件尾 */
    EOF;

    @Override
    public String toString() {
        switch (this) {
            case None:
                return "NullToken";
            case FN_KW:
                return "FN";
            case LET_KW:
                return "LET";
            case CONST_KW:
                return "CONST";
            case AS_KW:
                return "AS";
            case WHILE_KW:
                return "WHILE";
            case IF_KW:
                return "IF";
            case ELSE_KW:
                return "ELSE";
            case RETURN_KW:
                return "RETURN";
            case BREAK_KW:
                return "BREAK";
            case CONTINUE_KW:
                return "CONTINUE";
            case UINT_LITERAL:
                return "UINT";
            case STRING_LITERAL:
                return "STRING";
            case DOUBLE_LITERAL:
                return "DOUBLE";
            case CHAR_LITERAL:
                return "CHAR";
            case IDENT:
                return "IDENT";
            case PLUS:
                return "PLUS";
            case MINUS:
                return "MINUS";
            case MUL:
                return "MUL";
            case DIV:
                return "DIV";
            case ASSIGN:
                return "ASSIGN";
            case EQ:
                return "EQ";
            case NEQ:
                return "NEQ";
            case LT:
                return "LT";
            case GT:
                return "GT";
            case LE:
                return "LE";
            case GE:
                return "GE";
            case L_PAREN:
                return "LPAREN";
            case R_PAREN:
                return "RPAREN";
            case L_BRACE:
                return "LBRACE";
            case R_BRACE:
                return "RBRACE";
            case ARROW:
                return "ARROW";
            case COMMA:
                return "COMMA";
            case COLON:
                return "COLON";
            case SEMICOLON:
                return "SEMICOLON";
            case COMMENT:
                return "COMMENT";
            case INT:
                return "int";
            case VOID:
                return "void";
            case DOUBLE:
                return "double";
            case BOOL:
                return "bool";
            default:
                return "InvalidToken";
        }
    }
}
