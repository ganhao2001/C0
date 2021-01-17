package c0.util;

import c0.instruction.Instruction;
import c0.table.Function;
import c0.table.SymbolEntry;
import c0.table.SymbolType;
import c0.table.Table;
import c0.tokenizer.TokenType;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

public class ToBinary {
    Table table;
    List<Byte> out;
    public ToBinary(Table table){
        this.table=table;
        out=new ArrayList<>();
    }
    public List<Byte> toBinary(){
        List<Byte> magic =uint2bytes(4,0x72303b3e);
        out.addAll(magic);
        List<Byte> version =uint2bytes(4,0x00000001);
        out.addAll(version);
        List<SymbolEntry> globalSymbols =table.getSymbolTable();
        List<Byte> globalnum=uint2bytes(4,globalSymbols.size());
        out.addAll(globalnum);
        for (int i=0;i<globalSymbols.size();i++){
            SymbolEntry symbolEntry=globalSymbols.get(i);
            List<Byte> isConst =uint2bytes(1,symbolEntry.isConstant()?1:0);
            out.addAll(isConst);
            List<Byte> bytes=new ArrayList<>();
            if(symbolEntry.getSymbolType()== SymbolType.FUNC||symbolEntry.getSymbolType()==SymbolType.STRING){
                bytes =string2bytes(symbolEntry.getName());
            }else if(symbolEntry.getTokenType()== TokenType.INT){
                bytes=uint2bytes(8,0);
            }
            List<Byte> valueCount=uint2bytes(4,bytes.size());
            out.addAll(valueCount);
            out.addAll(bytes);
        }
        List<Function> functions=table.getFunctionTable();
        List<Byte> fuctionsnum=uint2bytes(4,functions.size());
        out.addAll(fuctionsnum);
        for (int i=0;i<functions.size();i++){
            Function function=functions.get(i);
            List<Byte> name =uint2bytes(4,table.getFunctionID(function.getName()));
            out.addAll(name);
            List<Byte> retSlots = uint2bytes(4,function.getType()==TokenType.VOID?0:1);
            out.addAll(retSlots);
            List<Byte> paramSlots =uint2bytes(4,function.getParamsnum());
            out.addAll(paramSlots);
            List<Byte> locSlots =uint2bytes(4,function.getVarnum());
            out.addAll(locSlots);
            List<Byte> bodyCount = uint2bytes(4,function.getInstructions().size());
            out.addAll(bodyCount);
            List<Instruction> instructions =function.getInstructions();
            for(Instruction instruction:instructions){
                List<Byte> opt =uint2bytes(1,instruction.getType());
                out.addAll(opt);
                if(instruction.getLen()==2){
                    List<Byte> bytes=new ArrayList<>();
                    if(instruction.getType()==1){
                        bytes=uint2bytes(8, instruction.getOff());
                    }else bytes=uint2bytes(4,instruction.getOff());
                    out.addAll(bytes);
                }
            }
        }
        return out;
    }

    private List<Byte> uint2bytes(int length,int target){
        List<Byte> bytes=new ArrayList<>();
        int l=8*(length-1);
        for (int i=0;i<length;i++){
            bytes.add((byte)((target>>(l - i * 8))&0xFF));
        }
        return bytes;
    }
    private List<Byte> uint2bytes(int length,long target){
        List<Byte> bytes=new ArrayList<>();
        int l=8*(length-1);
        for (int i=0;i<length;i++){
            bytes.add((byte)((target>>(l - i * 8))&0xFF));
        }
        return bytes;
    }
    private List<Byte> string2bytes(String string){
        List<Byte> bytes=new ArrayList<>();
        for (int i=0;i<string.length();i++){
            char c=string.charAt(i);
            bytes.add((byte)(c&0xFF));
        }
        return bytes;
    }

}
