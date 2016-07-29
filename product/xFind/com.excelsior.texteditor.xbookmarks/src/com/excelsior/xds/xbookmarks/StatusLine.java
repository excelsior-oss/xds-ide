package com.excelsior.xds.xbookmarks;

import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.SubStatusLineManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;

/**
 * Wrapper to display temporary messages on a status line.
 */
public class StatusLine {
    
    private IStatusLineManager lastStatusLine = null;
    private long lastMsgId = 0;

    /**
     * Displays a error message in the currently visible status line.
     * 
     * @param message - error message
     */
    public void showErrMessage(String message) {
        showMessage0(message, XBookmarksPlugin.IMGID_ERROR, true);
    }

    /**
     * Displays a message in the currently visible status line.
     * 
     * @param message - text to display
     * @param imgId   - IMGID_* constant
     */
    public void showMessage(String message, int imgId) {
        showMessage0(message, imgId, false);
    }

    private void showMessage0(String message, int imgId, boolean error) {
        cleanStatusLine();
        IStatusLineManager statusLine = WorkbenchUtils.getStatusLine();
        if (statusLine == null) {
            return;
        }
        // get the topmost StatusLineManager to not destroy
        // messages set by views etc.
        while (statusLine instanceof SubStatusLineManager) {
            IContributionManager cb = ((SubStatusLineManager) statusLine)
                    .getParent();
            if (!(cb instanceof IStatusLineManager)) {
                break;
            }
            statusLine = (IStatusLineManager) cb;
        }
        
        Image img = XBookmarksPlugin.getDefault().getCachedImage(imgId);
        
        int delay;
        if (error) {
            statusLine.setMessage(null);
            statusLine.setErrorMessage(img, message);
            Display.getDefault().beep();
            delay = 5;
        } else {
            statusLine.setMessage(img, message);
            statusLine.setErrorMessage(null);
            delay = 3;
        }

        setStatusLine(statusLine);
        clearStatusLineAfter(delay, lastMsgId);
    }
    
    /**
     * Remove any message set by us from the right status line.
     */
    protected synchronized void cleanStatusLine() {
        if (lastStatusLine != null) {
            lastStatusLine.setErrorMessage(null);
            lastStatusLine.setMessage(null);
            lastStatusLine = null;
        }
    }
    
    /**
     * Set status line to be auto-cleaned.
     */
    protected synchronized void setStatusLine(IStatusLineManager statusLine) {
        lastStatusLine = statusLine;
        ++lastMsgId;
    }
    
    /**
     * Cleans the last message set by us after the given seconds in the given
     * status line. If that status line is cleared already by other means, e.g.
     * by a selection change or some other thread this does nothing.
     * 
     * @param seconds  the time to elapse before clearing the message
     * @param msgId  the id of the message to clear
     */
    protected void clearStatusLineAfter(int seconds, long msgId) {
        Display.getDefault().timerExec(1000 * seconds, new Clearer(msgId));
    }

    
    private class Clearer implements Runnable {
        private long msgId = -1;

        public Clearer(long msgId) {
            this.msgId = msgId;
        }

        public void run() {
            if (msgId == lastMsgId) {
                cleanStatusLine();
            }
        }
    }
    
}
