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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.rapla.client.extensionpoints.AnnotationEditAttributeExtension;
import org.rapla.components.calendar.DateChangeEvent;
import org.rapla.components.calendar.DateChangeListener;
import org.rapla.components.calendar.RaplaCalendar;
import org.rapla.components.calendar.RaplaNumber;
import org.rapla.components.layout.TableLayout;
import org.rapla.entities.Annotatable;
import org.rapla.entities.Category;
import org.rapla.entities.dynamictype.Attribute;
import org.rapla.entities.dynamictype.AttributeAnnotations;
import org.rapla.entities.dynamictype.AttributeType;
import org.rapla.entities.dynamictype.ConstraintIds;
import org.rapla.entities.dynamictype.DynamicType;
import org.rapla.entities.dynamictype.DynamicTypeAnnotations;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.RaplaGUIComponent;
import org.rapla.gui.internal.edit.RaplaListEdit.NameProvider;
import org.rapla.gui.internal.edit.annotation.AnnotationEditUI;
import org.rapla.gui.internal.edit.fields.AbstractEditField;
import org.rapla.gui.internal.edit.fields.BooleanField;
import org.rapla.gui.internal.edit.fields.CategorySelectField;
import org.rapla.gui.internal.edit.fields.ListField;
import org.rapla.gui.internal.edit.fields.MultiLanguageField;
import org.rapla.gui.internal.edit.fields.TextField;
import org.rapla.gui.toolkit.DialogUI;
import org.rapla.gui.toolkit.EmptyLineBorder;
import org.rapla.gui.toolkit.RaplaButton;
import org.rapla.gui.toolkit.RaplaWidget;

