/*--------------------------------------------------------------------------*
 | Copyright (C) 2014 Christopher Kohlhaas                                  |
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
package org.rapla.gui;

import java.util.List;

import org.rapla.framework.RaplaException;
import org.rapla.gui.toolkit.RaplaWidget;
import org.rapla.inject.ExtensionPoint;
import org.rapla.inject.InjectionContext;

@ExtensionPoint(context = InjectionContext.swing, id = "editDialogFor")
public interface EditComponent<T,W> extends RaplaWidget<W>
{
    /** maps all fields back to the current object.*/
    public void mapToObjects() throws RaplaException;
    public List<T> getObjects();
    public void setObjects(List<T> o) throws RaplaException;
}
