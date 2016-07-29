package com.excelsior.xds.parser.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.core.todotask.TodoTask;
import com.excelsior.xds.core.todotask.TodoTaskManager;

public class TodoTaskParser
{
    public static final TaskEntry[] EMPTY_TASKENTRY_ARRAY = new TaskEntry[0];
    
    private final boolean    isTaskCaseSensitive;
    private final TodoTask[] tasks; 
    
    
    /**
     * Creates a instance of "to do" task parser. 
     */
    public TodoTaskParser() {
        isTaskCaseSensitive = TodoTaskManager.getInstance().isCaseSensitive();
        tasks = getOrderedTasks(TodoTaskManager.getInstance().getAllTasks());
    }

    
    /**
     * Parses comment and notifies the listener to "to do" task entries.
     * 
     * @param file, a source file in which the comment located
     * @param comment, a text of the comment with comment's opening and closing symbols  
     * @param commentPosition, a start position of the comment
     * @param listener, an object listening to "to do" task entries
     */
    public void parse( IFileStore file, CharSequence comment, TextPosition commentPosition
                     , ITodoTaskListener listener )
    {
        TaskEntry[] taskEntries = parse(file, comment, commentPosition); 
        for (TaskEntry entry : taskEntries) {
            listener.taskTag( 
                entry.getFile(), entry.getPosition(), entry.getEndOffset(),
                entry.getTask(), entry.getMessage()
            );
        }
    }
    
    
    /**
     * Parses comment and finds "to do" task entries.
     * 
     * @param file, a source file in which the comment located
     * @param comment, a text of the comment with comment's opening and closing symbols  
     * @param commentPosition, a start position of the comment
     * 
     * @return an array containing the detected "to do" task entries.
     */
    public TaskEntry[] parse(IFileStore file, CharSequence comment, TextPosition commentPosition) 
    {
        List<TaskEntry> commentEntries = new ArrayList<TaskEntry>();    
        parse(file, comment, commentPosition, commentEntries);
        if (commentEntries.isEmpty()) {                                                  
            return EMPTY_TASKENTRY_ARRAY;                                             
        }                                                                             
        return commentEntries.toArray(new TaskEntry[commentEntries.size()]);                
    }

    
    /**
     * Parses comment and adds founded "to do" task entries to the list.
     * 
     * @param file, a source file in which the comment located
     * @param comment, a text of the comment with comment's opening and closing symbols  
     * @param commentPosition, a start position of the comment
     * @param taskEntries, a storage to add the found "to do" task entries
     */
    public void parse( IFileStore sourceFile, CharSequence comment, TextPosition commentPosition
                     , List<TaskEntry> taskEntries )
    {
        int foundTaskIndex = taskEntries.size();
        findTaskEntries(sourceFile, comment, commentPosition, taskEntries);
         if (foundTaskIndex < taskEntries.size()) {                                                  
            retrieveTaskMessages(comment, commentPosition, taskEntries, foundTaskIndex);                                   
        }                                                                             
    }
    
    
    private void retrieveTaskMessages( CharSequence comment, TextPosition commentPosition
                                     , List<TaskEntry> taskEntries, int foundTaskIndex )                                                    
    {                                                                                                                                       
        boolean containsEmptyTask = false;                                                                                                  
        int commentLength = comment.length();                                                                                               
                                                                                                                                            
        for (int i = foundTaskIndex; i < taskEntries.size(); i++) {                                                                                      
            TaskEntry entry = taskEntries.get(i);                                                                                           

            // Retrieve message start and end positions                                                                                     
            int msgStart = entry.position.getOffset() - commentPosition.getOffset() + entry.task.tag.length();                                                  
            int maxValue = i + 1 < taskEntries.size()                                                                                       
                         ? taskEntries.get(i + 1).position.getOffset() - commentPosition.getOffset()
                         : commentLength;                                                
            // At most beginning of next task                                                                                               
            if (maxValue < msgStart) {                                                                                                      
                maxValue = msgStart; // Would only occur if tag is before EOF.                                                              
            }                                                                                                                               
                                                                                                                                            
            int msgEnd = -1;                                                                                                                   
            char c;                                                                                                                         
            for (int j = msgStart; j < maxValue; j++) {                                                                                     
                c = comment.charAt(j);                                                                                                      
                if (c == '\n' || c == '\r') {                                                                                               
                    msgEnd = j;                                                                                                                
                    break;                                                                                                                  
                }                                                                                                                           
            }                                                                                                                               
            if (msgEnd == -1) {                                                                                                                
                for (int j = maxValue; --j >= msgStart;) {                                                                                  
                    if ((c = comment.charAt(j)) == '*') {                                                                                   
                        msgEnd = j;                                                                                                            
                        break;                                                                                                              
                    }                                                                                                                       
                }                                                                                                                           
                if (msgEnd == -1) {                                                                                                            
                    msgEnd = maxValue;                                                                                                         
                }                                                                                                                           
            }                                                                                                                               
                                                                                                                                            
            // Trim the message                                                                                                             
            while (msgStart < msgEnd && Character.isWhitespace(comment.charAt(msgEnd - 1))) {                                                     
                msgEnd--;                                                                                                                      
            }                                                                                                                               
            while (  msgStart < msgEnd 
                  && (  Character.isWhitespace(comment.charAt(msgStart)) 
                     || comment.charAt(msgStart) == ':'
                     )
                  ) 
            {               
                msgStart++;                                                                                                                 
            }                                                                                                                               
                                                                                                                                            
            if (msgStart == msgEnd) {                                                                                                          
                // If the description is empty, we might want to see if two tags                                                            
                // are not sharing the same message.                                                                                        
                containsEmptyTask = true;                                                                                                   
                continue;                                                                                                                   
            }                                                                                                                               
                                                                                                                                            
            // Get the message source                                                                                                       
            entry.setMessage( comment.subSequence(msgStart, msgEnd).toString()
                            , commentPosition.getOffset() + msgEnd );                                                                
        }                    
        
        if (containsEmptyTask) {
            processEmptyTasks(taskEntries, foundTaskIndex);
        }
    }                                                                                                                                       


