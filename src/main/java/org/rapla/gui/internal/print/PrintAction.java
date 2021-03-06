/*--------------------------------------------------------------------------*
 | Copyright (C) 2014 Christopher Kohlhaas, Bettina Lademann                |
 |                                                                          |
 | This program is free software; you can redistribute it and/or modify     |
 | it under the terms of the GNU General Public License as published by the |
 | Free Software Foundation. A copy of the license has been included with   |
 | these distribution in the COPYING file, if not go to www.fsf.org         |
 |                                                                          |
 | As a special exception, you are granted the permissions to link this     |
 | program with every library, which license fulfills the Open Source       |
 | Definition as published by the Open Source Initiative (OSI).             |
 *--------------------------------------------------------------------------*/
package org.rapla.gui.internal.print;
import java.awt.*;
import java.awt.print.PageFormat;
import java.util.Map;

import javax.inject.Inject;
import javax.swing.SwingUtilities;

import org.rapla.client.swing.extensionpoints.SwingViewFactory;
import org.rapla.facade.CalendarSelectionModel;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.RaplaAction;


public class PrintAction extends RaplaAction {
    CalendarSelectionModel model;
    PageFormat m_pageFormat;
    final Map<String,SwingViewFactory> factoryMap;
    @Inject
    public PrintAction(RaplaContext sm, Map<String, SwingViewFactory> factoryMap) {
        super( sm);
        this.factoryMap = factoryMap;
        setEnabled(false);
        putValue(NAME,getString("print"));
        putValue(SMALL_ICON, getIcon("icon.print"));
    }

    public void setModel(CalendarSelectionModel settings) {
        this.model = settings;
        setEnabled(settings != null);
    }


    public void setPageFormat(PageFormat pageFormat) {
        m_pageFormat = pageFormat;
    }


    public void actionPerformed() {
        Component parent = getMainComponent();
        try {
            boolean modal = true;
            CalendarPrintDialog dialog = new CalendarPrintDialog(getContext(),(Frame)parent);

            dialog.init(modal,factoryMap,model,m_pageFormat);
            final Dimension dimension = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            dialog.setSize(new Dimension(
                                        Math.min(dimension.width,900)
                                        ,Math.min(dimension.height-10,700)
                                        )
                          );
            
            SwingUtilities.invokeLater( new Runnable() {
                public void run()
                {
                    dialog.setSize(new Dimension(
                                             Math.min(dimension.width,900)
                                             ,Math.min(dimension.height-11,699)
                                             )
                               );
                }
                
            }
            );
            dialog.startNoPack();
            
        } catch (Exception ex) {
            showException(ex, parent);
        }
    }
}

