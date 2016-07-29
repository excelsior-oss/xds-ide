package com.excelsior.xds.core.compiler.driver;

import java.nio.charset.Charset;
import java.util.ArrayList;

import com.excelsior.xds.core.compiler.driver.IXShellListener.MessageType;
import com.excelsior.xds.core.process.InputStreamListener;

public class XShellBinary implements InputStreamListener {
    
    // String lengths are passed in some previous arguments of the command
    // so type ARG_STRING_<N> means that the string with len from argument N is expected
    private enum ArgType {
        ARG_INT2     (2, -1),
        ARG_INT4     (4, -1),
        ARG_STRING_0 (-1, 0),
        ARG_STRING_1 (-1, 1),
        ARG_STRING_3 (-1, 3),
        ARG_STRING_4 (-1, 4),
        ARG_CHAR     (-1, -1);
        
        ArgType(int argLen, int lenFromArg) {
            this.argLen = argLen;
            this.lenFromArg = lenFromArg;
        }
        private int argLen;     // len of this arg (or -1) 
        private int lenFromArg; // get len for this string from 'lenFromArg' argument (or -1)
        
        public int getArgLen() {
            return argLen;
        }
        public int getLenFromArg() {
            return lenFromArg;
        }
    }
    
    private enum CmdState {
        EOF_STATE            ((char)0, null),
        NO_COMMAND           ((char)0, null), // raw stream
        WAIT_COMMAND         ((char)0, null), // after char(1) in the stream

        // len[2] + string[len]:
        CMD_ConsoleString    ('S', new ArgType[]{ArgType.ARG_INT2, ArgType.ARG_STRING_0}),
        CMD_JobCaption       ('C', new ArgType[]{ArgType.ARG_INT2, ArgType.ARG_STRING_0}),
        CMD_JobComment       ('M', new ArgType[]{ArgType.ARG_INT2, ArgType.ARG_STRING_0}),

        // 0 err_no[4]
        // 1 line[4]
        // 2 pos[4]
        // 3 fileNameLen[2]
        // 4 msgLen[2]
        // 5 errClass[1]
        // 6 fName[fNameLen]
        // 7 message[msgLen]:
        CMD_Message          ('E', new ArgType[]{ArgType.ARG_INT4,  
                                                 ArgType.ARG_INT4,    
                                                 ArgType.ARG_INT4, 
                                                 ArgType.ARG_INT2, 
                                                 ArgType.ARG_INT2, 
                                                 ArgType.ARG_CHAR,
                                                 ArgType.ARG_STRING_3, 
                                                 ArgType.ARG_STRING_4 }),
                                                 
        // progressLimit[4], len[2] + string[len]:
        CMD_JobStart         ('J', new ArgType[]{ArgType.ARG_INT4, ArgType.ARG_INT2, ArgType.ARG_STRING_1}),

        // commentProgress[4], progress[4]:
        CMD_JobProgress      ('P', new ArgType[]{ArgType.ARG_INT4, ArgType.ARG_INT4}),

        // 
        CMD_ModuleListStart  ('F', new ArgType[]{}),
        
        // len[2] + string[len]:
        CMD_ModuleListAppend ('f', new ArgType[]{ArgType.ARG_INT2, ArgType.ARG_STRING_0}),
        
        //
        CMD_ModuleListCommit ('X', new ArgType[]{}),
        
        // 'S' or 's' - ignored:
        CMD_SetMessagesSort  ('m', new ArgType[]{ArgType.ARG_CHAR});
        
        CmdState(char cmdChar, ArgType[] argTypes) {
            this.cmdChar = cmdChar;
            this.argTypes = argTypes;
        }
        public ArgType[] getArgTypes() {
            return argTypes;
        }
        
        public char getCmdChar() {
            return cmdChar;
        }
        
        private char cmdChar;
        private ArgType[] argTypes; 
    }
    
    private IXShellListener listener;
    
    // Parser state (+ see argNum):
    private CmdState cmdState;
    private ArgType argType;
    
