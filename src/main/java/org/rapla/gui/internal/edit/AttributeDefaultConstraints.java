package org.rapla.gui.internal.edit;

import org.rapla.client.extensionpoints.AnnotationEditAttributeExtension;
import org.rapla.components.calendar.DateChangeEvent;
import org.rapla.components.calendar.DateChangeListener;
import org.rapla.components.calendar.RaplaCalendar;
import org.rapla.components.calendar.RaplaNumber;
import org.rapla.components.layout.TableLayout;
import org.rapla.entities.Annotatable;
import org.rapla.entities.Category;
import org.rapla.entities.dynamictype.*;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.internal.edit.annotation.AnnotationEditUI;
import org.rapla.gui.internal.edit.fields.*;
import org.rapla.gui.toolkit.DialogUI;
import org.rapla.gui.toolkit.RaplaButton;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class AttributeDefaultConstraints extends AbstractEditField
    implements ActionListener
        ,ChangeListener
{
    JPanel panel = new JPanel();
    JLabel nameLabel = new JLabel();
    JLabel keyLabel = new JLabel();
    JLabel typeLabel = new JLabel();
    JLabel categoryLabel = new JLabel();
    JLabel dynamicTypeLabel = new JLabel();
    JLabel defaultLabel = new JLabel();
    JLabel multiSelectLabel = new JLabel();
    JLabel tabLabel = new JLabel();
    JLabel specialkeyLabel = new JLabel(); // BJO
    AttributeType types[] = {
        AttributeType.BOOLEAN
        ,AttributeType.STRING
        ,AttributeType.INT
        ,AttributeType.CATEGORY
        ,AttributeType.ALLOCATABLE
        ,AttributeType.DATE
    };

    String tabs[] = {
            AttributeAnnotations.VALUE_EDIT_VIEW_MAIN
            ,AttributeAnnotations.VALUE_EDIT_VIEW_ADDITIONAL
            ,AttributeAnnotations.VALUE_EDIT_VIEW_NO_VIEW
    };

    boolean mapping = false;
    MultiLanguageField name ;
    TextField key;
    JComboBox classSelect = new JComboBox();
    ListField<DynamicType> dynamicTypeSelect;

    CategorySelectField categorySelect;
    CategorySelectField defaultSelectCategory;
    TextField defaultSelectText;
    BooleanField defaultSelectBoolean;
    BooleanField multiSelect;
    RaplaNumber defaultSelectNumber = new RaplaNumber(new Long(0),null,null, false);
    RaplaCalendar defaultSelectDate ;
    RaplaButton annotationButton = new RaplaButton(RaplaButton.DEFAULT);
    JComboBox tabSelect = new JComboBox();
    DialogUI dialog;
    boolean emailPossible = false;
    Category rootCategory;
    AnnotationEditUI annotationEdit;
    Attribute attribute;

    @Inject
    public AttributeDefaultConstraints(RaplaContext context, Set<AnnotationEditAttributeExtension> attributeExtensionSet) throws RaplaException
    {
        super( context );
        annotationEdit = new AnnotationEditUI(context, attributeExtensionSet);
        key = new TextField(context);
        name = new MultiLanguageField(context);
        Collection<DynamicType> typeList = new ArrayList<DynamicType>(
                Arrays.asList(getQuery().getDynamicTypes(DynamicTypeAnnotations.VALUE_CLASSIFICATION_TYPE_RESOURCE)));
        typeList.addAll(Arrays.asList(getQuery().getDynamicTypes(DynamicTypeAnnotations.VALUE_CLASSIFICATION_TYPE_PERSON)));
        dynamicTypeSelect = new ListField<DynamicType>(context,true );
        dynamicTypeSelect.setVector( typeList );
        rootCategory = getQuery().getSuperCategory();

        categorySelect = new CategorySelectField(context,rootCategory);
        categorySelect.setUseNull(false);
        defaultSelectCategory = new CategorySelectField(context,rootCategory);
        defaultSelectText = new TextField(context);
        addCopyPaste( defaultSelectNumber.getNumberField());
        //addCopyPaste( expectedRows.getNumberField());
        //addCopyPaste( expectedColumns.getNumberField());

        defaultSelectBoolean = new BooleanField(context);
        defaultSelectDate = createRaplaCalendar();
        defaultSelectDate.setNullValuePossible( true);
        defaultSelectDate.setDate( null);
        multiSelect = new BooleanField(context);
        double fill = TableLayout.FILL;
        double pre = TableLayout.PREFERRED;
        panel.setLayout( new TableLayout( new double[][]
            {{5, pre, 5, fill },  // Columns
             {5, pre ,5, pre, 5, pre, 5, pre, 5, pre, 5, pre, 5,pre, 5, pre, 5}} // Rows
                                          ));
        panel.add("1,1,l,f", nameLabel);
        panel.add("3,1,f,f", name.getComponent() );
        panel.add("1,3,l,f", keyLabel);
        panel.add("3,3,f,f", key.getComponent() );
        panel.add("1,5,l,f", typeLabel);
        panel.add("3,5,l,f", classSelect);

        // constraints
        panel.add("1,7,l,t", categoryLabel);
        panel.add("3,7,l,t", categorySelect.getComponent());
        panel.add("1,7,l,t", dynamicTypeLabel);
        panel.add("3,7,l,t", dynamicTypeSelect.getComponent());
        panel.add("1,9,l,t", defaultLabel);
        panel.add("3,9,l,t", defaultSelectCategory.getComponent());
        panel.add("3,9,l,t", defaultSelectText.getComponent());
        panel.add("3,9,l,t", defaultSelectBoolean.getComponent());
        panel.add("3,9,l,t", defaultSelectDate);
        panel.add("3,9,l,t", defaultSelectNumber);
        panel.add("1,11,l,t", multiSelectLabel);
        panel.add("3,11,l,t", multiSelect.getComponent());
        panel.add("1,13,l,t", tabLabel);
        panel.add("3,13,l,t", tabSelect);
        panel.add("1,15,l,t", specialkeyLabel); // BJO
        panel.add("3,15,l,t", annotationButton);
        annotationButton.setText(getString("edit"));
        annotationButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    showAnnotationDialog();
                } catch (RaplaException ex) {
                    showException(ex, getComponent());
                }

            }
        });

        setModel();

        nameLabel.setText(getString("name") + ":");
        keyLabel.setText(getString("key") +" *" + ":");
        typeLabel.setText(getString("type") + ":");
        categoryLabel.setText(getString("root") + ":");
        dynamicTypeLabel.setText(getString("root") + ":");
        tabLabel.setText(getString("edit-view") + ":");
        multiSelectLabel.setText("Multiselect:");
        defaultLabel.setText(getString("default") + ":");
        specialkeyLabel.setText(getString("options") + ":");
        categorySelect.addChangeListener ( this );
        categorySelect.addChangeListener( new ChangeListener() {

            public void stateChanged(ChangeEvent e)
            {
                final Category rootCategory = categorySelect.getValue();
                defaultSelectCategory.setRootCategory( rootCategory );
                defaultSelectCategory.setValue( null);
                defaultSelectCategory.getComponent().setEnabled( rootCategory != null);
            }
        }

        );
        name.addChangeListener ( this );
        key.addChangeListener ( this );
        classSelect.addActionListener ( this );
        tabSelect.addActionListener( this);
        multiSelect.addChangeListener( this );
        defaultSelectCategory.addChangeListener( this );
        defaultSelectText.addChangeListener( this );
        defaultSelectBoolean.addChangeListener( this );
        defaultSelectNumber.addChangeListener( this );
        defaultSelectDate.addDateChangeListener( new DateChangeListener() {

            public void dateChanged(DateChangeEvent evt)
            {
                stateChanged(null);
            }
        });
    }

	@SuppressWarnings("unchecked")
	private void setModel() {
		DefaultComboBoxModel model = new DefaultComboBoxModel();
        for ( int i = 0; i < types.length; i++ ) {
            model.addElement(getString("type." + types[i]));
        }
        classSelect.setModel( model );

        model = new DefaultComboBoxModel();
        for ( int i = 0; i < tabs.length; i++ ) {
            model.addElement(getString(tabs[i]));
        }
        tabSelect.setModel( model );
	}

    public JComponent getComponent() {
        return panel;
    }

    private void clearValues() {
    	 categorySelect.setValue(null);
         defaultSelectCategory.setValue( null);
         defaultSelectText.setValue("");
         defaultSelectBoolean.setValue( null);
         defaultSelectNumber.setNumber(null);
         defaultSelectDate.setDate(null);
         multiSelect.setValue( Boolean.FALSE);
	}

    public void mapFrom(Attribute attribute) throws RaplaException  {
    	clearValues();
        try {
            mapping = true;
            this.attribute = attribute;
            clearValues();
            String classificationType = attribute.getDynamicType().getAnnotation(DynamicTypeAnnotations.KEY_CLASSIFICATION_TYPE);
			emailPossible = classificationType != null && (classificationType.equals( DynamicTypeAnnotations.VALUE_CLASSIFICATION_TYPE_PERSON) || classificationType.equals( DynamicTypeAnnotations.VALUE_CLASSIFICATION_TYPE_RESOURCE));
            name.setValue( attribute.getName());
            key.setValue( attribute.getKey());
            final AttributeType attributeType = attribute.getType();
            classSelect.setSelectedItem(getString("type." + attributeType));
            if (attributeType.equals(AttributeType.CATEGORY)) {
                final Category rootCategory = (Category)attribute.getConstraint(ConstraintIds.KEY_ROOT_CATEGORY);
                categorySelect.setValue( rootCategory );
                defaultSelectCategory.setRootCategory( rootCategory);
                defaultSelectCategory.setValue( (Category)attribute.convertValue(attribute.defaultValue()));
                defaultSelectCategory.getComponent().setEnabled( rootCategory != null);
            }
            else if (attributeType.equals(AttributeType.ALLOCATABLE)) {
                final DynamicType rootCategory = (DynamicType)attribute.getConstraint(ConstraintIds.KEY_DYNAMIC_TYPE);
                dynamicTypeSelect.setValue( rootCategory );
            }
            else if (attributeType.equals(AttributeType.STRING))
            {
                defaultSelectText.setValue( (String)attribute.defaultValue());
            }
            else if (attributeType.equals(AttributeType.BOOLEAN))
            {
                defaultSelectBoolean.setValue( (Boolean)attribute.defaultValue());
            }
            else if (attributeType.equals(AttributeType.INT))
            {
                defaultSelectNumber.setNumber( (Number)attribute.defaultValue());
            }
            else if (attributeType.equals(AttributeType.DATE))
            {
                defaultSelectDate.setDate( (Date)attribute.defaultValue());
            }

            if (attributeType.equals(AttributeType.CATEGORY) || attributeType.equals(AttributeType.ALLOCATABLE)) {
            	Boolean multiSelectValue = (Boolean) attribute.getConstraint(ConstraintIds.KEY_MULTI_SELECT) ;
            	multiSelect.setValue( multiSelectValue != null ? multiSelectValue: Boolean.FALSE );
            }
            String selectedTab = attribute.getAnnotation(AttributeAnnotations.KEY_EDIT_VIEW, AttributeAnnotations.VALUE_EDIT_VIEW_MAIN);
            tabSelect.setSelectedItem(getString(selectedTab));
            update();
        } finally {
            mapping = false;
        }
    }

    public void mapTo(Attribute attribute) throws RaplaException  {
        attribute.getName().setTo( name.getValue());
        attribute.setKey( key.getValue());
        AttributeType type = types[classSelect.getSelectedIndex()];
        attribute.setType( type );
        if ( type.equals(AttributeType.CATEGORY)) {
            Object defaultValue = defaultSelectCategory.getValue();
            Object rootCategory = categorySelect.getValue();
            if ( rootCategory == null)
            {
                rootCategory = this.rootCategory;
                defaultValue = null;
            }
            attribute.setConstraint(ConstraintIds.KEY_ROOT_CATEGORY, rootCategory );
            attribute.setDefaultValue( defaultValue);
        } else {
            attribute.setConstraint(ConstraintIds.KEY_ROOT_CATEGORY, null);
        }

        if ( type.equals(AttributeType.ALLOCATABLE)) {
            Object rootType = dynamicTypeSelect.getValue();
//            if ( rootType == null)
//            {
//                rootType = this.rootCategory;
//            }
            attribute.setConstraint(ConstraintIds.KEY_DYNAMIC_TYPE, rootType );
	        attribute.setDefaultValue( null);
        } else {
            attribute.setConstraint(ConstraintIds.KEY_DYNAMIC_TYPE, null);
        }

        if ( type.equals(AttributeType.ALLOCATABLE) || type.equals(AttributeType.CATEGORY))
        {
            Boolean value = multiSelect.getValue();
            attribute.setConstraint(ConstraintIds.KEY_MULTI_SELECT, value);
        }
        else
        {
        	attribute.setConstraint(ConstraintIds.KEY_MULTI_SELECT, null);
        }

        if ( type.equals(AttributeType.BOOLEAN)) {
            final Object defaultValue = defaultSelectBoolean.getValue();
            attribute.setDefaultValue( defaultValue);
        }

        if ( type.equals(AttributeType.INT)) {
            final Object defaultValue = defaultSelectNumber.getNumber();
            attribute.setDefaultValue( defaultValue);
        }

        if ( type.equals(AttributeType.DATE)) {
            final Object defaultValue = defaultSelectDate.getDate();
            attribute.setDefaultValue( defaultValue);
        }
        if ( type.equals(AttributeType.STRING)) {
            final Object defaultValue = defaultSelectText.getValue();
            attribute.setDefaultValue( defaultValue);
        }
        List<Annotatable> asList = Arrays.asList((Annotatable)attribute);
        annotationEdit.mapTo(asList);
        String selectedTab = tabs[tabSelect.getSelectedIndex()];
        if ( selectedTab != null && !selectedTab.equals(AttributeAnnotations.VALUE_EDIT_VIEW_MAIN)) {
            attribute.setAnnotation(AttributeAnnotations.KEY_EDIT_VIEW,  selectedTab);
        } else {
            attribute.setAnnotation(AttributeAnnotations.KEY_EDIT_VIEW,  null);
        }
    }

    private void update() throws RaplaException {
        AttributeType type = types[classSelect.getSelectedIndex()];
        List<Annotatable> asList = Arrays.asList((Annotatable)attribute);
        annotationEdit.setObjects( asList);
        final boolean categoryVisible = type.equals(AttributeType.CATEGORY);
        final boolean allocatableVisible = type.equals(AttributeType.ALLOCATABLE);
        final boolean textVisible = type.equals(AttributeType.STRING);
        final boolean booleanVisible = type.equals(AttributeType.BOOLEAN);
        final boolean numberVisible = type.equals(AttributeType.INT);
        final boolean dateVisible  = type.equals(AttributeType.DATE);
        categoryLabel.setVisible( categoryVisible );
        categorySelect.getComponent().setVisible( categoryVisible );
        dynamicTypeLabel.setVisible( allocatableVisible);
        dynamicTypeSelect.getComponent().setVisible( allocatableVisible);
        defaultLabel.setVisible( !allocatableVisible);
        defaultSelectCategory.getComponent().setVisible( categoryVisible);
        defaultSelectText.getComponent().setVisible( textVisible);
        defaultSelectBoolean.getComponent().setVisible( booleanVisible);
        defaultSelectNumber.setVisible( numberVisible);
        defaultSelectDate.setVisible( dateVisible);
        multiSelectLabel.setVisible( categoryVisible || allocatableVisible);
        multiSelect.getComponent().setVisible( categoryVisible || allocatableVisible);
    }

    private void showAnnotationDialog() throws RaplaException
    {
        RaplaContext context = getContext();
        boolean modal = false;
        if (dialog != null)
        {
            dialog.close();
        }
        dialog = DialogUI.create(context
                ,getComponent()
                ,modal
                ,annotationEdit.getComponent()
                ,new String[] { getString("close")});

        dialog.getButton(0).setAction( new AbstractAction() {
            private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                fireContentChanged();
                dialog.close();
            }
        });
        dialog.setTitle(getString("select"));
        dialog.start();
    }

    public void actionPerformed(ActionEvent evt) {
        if (mapping)
            return;
        if ( evt.getSource() == classSelect) {
        	clearValues();
            AttributeType newType = types[classSelect.getSelectedIndex()];
            if (newType.equals(AttributeType.CATEGORY)) {
                categorySelect.setValue( rootCategory );
            }
        }
        fireContentChanged();
        try {
            update();
        } catch (RaplaException ex) {
            showException(ex, getComponent());
        }
    }

	public void stateChanged(ChangeEvent e) {
        if (mapping)
            return;
        fireContentChanged();
    }


}
