package de.unitrier.st.uap.w23.tram;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.util.Arrays;
import java.util.Scanner;

public class AbstractMachine {
    public static final int STEPWISE = 1;
    public static final int LOG_CONSOLE = 2;
    public static final int LOG_FILE = 4;
    public static final int LOG_DETAILED = 8;
    private int settings = 0;
    private final LoggerContext ctx = (LoggerContext) LogManager.getContext(LogManager.class.getClassLoader(), false);
    private final Logger consoleLogger = ctx.getLogger("consoleLogger");
    private final Logger fileLogger = ctx.getLogger("fileLogger");

    private final int STACK_SIZE = 32;
    private int TOP,PP,FP,PC;
private int[] STACK;

private Instruction[] instructions;

public AbstractMachine(){
    Reset();
}

private void Reset(){
    TOP = -1;
    PP = FP = PC = 0;
    STACK = new int[STACK_SIZE];
}

public String getFullStackString(){
    return String.format("TOP: %d\nPP: %d\nFP: %d\nPC: %d\nSTACK: %s\n",TOP,PP,FP,PC, Arrays.toString(STACK));
}

public String toString(){
        return String.format("TOP: %d\t|\tPP: %d\t|\tFP: %d\t|\tPC: %d\t|\tSTACK: %s\n",TOP,PP,FP,PC, Arrays.toString(Arrays.stream(STACK, 0, TOP+1).toArray()));
}

private void ensureStackSize(int i){
    if(TOP+i >= STACK_SIZE){
        if (TOP+i >= STACK.length-1){
            STACK = Arrays.copyOf(STACK, (((TOP+i)/STACK_SIZE)+1)*STACK_SIZE);
        } else if (TOP <= (STACK.length/2)) {
            STACK = Arrays.copyOf(STACK, ((TOP/STACK_SIZE)+1)*STACK_SIZE);
        }
    }

}

public void load(Instruction[] instructions){
    this.instructions = instructions;
    Reset();
}

public void run(){
    Reset();
    if(isStepWise()) {
        Scanner scanner = new Scanner(System.in);
        while (PC >= 0){
            scanner.nextLine();
            nextInstruction();
        };
    }else{
        while (PC >= 0) nextInstruction();
        }

}

public void nextInstruction(){
    if(PC >= 0) processInstruction(this.instructions [PC]);
}

public void processInstruction(Instruction instruction) {
    switch (instruction.getOpcode()) {
        case Instruction.CONST:
            doConst(instruction.getArg1());
            break;
        case Instruction.LOAD:
            doLoad(instruction.getArg1(),instruction.getArg2());
            break;
        case Instruction.STORE:
            doStore(instruction.getArg1(),instruction.getArg2());
            break;
        case Instruction.ADD:
            doAdd();
            break;
        case Instruction.SUB:
            doSub();
            break;
        case Instruction.MUL:
            doMul();
            break;
        case Instruction.DIV:
            doDiv();
            break;
        case Instruction.LT:
            doLt();
            break;
        case Instruction.GT:
            doGt();
            break;
        case Instruction.EQ:
            doEq();
            break;
        case Instruction.NEQ:
            doNeq();
            break;
        case Instruction.IFZERO:
            doIfZero(instruction.getArg1());
            break;
        case Instruction.GOTO:
            doGoto(instruction.getArg1());
            break;
        case Instruction.HALT:
            doHalt();
            break;
        case Instruction.NOP:
            doNop();
            break;
        case Instruction.INVOKE:
            doInvoke(instruction.getArg1(),instruction.getArg2(),instruction.getArg3());
            break;
        case Instruction.RETURN:
            doReturn();
            break;
        case Instruction.POP:
            doPop();
            break;
        default:
            break;
    }
    if(settings > 1){
        log(instruction);
    }
}

    private void doConst(int k) {
        ensureStackSize(1);
        STACK[TOP+1] = k;
        TOP = TOP + 1;
        PC = PC +1;
    }

    private void doStore(int k, int d) {
        STACK[spp(d,PP,FP)+k] = STACK[TOP];
        TOP = TOP - 1;
        PC = PC +1;
    }

    private void doLoad(int k, int d) {
        ensureStackSize(1);
        STACK[TOP+1] = STACK[spp(d,PP,FP)+k];
        TOP = TOP + 1;
        PC = PC + 1;
    }

    private void doAdd() {
        STACK[TOP-1] = STACK[TOP-1] + STACK[TOP];
        TOP = TOP - 1;
        PC = PC +1;
    }

    private void doSub() {
        STACK[TOP-1] = STACK[TOP-1] - STACK[TOP];
        TOP = TOP - 1;
        PC = PC +1;
    }

