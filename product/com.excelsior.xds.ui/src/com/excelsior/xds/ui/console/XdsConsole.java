package com.excelsior.xds.ui.console;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IHyperlink2;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.part.IPageBookViewPage;

import com.excelsior.xds.core.console.ColorStreamType;
import com.excelsior.xds.core.console.IXdsConsole;
import com.excelsior.xds.core.console.IXdsConsoleTerminateCallback;
import com.excelsior.xds.core.console.XdsConsoleLink;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.ui.commons.swt.resources.ResourceRegistry;
import com.excelsior.xds.ui.swt.resources.SharedResourceManager;

public class XdsConsole extends IOConsole implements IXdsConsole {
    // Data to connect ProcessLauncher and XdsConsoleTerminateAction (red
    // button):
    private Action fFilteredAction;
    private Action fTerminated;
    private String encoding;
    private Boolean isTerminateButtonEnabledInitial = null;
    private AtomicInteger printedSize;
    private String crlf = SystemUtils.LINE_SEPARATOR;
    private XdsConsolePage xdsConsolePage;
    private ResourceRegistry resRegistry;

    private Map<RGB, IOConsoleOutputStream> rgb2streamMap; 
    
    private List<LinkInfo> alLinkInfo;

    private int currentLinkIdx = 0; // for prev/next link arrows
	private IXdsConsoleTerminateCallback terminateCallback;

    private static boolean isLinkedWithEditor;

    public XdsConsole(String id) {
        super(id, null);
        resRegistry = new ResourceRegistry();
        rgb2streamMap = new HashMap<RGB, IOConsoleOutputStream>();
        alLinkInfo = Collections.synchronizedList(new ArrayList<LinkInfo>());
        printedSize = new AtomicInteger(0);
        this.getDocument().addDocumentListener(new IDocumentListener() {
            @Override
            public void documentChanged(DocumentEvent event) {
                addHyperlinks(event.getDocument().getLength());
            }

            @Override
            public void documentAboutToBeChanged(DocumentEvent event) {
            }
        });
    }
    
    @Override
	public void show() {
    	ConsolePlugin.getDefault().getConsoleManager().showConsoleView(this);
	}

	@Override 
    protected void dispose() {
        super.dispose();
        resRegistry.dispose();
    }

