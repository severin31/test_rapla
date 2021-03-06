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
package org.rapla.gui.internal.edit;

import java.util.*;

import org.rapla.RaplaResources;
import org.rapla.entities.Category;
import org.rapla.entities.Entity;
import org.rapla.entities.RaplaType;
import org.rapla.entities.User;
import org.rapla.entities.configuration.Preferences;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.dynamictype.DynamicType;
import org.rapla.entities.dynamictype.DynamicTypeAnnotations;
import org.rapla.facade.ClientFacade;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.*;
import org.rapla.inject.DefaultImplementation;
import org.rapla.inject.InjectionContext;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.swing.*;

/** This class handles the edit-ui for all entities (except reservations). */

@DefaultImplementation(of=EditController.class, context = InjectionContext.swing)
@Singleton
public class EditControllerImpl<W> implements
		EditController {
	Collection<EditDialog<?>> editWindowList = new ArrayList<EditDialog<?>>();
	private Map<String,Provider<EditComponent>> editUiProviders;
	private final ReservationController reservationController;
	private final RaplaResources i18n;
	private final ClientFacade facade;
	private final RaplaContext context;

	@Inject
	public EditControllerImpl(Map<String,Provider<EditComponent>> editUiProviders, ReservationController controller, RaplaResources i18n, ClientFacade facade,
			RaplaContext context)
	{
		this.editUiProviders = editUiProviders;
		this.reservationController = controller;
		this.i18n = i18n;
		this.facade = facade;
		this.context = context;
	}

	@Override
	public <T extends Entity> void edit(T obj, PopupContext popupContext) throws RaplaException {
        String title = null;
        boolean createNew=false;
        EditCallback<List<T>> callback = null;
        List<T> list = Collections.singletonList(obj);
        editAndOpenDialog(list, title, popupContext, createNew, callback);
	}

	public <T extends Entity> void editNew(T obj, PopupContext popupContext) throws RaplaException {
        String title = null;
        boolean createNew=true;
        EditCallback<List<T>> callback = null;
        List<T> list = Collections.singletonList(obj);
        editAndOpenDialog(list, title, popupContext, createNew, callback);
	}

	public <T extends Entity> void edit(final T obj, final String title,final PopupContext popupContext, final EditController.EditCallback<T> callback) throws RaplaException {
        final EditCallback<List<T>> listCallback;
        if ( callback != null)
        {
            listCallback = new EditCallback<List<T>>()
            {
                @Override public void onFailure(Throwable e)
                {
                    callback.onFailure(e);
                }

                @Override public void onSuccess(List<T> editObject)
                {
                    callback.onSuccess(editObject.get(0));
                }

                @Override public void onAbort()
                {
                    callback.onAbort();
                }
            };
        }
        else
        {
            listCallback = null;
        }
        edit(Collections.singletonList(obj), title, popupContext, listCallback);
	}

	public <T extends Entity> void edit(List<T> list, String title,PopupContext popupContext, EditController.EditCallback<List<T>> callback) throws RaplaException {

		//		if selektion contains only one object start usual Edit dialog
        editAndOpenDialog(list, title, popupContext, false, callback);
	}


	void addEditDialog(EditDialog<?> editWindow) {
		editWindowList.add(editWindow);
	}

	void removeEditDialog(EditDialog<?> editWindow) {
		editWindowList.remove(editWindow);
	}


	@SuppressWarnings("unchecked")
    private <T extends Entity> EditComponent<T,W> createUI(T obj) throws RaplaException {
		RaplaType type = obj.getRaplaType();
		final String id = type.getTypeClass().getName();
		final Provider<EditComponent> editComponentProvider = editUiProviders.get(id);
		if ( editUiProviders != null)
		{
			EditComponent<T,W> ui = (EditComponent<T,W>)editComponentProvider.get();
			return ui;
		}
		else
		{
			throw new RuntimeException("Can't edit objects of type " + type.toString());
		}
	}

//	enhancement of the method to deal with arrays
	private String guessTitle(Collection obj) {
		RaplaType raplaType = getRaplaType(obj);
		String title = "";
		if(raplaType != null) {
			title = i18n.getString(raplaType.getLocalName());
		}

		return title;
	}

//	method for determining the consistent RaplaType from different objects
	protected RaplaType getRaplaType(Collection obj){
		Set<RaplaType> types = new HashSet<RaplaType>();


//		iterate all committed objects and store RaplayType of the objects in a Set
//		identic typs aren't stored double because of Set
		for (Object o : obj) {
			if (o instanceof Entity) {
				RaplaType type = ((Entity<?>) o).getRaplaType();
				types.add(type);
			}
		}

//		check if there is a explicit type, then return this type; otherwise return null
		if (types.size() == 1)
			return types.iterator().next();
		else
			return null;
	}

    private <T extends Entity> void editAndOpenDialog(List<T> list, String title, PopupContext popupContext, boolean createNew, EditCallback<List<T>> callback) throws RaplaException {
        if( list.size() == 0)
        {
            throw new RaplaException("Empty list not allowed. You must have at least one entity to edit.");
        }
        if(title == null)
        {
            title = guessTitle(list);

        }
        //		checks if all entities are from the same type; otherwise return
        if(getRaplaType(list) == null)
        {
            if (callback != null)
            {
                callback.onAbort();
            }
            return;
        }

        if ( list.size() == 1)
         {
             Entity<?> testObj = (Entity<?>) list.get(0);
             if ( testObj instanceof Reservation)
             {
                 reservationController.edit((Reservation) testObj);
                 return;
             }
             // Lookup if the entity (not a reservation) is already beeing edited
             EditDialog<?> c = null;
             Iterator<EditDialog<?>> it = editWindowList.iterator();
             while (it.hasNext()) {
                 c =  it.next();
                 List<?> editObj = c.ui.getObjects();
                 if (editObj != null && editObj.size() == 1 )
                 {
                     Object first = editObj.get(0);
                     if (first  instanceof Entity && ((Entity<?>) first).isIdentical(testObj))
                     {
                         break;
                     }
                 }
                 c = null;
             }

             if (c != null)
             {
                 c.dlg.requestFocus();
                 c.dlg.toFront();
                 return;
             }
         }
        //		gets for all objects in array a modifiable version and add it to a set to avoid duplication
    	Collection<T> toEdit = facade.edit(list);
    	if (toEdit.size() > 0) {
        	EditComponent<T,JComponent> ui = (EditComponent<T,JComponent>)createUI(toEdit.iterator().next());
        	EditDialog<T> gui = new EditDialog<T>(context, ui);
            gui.start(toEdit, title, popupContext, createNew, callback);
        }
    }
}
