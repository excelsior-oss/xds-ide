package com.excelsior.xds.parser.modula;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.parser.commons.IParserEventListener;
import com.excelsior.xds.parser.commons.TodoTaskParser;
import com.excelsior.xds.parser.commons.TodoTaskParser.TaskEntry;
import com.excelsior.xds.parser.commons.ast.TokenType;

public class XdsCommentParser extends XdsTokenParser
{
    public static final String[] EMPTY_COMMENT_ARRAY = new String[0];
    
    private final TodoTaskParser commentParser;
    
    private boolean isParseComment—ontents;
    
    public XdsCommentParser( IFileStore sourceFile, CharSequence chars
                           , XdsSettings settings
                           , IParserEventListener reporter )
    {
        super(sourceFile, chars, settings, reporter);
        commentParser = new TodoTaskParser();
        this.isParseComment—ontents = true;
    }
    
    protected void setParseComment—ontents(boolean isParseComments) {
        this.isParseComment—ontents = isParseComments;
    }

    /**
     * Parses Modula-2 text to find 'to-do' tasks in the comments.
     * 
     * @return array of the found 'to-do' tasks.
     */
    public TaskEntry[] parseTodoTaks() {
        setTokenListener(NullTokenListener.INSTANCE);
        reset();
        
        List<TaskEntry> taskEntries = new ArrayList<TaskEntry>();
        super.nextToken();
        while (token != EOF) {
            if (COMMENT_SET.contains(token)) {
                int foundTaskIndex = taskEntries.size();
                commentParser.parse( 
                    sourceFile, getTokenText(), getTokenPosition(), taskEntries 
                );
                if (foundTaskIndex < taskEntries.size()) {
                    for (int i = foundTaskIndex; i < taskEntries.size(); i++) {                                                                                      
                        TaskEntry entry = taskEntries.get(i);
                        reporter.taskTag( 
                            entry.getFile(), entry.getPosition(), entry.getEndOffset(),
                            entry.getTask(), entry.getMessage()
                        );
                    }
                }
            }
            super.nextToken();
        }
        
        reporter.endFileParsing(sourceFile);
        
        if (taskEntries.isEmpty()) {                                                  
            return TodoTaskParser.EMPTY_TASKENTRY_ARRAY;                                             
        }
        return taskEntries.toArray(new TaskEntry[taskEntries.size()]);                
    }

    
    /**
     * Parses Modula-2 text to find comments.
     * 
     * @return array of the found comments.
     */
    public String[] parseComments() {
        setTokenListener(NullTokenListener.INSTANCE);
        reset();
        
        List<String> comments = new ArrayList<String>();
        super.nextToken();
        while (token != EOF) {
            if (COMMENT_SET.contains(token)) {
                String comment = getTokenText();
                comments.add(comment);
                if (isParseComment—ontents) {
                    commentParser.parse(sourceFile, comment, getTokenPosition(), reporter);
                }
            }
            super.nextToken();
        }
        
        reporter.endFileParsing(sourceFile);

        if (comments.isEmpty()) {                                                  
            return EMPTY_COMMENT_ARRAY;                                             
        }
        return comments.toArray(new String[comments.size()]);                
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected TokenType nextToken() {
        while (true) {
            super.nextToken();

            if (COMMENT_SET.contains(token)) {
                if (isParseComment—ontents) {
                    commentParser.parse( 
                        sourceFile, getTokenText(), getTokenPosition(), reporter 
                    );
                }
            }
            else if (token != WHITE_SPACE) {
                return token;
            }
        }
    }
        
}