    // List of args filled while parsting:
    private ArrayList<Object> argsList; // elements types may be Integer/String/Character
    // Currently parsing args (not added to argsList yet):
    private int argNum;             // arg number
    private ByteBuffer argString;   // arg buffer (for ARG_STRING_x)
    private char       argChar;     // arg buffer (for ARG_CHAR)
    private int        argInt;      // arg buffer (for ARG_INTx)
    private int        argIntShift; // used to read ARG_INTx
    private int        argLenCnt;   // bytes in arg: downcount to 0
    

    // Buffer to collect raw strings for transmitting to console: 
    private ByteBuffer rawStrBuilder;
    
    
    public XShellBinary(IXShellListener listener) {
        this.listener = listener;
        cmdState = CmdState.NO_COMMAND;
        argType = null;
        argsList = new ArrayList<Object>();
        argNum = 0;
        argString = new ByteBuffer(); 
        rawStrBuilder = new ByteBuffer();
    }
    
    @Override
    public void onHasData(byte[] buffer, int length) {
        for (int i=0; i<length; ++i) {
            onByte(((int)buffer[i]) & 0x000000ff);
        }
    }
    
    @Override
    public void onEndOfStreamReached() {
        flushRawString();
        cmdState = CmdState.EOF_STATE;
    }

    
    
    private void flushRawString() {
        if (rawStrBuilder.length() > 0) {
            listener.onConsoleString(rawStrBuilder.bytesToString(listener.getStreamCharset()));
            rawStrBuilder.clear();
        }
    }
    
    // Set state to read 'argNum'-th argument 
    private void initArgReadState() {
        // Clear argument values (for all types) 
        argString.clear();
        argInt = 0;
        argIntShift = 0;
        
        // This is the argument type to read:
        argType = cmdState.getArgTypes()[argNum];

        // Determine length in bytes to read (argLenCnt): 
        if (argType.getArgLen() >= 0) {
            argLenCnt = argType.getArgLen();
        } else if (argType.getLenFromArg() >= 0) {
            // It is string and its length is given in some already readen argument:
            argLenCnt = (Integer)(argsList.get(argType.getLenFromArg()));

            // 0-len string: it is already finished:
            if (argLenCnt == 0) {
                argComplete();
            }
        } // else - it is ARG_CHAR
    }
    
    private void argComplete() {
        switch(argType) {
        case ARG_INT2:
        case ARG_INT4:
            argsList.add(argInt); 
            break;
        case ARG_STRING_0: 
        case ARG_STRING_1: 
        case ARG_STRING_3:
        case ARG_STRING_4: 
            argsList.add(argString.bytesToString(listener.getStreamCharset())); 
            break;
        case ARG_CHAR:   argsList.add(argChar); break;
        }
        ++argNum;
        if (cmdState.getArgTypes().length == argNum) {
            cmdComplete();
        } else {
            initArgReadState(); // start read next arg
        }
    }
    
