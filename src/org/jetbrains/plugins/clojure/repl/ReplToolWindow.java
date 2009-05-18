package org.jetbrains.plugins.clojure.repl;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.TextConsoleBuilderImpl;
import com.intellij.execution.process.*;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.EditorComponentImpl;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.clojure.ClojureBundle;
import org.jetbrains.plugins.clojure.ClojureIcons;
import org.jetbrains.plugins.clojure.runner.console.ClojureConsoleViewImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ilyas, Kurt Christensen
 */

public class ReplToolWindow implements ProjectComponent {

  private static final String REPL_TOOL_WINDOW_ID = "repl.toolWindow";

  private Project myProject;
  private List<Repl> replList = new ArrayList<Repl>();
  private JTabbedPane tabbedPane;
  private ToolWindow toolWindow;
  private ActionPopupMenu popup;
  private static final String REPL_NAME = "Clojure";
  private static final String CLOJURE_REPL_ACTION_GROUP = "ClojureReplActionGroup";


  public ReplToolWindow(Project project) {
    myProject = project;
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        disposeComponent();
      }
    });
  }

  public void requestFocus() {
    toolWindow.activate(null, true);
    if (tabbedPane.getSelectedIndex() > -1) {
      Repl repl = replList.get(tabbedPane.getSelectedIndex());
      repl.view.getPreferredFocusableComponent().requestFocusInWindow();
    }
  }

  public String writeToCurrentRepl(String s) {
    return writeToCurrentRepl(s, true);
  }

  public String writeToCurrentRepl(String s, boolean requestFocus) {
    if (tabbedPane.getSelectedIndex() > -1) {
      final PipedWriter pipeOut;
      PipedReader pipeIn = null;
      try {
        if (requestFocus) requestFocus();
        final Repl repl = replList.get(tabbedPane.getSelectedIndex());

        pipeOut = new PipedWriter();
        pipeIn = new PipedReader(pipeOut);
        BufferedReader in = new BufferedReader(pipeIn);

        ProcessListener processListener = new ProcessAdapter() {
          @Override
          public void onTextAvailable(ProcessEvent event, Key outputType) {
            try {
              pipeOut.write(event.getText());
              pipeOut.flush();
              pipeOut.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        };
        repl.processHandler.addProcessListener(processListener);

        repl.view.print(s + "\r\n", ConsoleViewContentType.USER_INPUT);

        StringBuffer buf = new StringBuffer();
        //if (pipeIn.ready()) {
        String str;
        while ((str = in.readLine()) != null) {
          buf.append(str);
        }
        //}
        repl.processHandler.removeProcessListener(processListener);

        return buf.toString();

      } catch (IOException e) {
        e.printStackTrace();
        return null;
      } finally {
        if (pipeIn != null) {
          try {
            pipeIn.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return null;
  }

  public void projectOpened() {
    try {
      initToolWindow();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void projectClosed() {
    // ??? unregisterToolWindow();
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  @NotNull
  public String getComponentName() {
    return REPL_TOOL_WINDOW_ID;
  }


  private void initToolWindow() throws ExecutionException, IOException {
    if (myProject != null) {
      tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);

      JPanel panel = new JPanel(new BorderLayout());
      panel.add(tabbedPane, BorderLayout.CENTER);

      ActionManager am = ActionManager.getInstance();
      ActionGroup group = (ActionGroup) am.getAction(CLOJURE_REPL_ACTION_GROUP);
      ActionToolbar toolbar = am.createActionToolbar(REPL_NAME, group, false);
      panel.add(toolbar.getComponent(), BorderLayout.WEST);

      toolWindow = ToolWindowManager.getInstance(myProject).registerToolWindow(ClojureBundle.message("repl.toolWindowName"), false, ToolWindowAnchor.BOTTOM);
      ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
      Content content = contentFactory.createContent(panel, null, true);
      toolWindow.getContentManager().addContent(content);
      toolWindow.setIcon(ClojureIcons.CLOJURE_ICON_16x16);
      // toolWindow.setToHideOnEmptyContent(true);

      popup = am.createActionPopupMenu(ClojureBundle.message("repl.toolWindowName"), group);
      panel.setComponentPopupMenu(popup.getComponent());
      toolWindow.getComponent().setComponentPopupMenu(popup.getComponent());
      toolbar.getComponent().setComponentPopupMenu(popup.getComponent());

      // Firts REPL is created by USER
      //createRepl();
    }
  }

  public void createRepl() {
    try {
      Repl repl = new Repl();
      replList.add(repl);

      final int numOfRepls = tabbedPane.getTabCount();
      tabbedPane.addTab(ClojureBundle.message("repl.title") + numOfRepls, repl.view.getComponent());
      tabbedPane.setSelectedIndex(numOfRepls - 1);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ConfigurationException e) {
      JOptionPane.showMessageDialog(null,
              ClojureBundle.message("config.error.replNotConfiguredMessage"),
              ClojureBundle.message("config.error.replNotConfiguredTitle"),
              JOptionPane.WARNING_MESSAGE);
    }
  }

  public void removeCurrentRepl() {
    int i = tabbedPane.getSelectedIndex();
    if (i > -1) {
      replList.remove(i).close();
      tabbedPane.removeTabAt(i);
    }
  }

  public void renameCurrentRepl() {
    int tabIndex = tabbedPane.getSelectedIndex();
    if (tabIndex > -1) {
      String oldTitle = tabbedPane.getTitleAt(tabIndex);

      // TODO - Should build my own small tool window dialog, positioned wherever the user clicked
      String newTitle = (String) JOptionPane.showInputDialog(
              tabbedPane.getSelectedComponent(), null, ClojureBundle.message("repl.rename"),
              JOptionPane.PLAIN_MESSAGE, null, null, null);
      if (newTitle != null) {
        tabbedPane.setTitleAt(tabIndex, newTitle);
      }
    }
  }


  private class Repl {
    public ConsoleView view;
    private ProcessHandler processHandler;

    public Repl() throws IOException, ConfigurationException {
      final TextConsoleBuilderImpl builder = new TextConsoleBuilderImpl(myProject) {
        private final ArrayList<Filter> filters = new ArrayList<Filter>();

        @Override
        public ConsoleView getConsole() {
          final ClojureConsoleViewImpl view = new ClojureConsoleViewImpl(myProject);
          for (Filter filter : filters) {
            view.addMessageFilter(filter);
          }
          return view;
        }

        @Override
        public void addFilter(Filter filter) {
          filters.add(filter);
        }
      };
      view = builder.getConsole();

      // TODO - What does the "help ID" give us??

      final VirtualFile baseDir = myProject.getBaseDir();
      processHandler = new ClojureReplProcessHandler(baseDir.getPath());
      ProcessTerminatedListener.attach(processHandler);
      processHandler.startNotify();
      view.attachToProcess(processHandler);

      tabbedPane.addTab(ClojureBundle.message("repl.title"), view.getComponent());

      final EditorEx ed = getEditor();
      ed.getContentComponent().addKeyListener(new KeyAdapter() {
        public void keyTyped(KeyEvent event) {
          // TODO - This is probably wrong, actually, but it's a start...
//          ed.getCaretModel().moveToOffset(view.getContentSize());
//          ed.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
        }
      });

/* TODO - I may want this, but right now it pukes when you "Run Selected Text" from the editor and the result is an error...
            ed.getContentComponent().addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent event) {
                    // TODO - This is probably wrong, actually, but it's a start...
                    ed.getCaretModel().moveToOffset(view.getContentSize());
                    ed.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
                }
            });
*/

      // TODO - Experimental... Play around with what widgetry we'd like to see in the REPL
      ed.getSettings().setSmartHome(true);
      ed.getSettings().setVariableInplaceRenameEnabled(true);
      ed.getSettings().setAnimatedScrolling(true);
      ed.getSettings().setFoldingOutlineShown(true);
      //e.getSettings().setLineNumbersShown(true);

      ed.getContentComponent().setComponentPopupMenu(popup.getComponent());
      view.getComponent().setComponentPopupMenu(popup.getComponent());
      tabbedPane.setVisible(true);

    }

    public EditorEx getEditor() {
      EditorComponentImpl eci = (EditorComponentImpl) view.getPreferredFocusableComponent();
      return eci.getEditor();
    }

    public void close() {
      if (processHandler != null) {
        processHandler.destroyProcess();
      }
    }
  }
}