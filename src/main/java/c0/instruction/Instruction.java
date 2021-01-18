package c0.instruction;

import java.util.Objects;

public class Instruction {
    private Operation opt;
    long off;
    int len;

    public Instruction(Operation opt) {
        this.opt = opt;
        this.len = 1;
    }

    public Instruction(Operation opt, long off) {
        this.off = off;
        this.opt = opt;
        this.len = 2;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Instruction that = (Instruction) o;
        return opt == that.opt && Objects.equals(off, that.off);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opt, off);
    }

    public Operation getOpt() {
        return opt;
    }

    public void setOpt(Operation opt) {
        this.opt = opt;
    }

    public long getOff() {
        return off;
    }

    public void setOff(long off) {
        this.off = off;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }
    public int getType() {
        switch (this.opt) {
            case PUSH:
                return 0x1;
            case LOCA:
                return 0xa;
            case ARGA:
                return 0xb;
            case GLOBA:
                return 0xc;
            case ADD_I:
                return 0x20;
            case SUB_I:
                return 0x21;
            case MUL_I:
                return 0x22;
            case DIV_I:
                return 0x23;
            case ADD_F:
                return 0x24;
            case SUB_F:
                return 0x25;
            case MUL_F:
                return 0x26;
            case DIV_F:
                return 0x27;
            case STORE_64:
                return 0x17;
            case CMP_I:
                return 0x30;
            case CMP_F:
                return 0x32;
            case NEG_I:
                return 0x34;
            case NEG_F:
                return 0x35;
            case ITOF:
                return 0x36;
            case FTOI:
                return 0x37;
            case SET_LT:
                return 0x39;
            case SET_GT:
                return 0x3a;
            case BR:
                return 0x41;
            case BR_FALSE:
                return 0x42;
            case BR_TRUE:
                return 0x43;
            case CALL:
                return 0x48;
            case RET:
                return 0x49;
            case CALLNAME:
                return 0x4a;
            case LOAD_64:
                return 0x13;
            case STACKALLOC:
                return 0x1a;

            case NOT:
                return 0x2e;

            default:
                return 0xFF;
        }
    }
}
