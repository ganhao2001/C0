package c0.error;

public enum ErrorCode {
    NoError, // Should be only used internally.

    AssignToConstant,
    ConstantNeedValue,
    CannotGetOff,						// 无法获取偏移量
    DuplicateGlobalVar,            		// 变量重复定义
    DuplicateParamName,					// 参数命名重复
    DuplicateFuncName,					// 函数命名重复
    DuplicateName,						// 符号命名重复
    EOF,
    FuncParamsMisMatch,					// 函数参数不匹配
    InvalidInput,                    	// 无效输入
    InvalidIdentifier,               	// 无效标识符
    InvalidEscapeSequence,           	// 无效转义序列
    InvalidDouble,						// 无效浮点数
    InvalidChar,                    	// 无效字符常量
    InvalidVariableDeclaration,
    IntegerOverflow,
    IncompleteExpression,				// 表达不完整
    IncompleteString,                	// 字符串常量左右引号无法对应
    IncompleteChar,                    	// 字符常量左右引号无法对应
    IfElseNotMatch,						// if-else不匹配
    StreamError,
    ShouldReturn,						// 函数需要返回值
    ShouldNotReturn,					// 函数不需要返回值
    ShouldNotBeExist, 					// 本不该存在
    SymbolShouldInGlobal,				// 符号应在全局
    TypeMisMatch, 						// 类型不匹配
    NeedIdentifier,
    NoSemicolon,
    NotComplete, 						// 不完整
    NotDeclared,						// 符号未声明
    NotInitialized,
    InvalidAssignment,
    InvalidPrint,
    InvalidIndent,						// 标识符无效					[语法分析]
    ExpectedToken,
    ExprERROR,
    WithOutMain,							// 无运行入口					[语义分析]
    BreakERROR,
    ASERROR
}
