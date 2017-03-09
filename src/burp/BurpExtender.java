package burp;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.datatransfer.*;
import java.awt.Toolkit;

public class BurpExtender implements IBurpExtender, IContextMenuFactory
{
    private IContextMenuInvocation inv;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
        callbacks.setExtensionName("Linux/Windows LF Fixer");
        callbacks.registerContextMenuFactory(this);
    }

    private void click()
    {
        IHttpRequestResponse messages[] = inv.getSelectedMessages();
        final byte context = inv.getInvocationContext();
        for (IHttpRequestResponse message : messages)
        {
            String str = "";
            String modifiedString = "";
            int bounds[] = inv.getSelectionBounds();
            if (bounds == null)
                return;

            if (bounds[0] != bounds[1])
            {
                if (context == 0 || context == 2)
                    str = new String(Arrays.copyOfRange(message.getRequest(), bounds[0], bounds[1]));

                if (context == 1 || context == 3)
                    str = new String(Arrays.copyOfRange(message.getResponse(), bounds[0], bounds[1]));
            }

            else
            {
                if (context == 0 || context == 2)
                    str = new String(message.getRequest());

                if (context == 1 || context == 3)
                    str = new String(message.getResponse());
            }

            for (int i = 0; i < str.length(); i++)
            {
                if (str.charAt(i) == '\r')
                {
                    if (str.charAt(i+1) == '\n')
                    {
                        continue;
                    }
                }
                modifiedString += str.charAt(i);
            }

            StringSelection stringSelection = new StringSelection(modifiedString);
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);
        }
    }

    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation)
    {
        JMenuItem item = new JMenuItem("Copy to Windows");
        inv = invocation;

        item.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                click();
            }
        });

        List<JMenuItem> menuItems = new ArrayList<>();
        menuItems.add(item);
        return menuItems;
    }

}