    private void cmdComplete() {
        switch (cmdState) {
        case CMD_ConsoleString: 
            // len[2] + string[len]:
            listener.onConsoleString((String)argsList.get(1));
            break;
        case CMD_JobCaption:
            // len[2] + string[len]:
            listener.onJobCaption((String)argsList.get(1));
            break;
        case CMD_JobComment:
            // len[2] + string[len]:
            listener.onJobComment((String)argsList.get(1));
            break;
        case CMD_Message:
            // 0 err_no[4]
            // 1 line[4]
            // 2 pos[4]
            // 3 fileNameLen[2]
            // 4 msgLen[2]
            // 5 errClass[1]
            // 6 fName[fNameLen]
            // 7 message[msgLen]:
            MessageType msgType;
            String message = (String)argsList.get(7);
            switch ((Character)argsList.get(5)) {
            case 'T': msgType = MessageType.COMPILE_TEXT;         break;
            case 'N': msgType = MessageType.COMPILE_NOTICE;       break;
            case 'W': msgType = MessageType.COMPILE_WARNING;      break;
            case 'E': msgType = MessageType.COMPILE_ERROR;        break;
            case 'S': msgType = MessageType.COMPILE_FATAL_ERROR;  break;
            default:
                msgType = MessageType.COMPILE_ERROR;
                message = "*** Unknown error type: bad compiler log format? -- " + message; //$NON-NLS-1$
            }
            listener.onMessage(msgType, (Integer)argsList.get(0), message, (String)argsList.get(6), 
                               (Integer)argsList.get(1), (Integer)argsList.get(2));
            break;
        case CMD_JobStart:
            // progressLimit[4], len[2] + string[len]:
            listener.onJobStart((Integer)argsList.get(0), (String)argsList.get(2));
            break;
        case CMD_JobProgress:
            // commentProgress[4], progress[4]:
            listener.onJobProgress((Integer)argsList.get(0), (Integer)argsList.get(1));
            break;
        case CMD_ModuleListStart:
            //
            listener.onModuleListStart();
            break;
        case CMD_ModuleListAppend:
            // len[2] + string[len]:
            listener.onModuleListAppend((String)argsList.get(1));
            break;
        case CMD_ModuleListCommit:
            //
            listener.onModuleListCommit();
            break;
        case CMD_SetMessagesSort:
            // char: expected 'S' or 's' ==> turn ON/OFF messages sorting by line numbers. Ignore it.
            break;
		default:
			break;
        }
        cmdState = CmdState.NO_COMMAND;
    }

    private void onByte(int b) {
        switch (cmdState) {
        case EOF_STATE:
            return;
        case NO_COMMAND:
            if (b == 1) {
                flushRawString();
                cmdState = CmdState.WAIT_COMMAND;
            } else if (b == 0x0a) {
                flushRawString();
            } else if (b != 0x0d) {
                rawStrBuilder.addByte(b);
            }
            return;
        case WAIT_COMMAND:
            for (CmdState cs : CmdState.values()) {
                if (cs.getCmdChar() == (char)b) {
                    cmdState = cs;
                    if (cs.getArgTypes() == null) {
                        break;
                    } else if (cs.getArgTypes().length==0) {
                        cmdComplete();
                    } else {
                        argsList.clear();
                        argNum = 0;
                        initArgReadState(); // start read args
                    }
                    return;
                }
            }
            cmdState = CmdState.NO_COMMAND; // error in the stream
            return;
		default:
			break;
        }
        
        switch(argType) {
        case ARG_INT2:
        case ARG_INT4:
            argInt |= (b & 0xff) << 8 * argIntShift++;
            if (--argLenCnt <= 0) { 
                argComplete();
            }
            break;
        case ARG_STRING_0:
        case ARG_STRING_1:
        case ARG_STRING_3:
        case ARG_STRING_4:
            argString.addByte(b);
            if (--argLenCnt <= 0) { 
                argComplete();
            }
            break;
        case ARG_CHAR:
            argChar = (char)b;
            argComplete();
            break;
        }
    }
    
    static final class ByteBuffer {
        private byte[] lineBuf;
        private int lineBufLen;

        public ByteBuffer() {
            lineBuf = new byte[4];
            lineBufLen = 0;
        }
        
        public void addByte(int ch) {
            if (lineBufLen == lineBuf.length) {
                byte[] newBuf = new byte[lineBufLen * 2];
                System.arraycopy(lineBuf, 0, newBuf, 0, lineBufLen);
                lineBuf = newBuf;
            }
            lineBuf[lineBufLen++] = (byte)ch;
        }
        
        public String bytesToString(Charset cs) {
            String res = new String(lineBuf, 0, lineBufLen, cs); 
            // Strings may come with zeroes and garbage after it in the tail. 
            // So cut the string up to char(0) if any
            int eos = res.indexOf(0);
            if (eos>=0) {
                res = res.substring(0, eos);
            }
            return res;
        }
        
        public void clear() {
            lineBufLen = 0;
        }
        
        public int length() {
            return lineBufLen;
        }
    }

}