    private void processEmptyTasks(List<TaskEntry> taskEntries, int foundTaskIndex) 
    {
        for (int i = foundTaskIndex; i < taskEntries.size(); i++) {                                                                                      
            TaskEntry entry1 = taskEntries.get(i);                                                                                           
            if (entry1.message.length() == 0) {
                for (int j = i + 1; j < taskEntries.size(); j++) {
                    TaskEntry entry2 = taskEntries.get(j);                                                                                           
                    if (entry2.message.length() != 0) {
                        entry1.setMessage(entry2.message, entry2.endOffset);
                        break;
                    }
                    
                }
            }
        }
    }
    
    private void findTaskEntries( IFileStore file, CharSequence comment
                                , TextPosition commentPosition
                                , List<TaskEntry> taskEntries )                                                            
    {                                                                                                                                   
        int commentLength = comment.length();                                                                                           
                                                                                                                                          
        char previous = comment.charAt(0); // the character at zero index is a comment start marker
        int column = commentPosition.getColumn();
        int line = commentPosition.getLine();
                                                                                                                                          
        for (int i = 1; i < commentLength; i++) {
            if (comment.charAt(i) == '\n') {
                line++;
                column = 1;
            }
            else {
                column++;
            }
        nextTag:                                                                                                                        
            for (TodoTask task : tasks) {                                                                                               
                int tagLength = task.tag.length();                                                                                      
                                                                                                                                          
                if (tagLength == 0 || i + tagLength > commentLength)                                                                    
                    continue nextTag;                                                                                                   
                                                                                                                                          
                // Ensure tag is not leaded by a letter if the tag starts with a letter.                                                
                if (isIdentifierStart(task.tag.charAt(0)) && isIdentifierPart(previous)) {                                              
                    continue nextTag;                                                                                                   
                }                                                                                                                       
                                                                                                                                          
                for (int t = 0; t < tagLength; t++) {                                                                                   
                    int x = i + t;                                                                                                      
                    if (x >= commentLength)                                                                                             
                        continue nextTag;                                                                                               
                                                                                                                                          
                    char sc = comment.charAt(x);                                                                                        
                    char tc = task.tag.charAt(t);                                                                                       
                    if (sc != tc) {     // case sensitive check                                                                         
                        if (isTaskCaseSensitive || Character.toLowerCase(sc) != Character.toLowerCase(tc)) { // case insensitive check  
                            continue nextTag;                                                                                           
                        }                                                                                                               
                    }                                                                                                                   
                }                                                                                                                       
                                                                                                                                          
                // Ensure tag is not followed by a letter if the tag ends with a letter.                                                
                if (  (i + tagLength < commentLength)                                                                                   
                   && isIdentifierPart(comment.charAt(i + tagLength - 1))                                                               
                   && isIdentifierPart(comment.charAt(i + tagLength)) )                                                                 
                {                                                                                                                       
                    continue nextTag;                                                                                                   
                }                                                                                                                       
                                                                                                                                          
                TextPosition position = new TextPosition(
                    line, column, commentPosition.getOffset() + i
                );                              
                TaskEntry entry = new TaskEntry(file, position, task);                                             
                taskEntries.add(entry);                                                                                                 
                                                                                                                                         
                i      += tagLength - 1;   // Will be incremented when looping                                                          
                column += tagLength - 1;   // Will be incremented when looping                                                          
                break nextTag;                                                                                                          
            }                                                                                                                           
            previous = comment.charAt(i);                                                                                               
        }                                                                                                                               
    }                                                                                                                                   

    
    /**
     * Returns given tasks in the checking order that gives preference to 
     * the longest matching tag.
     * 
     * @param tasks an array of tasks   
     * @return an array of ordered tasks
     */
    private TodoTask[] getOrderedTasks(TodoTask[] tasks) {
        if (tasks == null) {
            return new TodoTask[0]; 
        }
        
        TodoTask[] orderedTasks = Arrays.copyOf(tasks, tasks.length);  
        
        // Sort order array in reverse order of tag lengths.
        // Shell sort algorithm from http://en.wikipedia.org/wiki/Shell_sort
        for (int inc = orderedTasks.length / 2; inc > 0; inc /= 2) {
            for (int i = inc; i < orderedTasks.length; i++) {
                for ( int j = i
                    ; j >= inc && orderedTasks[j - inc].tag.length() < orderedTasks[j].tag.length()
                    ; j -= inc ) 
                {
                    TodoTask temp = orderedTasks[j];
                    orderedTasks[j] = orderedTasks[j - inc];
                    orderedTasks[j - inc] = temp;
                }
            }
        }
        return orderedTasks;
    }

    
    private static boolean isIdentifierStart(char c) {    
        return Character.isLetter(c) || c == '_';         
    }                                                     
                                                          
    private static boolean isIdentifierPart(char c) {     
        return Character.isLetterOrDigit(c) || c == '_';  
    }                                                     

    
    public static class TaskEntry 
    {
        private final IFileStore file;
        private final TextPosition position;
        private final TodoTask task; 
        private String message;
        private int endOffset;
        
        TaskEntry(IFileStore file, TextPosition position, TodoTask task) {
            this.file = file;
            this.position = position;
            this.task = task;
            this.message = "";    //$NON-NLS-1$
            this.endOffset = position.getOffset() + task.tag.length();
        }

        public IFileStore getFile() {
            return file;
        }

        /**
         * Returns the start position of the task.
         * 
         * @return start position of the task 
         */
        public TextPosition getPosition() {
            return position;
        }

        public TodoTask getTask() {
            return task;
        }

        /**
         * Returns the message of the task
         * 
         * @return message of the task 
         */
        public String getMessage() {
            return message;
        }

        /**
         * Returns the end offset of the task
         * 
         * @return end offset of the task 
         */
        public int getEndOffset() {
            return endOffset;
        }                                        
        
        private void setMessage(String message, int endOffset) {
            this.message   = message;              
            this.endOffset = endOffset;
        }

    }

}
