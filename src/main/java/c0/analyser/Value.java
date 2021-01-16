package c0.analyser;

import c0.instruction.Instruction;
import c0.tokenizer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Value {
    public List<Instruction> instructions=new ArrayList<>();
    public TokenType tokenType;
    public boolean isConstant;

    public Value(List<Instruction> instructions, TokenType tokenType, boolean isConstant) {
        this.instructions = instructions;
        this.tokenType = tokenType;
        this.isConstant = isConstant;
    }
    public Value(){}
    public List<Instruction> getInstructions() {
        return instructions;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public boolean isConstant() {
        return isConstant;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public void setConstant(boolean constant) {
        isConstant = constant;
    }
}
