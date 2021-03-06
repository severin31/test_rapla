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
package org.rapla.gui.internal.action.user;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.rapla.client.ClientService;
import org.rapla.entities.User;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.EditComponent;
import org.rapla.gui.EditController;
import org.rapla.gui.PopupContext;
import org.rapla.gui.RaplaAction;
import org.rapla.gui.internal.edit.EditDialog;


public class UserAction extends RaplaAction {
    Object object;
    public final int NEW = 1;
    public final int SWITCH_TO_USER = 3;
    int type = NEW;
    private PopupContext popupContext;

    public UserAction(RaplaContext sm,PopupContext popupContext) {
        super(sm);
        this.popupContext = popupContext;
    }

    public UserAction setNew() {
        type = NEW;
        putValue(NAME, getString("user"));
        putValue(SMALL_ICON, getIcon("icon.new"));
        update();
        return this;
    }

    public UserAction setSwitchToUser() {
        type = SWITCH_TO_USER;
        ClientService service = getService( ClientService.class);
        if (service.canSwitchBack()) {
            putValue(NAME, getString("switch_back"));
        } else {
            putValue(NAME, getString("switch_to"));
        }
        return this;
    }

    public void changeObject(Object object) {
        this.object = object;
        update();
    }

    private void update() {
        User user = null;
        try {
            user = getUser();
            if (type == NEW) {
                setEnabled(isAdmin());
            } else if (type == SWITCH_TO_USER) {
                ClientService service = getService(ClientService.class);
                setEnabled(service.canSwitchBack() || (object != null && isAdmin() && !user.equals(object)));
            }
        } catch (RaplaException ex) {
            setEnabled(false);
            return;
        }

    }

    public void actionPerformed() {
        try {
            if (type == SWITCH_TO_USER) {
                ClientService service = getService( ClientService.class);
            	if (service.canSwitchBack()) {
                    service.switchTo(null);
                    //putValue(NAME, getString("switch_to"));
                } else if ( object != null ){
                    service.switchTo((User) object);
                   // putValue(NAME, getString("switch_back"));
                }
            } else if (type == NEW) {
                User newUser = getModification().newUser();
                // create new user dialog and show password dialog if user is created successfully
                final String title = getString("user");
                getEditController().edit(newUser, title,popupContext,new EditController.EditCallback<User>()
                    {
                            @Override public void onFailure(Throwable e)
                            {
                                showException( e, popupContext);
                            }

                            @Override public void onSuccess(User editObject)
                            {
                                object = editObject;
                                if (getUserModule().canChangePassword())
                                {
                                    try
                                    {
                                        changePassword(editObject, false);
                                    }
                                    catch (RaplaException e)
                                    {
                                        onFailure(e);
                                    }
                                }
                            }

                            @Override public void onAbort()
                            {
                            }
                        }
                );

            }
        } catch (RaplaException ex) {
            showException(ex, popupContext);
        }
    }

    public void changePassword(User user,boolean showOld) throws RaplaException{
        new PasswordChangeAction(getContext(),popupContext).changePassword( user, showOld);
    }

}