public class AttributeEdit extends RaplaGUIComponent
    implements
    RaplaWidget
{
    RaplaListEdit<Attribute> listEdit;
    DynamicType dt;
    AttributeDefaultConstraints constraintPanel;
    ArrayList<ChangeListener> listenerList = new ArrayList<ChangeListener>();

    Listener listener = new Listener();
    DefaultListModel model = new DefaultListModel();



    @Inject
    public AttributeEdit(RaplaContext context, AttributeDefaultConstraints constraintPanel) throws RaplaException {
        super( context);

        // 1.
        //Set<AnnotationEditAttributeExtension> attributeExtensionSet = context.lookup(AnnotationEditExtensionWrapper.class).getAttributeExtensions();

        // 2.
        //Injector injector = context.lookup(Injector.class);
        //injector.inject(DefaultConst);

        // 3.
        //context.lookup(AttributeDefaultConstraints.class);


        this.constraintPanel = constraintPanel;
        listEdit = new RaplaListEdit<Attribute>( getI18n(),getImages(), constraintPanel.getComponent(), listener );
        listEdit.setListDimension( new Dimension( 200,220 ) );

        constraintPanel.addChangeListener( listener );
        listEdit.setNameProvider( new NameProvider<Attribute>()
                {
                    @Override
                    public String getName(Attribute a) {
                        String value = a.getName(getRaplaLocale().getLocale());
                        value = "{" + a.getKey() + "} " + value;
                        int index = listEdit.indexOf( a);
                        if ( index >= 0)
                        {
                            value = (index + 1) +") " + value;
                        }
                        return value;
                    }
                }
                );
        listEdit.getComponent().setBorder( BorderFactory.createTitledBorder( new EmptyLineBorder(),getString("attributes")) );
    }


    public RaplaWidget getConstraintPanel() {
        return constraintPanel;
    }
    
    public Attribute getSelectedAttribute()
    {
        return listEdit.getSelectedValue();
    }
    
    public void selectAttribute( Attribute attribute)
    {
        
        boolean shouldScroll = true;
        listEdit.getList().setSelectedValue( attribute, shouldScroll);
    }
   

    class Listener implements ActionListener,ChangeListener {
        public void actionPerformed(ActionEvent evt) {
            int index = getSelectedIndex();
            try {
                if (evt.getActionCommand().equals("remove")) {
                    removeAttribute();
                } else if (evt.getActionCommand().equals("new")) {
                    createAttribute();
                } else if (evt.getActionCommand().equals("edit")) {
                    Attribute attribute = (Attribute) listEdit.getList().getSelectedValue();
                    constraintPanel.mapFrom( attribute );
                } else if (evt.getActionCommand().equals("moveUp")) {
                    dt.exchangeAttributes(index, index -1);
                    updateModel(null);
                } else if (evt.getActionCommand().equals("moveDown")) {
                    dt.exchangeAttributes(index, index + 1);
                    updateModel(null);
                }

            } catch (RaplaException ex) {
                showException(ex, getComponent());
            }
        }
        public void stateChanged(ChangeEvent e) {
            try {
                confirmEdits();
                fireContentChanged();
            } catch (RaplaException ex) {
                showException(ex, getComponent());
            }
        }
    }

    public JComponent getComponent() {
        return listEdit.getComponent();
    }

    public int getSelectedIndex() {
        return listEdit.getList().getSelectedIndex();
    }

    public void setDynamicType(DynamicType dt)  {
        this.dt = dt;
        updateModel(null);
    }

    @SuppressWarnings("unchecked")
	private void updateModel(Attribute newSelectedItem) {
        Attribute selectedItem = newSelectedItem != null ? newSelectedItem : listEdit.getSelectedValue();
        model.clear();
        Attribute[] attributes = dt.getAttributes();
        for (int i = 0; i < attributes.length; i++ ) {
            model.addElement( attributes[i] );
        }
        listEdit.getList().setModel(model);
        if ( listEdit.getSelectedValue() != selectedItem )
            listEdit.getList().setSelectedValue(selectedItem, true );
    }

    @SuppressWarnings("unchecked")
	public void confirmEdits() throws RaplaException {
        if ( getSelectedIndex() < 0 )
            return;
        Attribute attribute =  listEdit.getSelectedValue();
        constraintPanel.mapTo (attribute );
        model.set( model.indexOf( attribute ), attribute );
    }

    private String createNewKey() {
        Attribute[] atts = dt.getAttributes();
        int max = 1;
        for (int i=0;i<atts.length;i++) {
            String key = atts[i].getKey();
            if (key.length()>1
                && key.charAt(0) =='a'
                && Character.isDigit(key.charAt(1))
                )
                {
                    try {
                        int value = Integer.valueOf(key.substring(1)).intValue();
                        if (value >= max)
                            max = value + 1;
                    } catch (NumberFormatException ex) {
                    }
                }
        }
        return "a" + (max);
    }

    void removeAttribute()  {
    	List<Attribute> toRemove = new ArrayList<Attribute>();
    	
    	for ( int index:listEdit.getList().getSelectedIndices())
    	{
    		Attribute att = dt.getAttributes() [index];
    		toRemove.add( att);
    	}
    	for (Attribute att:toRemove)
    	{
    		dt.removeAttribute(att);
    	}
        updateModel(null);
    }

    void createAttribute() throws RaplaException {
        confirmEdits();
        AttributeType type = AttributeType.STRING;
        Attribute att =  getModification().newAttribute(type);
        String language = getRaplaLocale().getLocale().getLanguage();
		att.getName().setName(language, getString("attribute"));
        att.setKey(createNewKey());
        dt.addAttribute(att);
        updateModel( att);
//        int index = dt.getAttributes().length -1;
//		listEdit.getList().setSelectedIndex( index );
		constraintPanel.name.selectAll();
		constraintPanel.name.requestFocus();
		
    }

    public void addChangeListener(ChangeListener listener) {
        listenerList.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listenerList.remove(listener);
    }

    public ChangeListener[] getChangeListeners() {
        return listenerList.toArray(new ChangeListener[]{});
    }

    protected void fireContentChanged() {
        if (listenerList.size() == 0)
            return;
        ChangeEvent evt = new ChangeEvent(this);
        ChangeListener[] listeners = getChangeListeners();
        for (int i = 0;i<listeners.length; i++) {
            listeners[i].stateChanged(evt);
        }
    }

}