    private void doMul() {
        STACK[TOP-1] = STACK[TOP-1] * STACK[TOP];
        TOP = TOP - 1;
        PC = PC +1;
    }

    private void doDiv() {
        STACK[TOP-1] = STACK[TOP-1] / STACK[TOP];
        TOP = TOP - 1;
        PC = PC +1;
    }

    private void doLt() {
        if(STACK[TOP - 1 ] < STACK[TOP]){
            STACK[TOP-1] = 1;
        }else{
            STACK[TOP-1] = 0;
        }
        TOP = TOP - 1;
        PC = PC + 1;
    }

    private void doGt() {
        if(STACK[TOP - 1 ] > STACK[TOP]){
            STACK[TOP-1] = 1;
        }else{
            STACK[TOP-1] = 0;
        }
        TOP = TOP - 1;
        PC = PC + 1;
    }

    private void doEq() {
        if(STACK[TOP - 1 ] == STACK[TOP]){
            STACK[TOP-1] = 1;
        }else{
            STACK[TOP-1] = 0;
        }
        TOP = TOP - 1;
        PC = PC + 1;
    }

    private void doNeq() {
        if(STACK[TOP - 1 ] != STACK[TOP]){
            STACK[TOP-1] = 1;
        }else{
            STACK[TOP-1] = 0;
        }
        TOP = TOP - 1;
        PC = PC + 1;
    }

    private void doGoto(int p) {
        PC = p;
    }
    private void doIfZero(int p) {
        if (STACK[TOP] == 0){
            PC = p;
        }else{
            PC = PC +1;
        }
        TOP = TOP -1;
    }
    private void doHalt() {
        PC = -1;
    }

    private void doNop() {
        PC = PC + 1;
    }

    private void doInvoke(int n, int p, int d) {
        ensureStackSize(5);
        STACK[TOP+1] = PC+1;
        STACK[TOP+2] = PP;
        STACK[TOP+3] = FP;
        STACK[TOP+4] = spp(d, PP, FP);
        STACK[TOP+5] = sfp(d, PP, FP);
        PP = TOP - n + 1;
        FP = TOP + 1;
        TOP = TOP + 5;
        PC = p;
    }


    private void doReturn() {
        int res = STACK[TOP];
        TOP = PP;
        PC = STACK[FP];
        PP = STACK[FP+1];
        FP = STACK[FP+2];
        STACK[TOP] = res;
    }

    private void doPop() {
        ensureStackSize(1);
        STACK[TOP] = 0;
        TOP = TOP - 1;
        PC = PC +1;
    }

    private int spp(int d, int pp, int fp){
        if ( d == 0 ) return pp;
        return spp(d-1, STACK[fp+3], STACK[fp+4]);
    }

    private int sfp(int d, int pp, int fp){
        if ( d == 0 ) return fp;
        return sfp(d-1, STACK[fp+3], STACK[fp+4]);
    }

    String indent(int count){
        return new String(new char[count]).replace("\0", " ");
    }

    public String debugStack(Instruction i) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("After instruction [%s]; configuration [PC %d, PP %d, FP %d, TOP %d ]\nStack:\n", i.toString(), PC, PP, FP, TOP));

        for (int j = 0; j <= TOP; j++) {
            stringBuilder.append(String.format("[%d] = %d", j, STACK[j]));

            if (PC == j || PP == j || FP == j || TOP == j) {
                int l1 = j > 0 ? (int) (Math.log10(j) + 1) : 1;
                int l2 = STACK[j] > 0 ? (int) (Math.log10(STACK[j]) + 1) : 1;
                stringBuilder.append(String.format("%s<---- (", indent((int) Math.max(10 - (l1 + l2), 0))));
            }

            if (PC == j) {
                stringBuilder.append(String.format(" PC = %d,", PC));
            }
            if (PP == j) {
                stringBuilder.append(String.format(" PP = %d,", PP));
            }
            if (FP == j) {
                stringBuilder.append(String.format(" FP = %d,", FP));
            }
            if (TOP == j) {
                stringBuilder.append(String.format(" TOP = %d,", TOP));
            }

            if (PC == j || PP == j || FP == j || TOP == j) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                stringBuilder.append(" )");
            }

            stringBuilder.append("\n");
        }
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }

    public void setSettings(int i){
        settings = i;
    }

    private void log(Instruction instruction){
        String s = ((LOG_DETAILED & settings) == LOG_DETAILED) ? debugStack(instruction) : toString();
        if((LOG_CONSOLE & settings) == LOG_CONSOLE){
            consoleLogger.debug(s);
        }

        if((LOG_FILE & settings) == LOG_FILE){
            fileLogger.debug(s);
        }
    }

    private boolean isStepWise(){
        return ((STEPWISE & settings) == STEPWISE);
    }
}
