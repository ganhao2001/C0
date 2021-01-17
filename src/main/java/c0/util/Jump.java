package c0.util;

import c0.instruction.Instruction;
import c0.instruction.Operation;

import java.util.ArrayList;
import java.util.List;

public class Jump {
    public List<Instruction> jumpcon =new ArrayList<>();
    public List<Instruction> br = new ArrayList<>();
    public List<Instruction> block = new ArrayList<>();
    public Instruction jump = new Instruction(Operation.BR,0);
    boolean hasRet =false;
    public Jump(){}

    public List<Instruction> getJumpcon() {
        return jumpcon;
    }

    public List<Instruction> getBr() {
        return br;
    }

    public List<Instruction> getBlock() {
        return block;
    }

    public Instruction getJump() {
        return jump;
    }

    public boolean isHasRet() {
        return hasRet;
    }

    public void setJumpcon(List<Instruction> jumpcon) {
        this.jumpcon = jumpcon;
    }

    public void setBr(List<Instruction> br) {
        this.br = br;
    }

    public void setBlock(List<Instruction> block) {
        this.block = block;
    }

    public void setJump(Instruction jump) {
        this.jump = jump;
    }

    public void setHasRet(boolean hasRet) {
        this.hasRet = hasRet;
    }
    public int getlen(){
        return jumpcon.size()+br.size()+block.size()+(isHasRet()? 0:1);
    }
    public void setJumpOff(int Off){
        this.jump.setOff(Off);
    }
    public List<Instruction> getAllInstruction(){
        List<Instruction> instructions=new ArrayList<>();
        instructions.addAll(jumpcon);
        if(br.size()>0) instructions.addAll(br);
        instructions.addAll(block);
        if(!isHasRet()) instructions.add(jump);
        return instructions;
    }
}
