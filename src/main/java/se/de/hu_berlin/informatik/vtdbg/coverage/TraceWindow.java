package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;

public class TraceWindow {
    private JPanel content;
    private JTextPane textPane1;

    public JPanel getContent() {
        return content;
    }

    public void setTextPane1(String trace){
        textPane1.setText(trace);

    }

}