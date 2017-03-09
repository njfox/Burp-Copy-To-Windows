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
        callbacks.setExtensionName("Copy to Windows");
        callbacks.registerContextMenuFactory(this);
    }

    private void click()
    {
        // Get HTTP Requests/Responses selected by the user
        // We only want to do something with one message selected, so just exit if there are multiple
        IHttpRequestResponse messages[] = inv.getSelectedMessages();
        if (messages.length > 1)
            return;

        IHttpRequestResponse message = messages[0];

        // Get the context the user was in when they opened the menu
        final byte context = inv.getInvocationContext();


        // Find the beginning and end of selected text (if there is any)
        int bounds[] = inv.getSelectionBounds();

        // If bounds is null, the button wasn't clicked in an HTTP Request/Response editor or viewer
        if (bounds == null)
            return;

        String str = "";
        String modifiedString = "";

        // If the user has text selected
        if (bounds[0] != bounds[1])
        {
            // We're in a request
            if (context == 0 || context == 2)
                str = new String(Arrays.copyOfRange(message.getRequest(), bounds[0], bounds[1]));

            // We're in a response
            if (context == 1 || context == 3)
                str = new String(Arrays.copyOfRange(message.getResponse(), bounds[0], bounds[1]));
        }

        else // No text selected, just grab the whole request or response
        {
            if (context == 0 || context == 2)
                str = new String(message.getRequest());

            if (context == 1 || context == 3)
                str = new String(message.getResponse());
        }

        for (int i = 0; i < str.length(); i++)
        {
            // Ignore the '\r' in an "\r\n" sequence
            if (str.charAt(i) == '\r' && str.charAt(i+1) == '\n')
                continue;
            modifiedString += str.charAt(i);
        }

        // Copy fixed text to clipboard
        StringSelection stringSelection = new StringSelection(modifiedString);
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);

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