    private void addHyperlinks(int docSize) {
        // add hyperlinks for ready part of the document:
        if (printedSize.get() > 0) {
            synchronized (alLinkInfo) { 
                for (LinkInfo lnk : alLinkInfo) {
                    if (!lnk.hyperlinked) {
                        if (lnk.end < docSize) { // Not "<=", hyperlinks up to
                                                 // the document end does not
                                                 // works.
                            try {
                                this.addHyperlink(lnk, lnk.begin, lnk.end - lnk.begin);
                            } catch (BadLocationException e) {
                            	LogHelper.logError(e); // LSA80 : тут же баг, если мы здесь - надо писать в системный лог.
                            	// ј то откуда мы узнаем от закачика, что это случилось.
                            }
                            lnk.hyperlinked = true;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setEncoding(String enc) {
        if (enc != null) {
            // check if the encoding is supported
            try {
                new String(new byte[] { 'z' }, 0, 1, enc); // LSA80 : жесть, по другому никак? ≈сли никак - то почему это не метод
                // в каких-ниьудь EncodingUtils в Core?
            } catch (UnsupportedEncodingException e) {
                enc = null;
            }
        }
        encoding = enc;
        synchronized (rgb2streamMap) {
        	for (IOConsoleOutputStream cs : rgb2streamMap.values()) {
        		cs.setEncoding(enc);
        	} 
		}
    }
    
    public void setBackground(RGB rgb) {
        setBackground(resRegistry.createColor(rgb));
    }

    // Launcher enables/disables terminate button using it:
    public void setXdsConsoleTerminateAction(Action a) {
        fTerminated = a;
        if (isTerminateButtonEnabledInitial != null) {
            fTerminated.setEnabled(isTerminateButtonEnabledInitial);
        }
    }

    public void setFilteredLogAction(Action filteredAction) {
        fFilteredAction = filteredAction;
        if (isTerminateButtonEnabledInitial != null) {
            fFilteredAction.setEnabled(!isTerminateButtonEnabledInitial);
        }
    }

    /* (non-Javadoc)
     * @see com.excelsior.xds.core.console.IXdsConsole#setTerminateCallback(com.excelsior.xds.core.console.IXdsConsoleTerminateCallback)
     */
    @Override
	public void setTerminateCallback(
			IXdsConsoleTerminateCallback terminateCallback) {
    	this.terminateCallback = terminateCallback;
	}
    
	IXdsConsoleTerminateCallback getTerminateCallback() {
		return terminateCallback;
	}

	@Override
    public void enableTerminateButton(boolean enable) {
        if (fTerminated != null) {
            fTerminated.setEnabled(enable);
            if (fFilteredAction != null) {
            	fFilteredAction.setEnabled(!enable);
            }
        } else {
            isTerminateButtonEnabledInitial = enable;
        }
    }

    @Override
    public OutputStream getConsoleStream(ColorStreamType cs) {
        final RGB rgb = cs.getRgb();
        IOConsoleOutputStream stream;
        synchronized (rgb2streamMap) {
        	stream = rgb2streamMap.get(rgb);
        	if (stream == null) {
        		stream = newOutputStream();
        		stream.setEncoding(encoding);
        		rgb2streamMap.put(rgb, stream);
        	}
		}
        final IOConsoleOutputStream streamParam = stream;
        Display.getDefault().syncExec(new Runnable(){
			@Override
			public void run() {
				streamParam.setColor(SharedResourceManager.getColor(rgb));
			}
		});
        
		return stream;
    }

    @Override
    public IOConsoleInputStream getInputStream() {
        final IOConsoleInputStream str = super.getInputStream();
        Display.getDefault().syncExec(new Runnable(){
			@Override
			public void run() {
				str.setColor(SharedResourceManager.getColor(ColorStreamType.USER_INPUT.getRgb()));
			}
		});
        return str;
    }

    @Override
    public void println(String line) {
        println(line, ColorStreamType.NORMAL);
    }

    @Override
    public void println(String line, ColorStreamType cs) {
        println(line, cs, null);
    }

    @Override
    public void println(String line, ColorStreamType cs, XdsConsoleLink link) {
        ConsoleTextItem it = new ConsoleTextItem(line, cs, link, false);
        consoleTextItems.add(it);
        outItem(it);
    }

    @Override
    public void printlnFiltered(final String line, final ColorStreamType cs) {
        ConsoleTextItem it = new ConsoleTextItem(line, cs, null, true);
        consoleTextItems.add(it);
        outItem(it);
    }
    
    @Override
    public void clearConsole() {
        clearDisplayed();
        consoleTextItems.clear();
    }

    private void clearDisplayed() {
        super.clearConsole();
        printedSize.set(0);
        alLinkInfo.clear();
        if (xdsConsolePage != null) {
            xdsConsolePage.setCurrentLinkRange(0, 0, false);
        }
    }

    @Override
    public IPageBookViewPage createPage(IConsoleView view) {
        xdsConsolePage = new XdsConsolePage(this, view);
        return xdsConsolePage;
    }

    public void goPrevNextMessage(boolean goPrev) {
        if (currentLinkIdx >= alLinkInfo.size()) {
            currentLinkIdx = 0;
        }
        if (goPrev) {
            if (currentLinkIdx > 0) {
                --currentLinkIdx;
            }
        } else {
            if (currentLinkIdx < alLinkInfo.size() - 1) {
                ++currentLinkIdx;
            }
        }

        if (currentLinkIdx < alLinkInfo.size()) {
            LinkInfo lm = alLinkInfo.get(currentLinkIdx);
            if (isLinkedWithEditor) {
                lm.linkActivated(true, true, false);
            } else {
                if (xdsConsolePage != null) {
                    xdsConsolePage.setCurrentLinkRange(lm.begin, lm.end
                            - lm.begin, true);
                }
            }
        }
    }

    public static void setLinkWithEditor(boolean state) {
        isLinkedWithEditor = state;
    }

    private class LinkInfo implements IHyperlink2 {
        private XdsConsoleLink link;
        int begin;
        int end;
        boolean hyperlinked;

        public LinkInfo(XdsConsoleLink link, int begin, int end) {
            this.link = link;
            this.begin = begin;
            this.end = end;
            this.hyperlinked = false;
        }

        @Override
        public void linkActivated(Event event) {
            linkActivated(false, false, event.type == SWT.MouseDoubleClick);
        }

        @Override
        public void linkActivated() {
            linkActivated(false, false, false);
        }

        public void linkActivated(boolean scrollToMakeItVisible, boolean fromPrevNext, boolean activateEditor) {
        	currentLinkIdx = alLinkInfo.indexOf(this); 

            if (xdsConsolePage != null) {
                xdsConsolePage.setCurrentLinkRange(begin, end - begin, scrollToMakeItVisible);
            }

            if (!fromPrevNext || link.isEditorLink()) { // don't open Problems when jump to prev.next link 
                link.gotoLink(activateEditor); 
            }
        }

        @Override
        public void linkEntered() {
        }

        @Override
        public void linkExited() {
        }
    }

    // ------- Console filtering

    private boolean isLogFiltered;
    private List<ConsoleTextItem> consoleTextItems = new ArrayList<ConsoleTextItem>();

    private static class ConsoleTextItem {
        ConsoleTextItem(String line, ColorStreamType cs, XdsConsoleLink link,
                boolean filterIt) {
            this.line = line;
            this.cs = cs;
            this.link = link;
            this.filterIt = filterIt;
        }

        String line;
        ColorStreamType cs;
        XdsConsoleLink link;
        boolean filterIt;
    }

    private void outItem(ConsoleTextItem it) {
        if (!isLogFiltered || !it.filterIt) {
            IOConsoleOutputStream str = (IOConsoleOutputStream) getConsoleStream(it.cs);
            if (str != null) {
                try {
                    int len = it.line.length() + crlf.length();
                    if (it.link != null) {
                        alLinkInfo.add(new LinkInfo(it.link, printedSize.get(), printedSize.get() + len));
                    }
                    printedSize.addAndGet(len); 
                    str.write(it.line);
                    str.write(crlf);
                } catch (Exception e) {
                    LogHelper.logError(e);
                }
            }
        }
    }

    public void setFilteredLog(Boolean isLogFiltered) {
    	setFilteredLogInternal(isLogFiltered, true);
    }
    
    void setFilteredLogInternal(Boolean isLogFiltered, boolean isResetView) {
        this.isLogFiltered = isLogFiltered;
        if (isResetView) {
        	clearDisplayed();
        	for (ConsoleTextItem it : consoleTextItems) {
        		outItem(it);
        	}
        }
    }

}
