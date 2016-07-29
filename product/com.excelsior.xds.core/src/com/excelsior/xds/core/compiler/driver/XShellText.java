package com.excelsior.xds.core.compiler.driver;

import java.util.ArrayList;

import com.excelsior.xds.core.compiler.driver.IXShellListener.MessageType;
import com.excelsior.xds.core.process.InputStreamListener;

public class XShellText implements InputStreamListener {
    private enum ArgType {
        ARG_INT,
        ARG_STRING,
        ARG_CHAR
    }
    
    private enum CmdState {
        EOF_STATE            ((char)0, null),
        NO_COMMAND           ((char)0, null), // raw stream
        WAIT_COMMAND         ((char)0, null), // after char(1) in the stream
        CMD_ConsoleString    ('S', new ArgType[]{ArgType.ARG_STRING}),
        CMD_JobCaption       ('C', new ArgType[]{ArgType.ARG_STRING}),
        CMD_JobComment       ('M', new ArgType[]{ArgType.ARG_STRING}),
        CMD_Message          ('E', new ArgType[]{ArgType.ARG_INT,  ArgType.ARG_INT,    ArgType.ARG_INT, 
                                                 ArgType.ARG_CHAR, ArgType.ARG_STRING, ArgType.ARG_STRING }),
        CMD_JobStart         ('J', new ArgType[]{ArgType.ARG_INT, ArgType.ARG_STRING}),
        CMD_JobProgress      ('P', new ArgType[]{ArgType.ARG_INT, ArgType.ARG_INT}),
        CMD_ModuleListStart  ('F', new ArgType[]{}),
        CMD_ModuleListAppend ('f', new ArgType[]{ArgType.ARG_STRING}),
        CMD_ModuleListCommit ('X', new ArgType[]{}),
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
    private int argNum;                        // arg number
    private int                     argInt;    // arg buffer (for int)
    private char                    argChar;   // arg buffer (for char)
    private XShellBinary.ByteBuffer argString; // arg buffer (for string)

    // Buffer to collect raw strings for transmitting to console: 
    private XShellBinary.ByteBuffer rawStrBuilder;
    
    
    public XShellText(IXShellListener listener) {
        this.listener = listener;
        cmdState = CmdState.NO_COMMAND;
        argType = null;
        argsList = new ArrayList<Object>();
        argNum = 0;
        argString = new XShellBinary.ByteBuffer();
        rawStrBuilder = new XShellBinary.ByteBuffer();
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
    
    private void argComplete() {
        switch(argType) {
        case ARG_INT:    argsList.add(argInt); break;
        case ARG_STRING: argsList.add(argString.bytesToString(listener.getStreamCharset())); break;
        case ARG_CHAR:   argsList.add(argChar); break;
        }
        ++argNum;
        if (cmdState.getArgTypes().length == argNum) {
            cmdComplete();
        } else {
            argInt = 0;
            argString.clear();
            argType = cmdState.getArgTypes()[argNum];
        }
    }
    
    private void cmdComplete() {
        switch (cmdState) {
        case CMD_ConsoleString: 
            listener.onConsoleString((String)argsList.get(0));
            break;
        case CMD_JobCaption:
            listener.onJobCaption((String)argsList.get(0));
            break;
        case CMD_JobComment:
            listener.onJobComment((String)argsList.get(0));
            break;
        case CMD_Message:
            MessageType msgType;
            String message = (String)argsList.get(5);
            switch ((Character)argsList.get(3)) {
            case 'T': msgType = MessageType.COMPILE_TEXT;         break;
            case 'N': msgType = MessageType.COMPILE_NOTICE;       break;
            case 'W': msgType = MessageType.COMPILE_WARNING;      break;
            case 'E': msgType = MessageType.COMPILE_ERROR;        break;
            case 'S': msgType = MessageType.COMPILE_FATAL_ERROR;  break;
            default:
                msgType = MessageType.COMPILE_ERROR;
                message = "*** Unknown error type: bad compiler log format? -- " + message; //$NON-NLS-1$
            }
            listener.onMessage(msgType, (Integer)argsList.get(0), message, (String)argsList.get(4), 
                               (Integer)argsList.get(1), (Integer)argsList.get(2));
            break;
        case CMD_JobStart:
            listener.onJobStart((Integer)argsList.get(0), (String)argsList.get(1));
            break;
        case CMD_JobProgress:
            listener.onJobProgress((Integer)argsList.get(0), (Integer)argsList.get(1));
            break;
        case CMD_ModuleListStart:
            listener.onModuleListStart();
            break;
        case CMD_ModuleListAppend:            
            listener.onModuleListAppend((String)argsList.get(0));
            break;
        case CMD_ModuleListCommit:
            listener.onModuleListCommit();
            break;
        case CMD_SetMessagesSort:
            // expected 'S' or 's' ==> turn ON/OFF messages sorting by line numbers. Ignore it.
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
                        argInt = 0;
                        argString.clear();
                        argType = cs.getArgTypes()[0];
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
        case ARG_INT:
            if (b<'0' || b>'9') { // '_' expected.
                argComplete();
            } else { 
                argInt = argInt*10 + b-'0';
            }
            break;
        case ARG_STRING:
            if (b == 0) {
                argComplete();
            } else {
                argString.addByte(b);
            }
            break;
        case ARG_CHAR:
            argChar = (char)b;
            argComplete();
            break;
        }
    }
}
