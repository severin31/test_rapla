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

import org.rapla.client.extensionpoints.PluginOptionPanel;
import org.rapla.entities.configuration.Preferences;
import org.rapla.entities.configuration.RaplaConfiguration;
import org.rapla.entities.configuration.internal.PreferencesImpl;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.Configuration;
import org.rapla.framework.DefaultConfiguration;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

abstract public class DefaultPluginOption extends RaplaGUIComponent implements PluginOptionPanel
{
    public DefaultPluginOption(RaplaContext sm)  {
        super(sm);
    }

    protected JCheckBox activate = new JCheckBox("Aktivieren");
    protected Configuration config;
    protected Preferences preferences;
    JComponent container;

    private Class getPluginClass()
    {
        return null;
    }

    /**
     * @throws RaplaException  
     */
    protected JPanel createPanel() throws RaplaException {
        JPanel panel = new JPanel();
        panel.setLayout( new BorderLayout());
        panel.add( activate, BorderLayout.NORTH );
        return panel;
    }
    
    
    /**
     * @see org.rapla.gui.OptionPanel#setPreferences(org.rapla.entities.configuration.Preferences)
     */
    public void setPreferences(Preferences preferences) {
       this.preferences = preferences;
    }

    /**
     * @see org.rapla.gui.OptionPanel#commit()
     */
    public void commit() throws RaplaException {
        writePluginConfig(true);
    }

    protected void writePluginConfig(boolean addChildren) {
        if ( true)
            return;
//        RaplaConfiguration config =  preferences.getEntry(RaplaComponent.PLUGIN_CONFIG);
//        if ( config  == null)
//	    {
//	    	config = new RaplaConfiguration("org.rapla.plugin");
//	    }
//        String className = getPluginClass().getName();
//        //getDescritorClassName()
//
//        RaplaConfiguration newChild = new RaplaConfiguration("plugin" );
//        newChild.setAttribute( "enabled", activate.isSelected());
//        newChild.setAttribute( "class", className);
//        if ( addChildren)
//        {
//            addChildren( newChild );
//        }
//        RaplaConfiguration newConfig = config.replace(config.find("class", className), newChild);
//        preferences.putEntry(RaplaComponent.PLUGIN_CONFIG, newConfig);
    }
    
    /**
     * 
     * @param newConfig
     */
    protected void addChildren( DefaultConfiguration newConfig) {
    }

    /**
     * 
     * @param config
     */
    protected void readConfig( Configuration config)   {
    	
    }

    /**
     * @see org.rapla.gui.OptionPanel#show()
     */
    public void show() throws RaplaException 
    {
        activate.setText( getString("selected"));
        container = createPanel();
//	    Class pluginClass = getPluginClass();
//        boolean defaultSelection = false;
//		try {
//			defaultSelection = ((Boolean )pluginClass.getField("ENABLE_BY_DEFAULT").get( null));
//		} catch (Throwable e) {
//		}
//
		config = getConfig();

//		String className = getPluginClass().getName();
//		RaplaConfiguration pluginConfig =  preferences.getEntry(RaplaComponent.PLUGIN_CONFIG);
//        Configuration pluginClassConfig = null;
//		if ( pluginConfig != null)
//		{
//		    pluginClassConfig = pluginConfig.find("class", className);
//		}
//        if ( pluginClassConfig == null )
//        {
//            // use old config for compatibilty
//            pluginClassConfig = config;
//        }
        //activate.setSelected( defaultSelection);
        readConfig( config );
    }

    @SuppressWarnings({ "deprecation", "unused" })
    protected Configuration getConfig()  throws RaplaException {
        return new RaplaConfiguration();//((PreferencesImpl)preferences).getOldPluginConfig(getPluginClass().getName());
    }

    /**
     * @see org.rapla.gui.toolkit.RaplaWidget#getComponent()
     */
    public JComponent getComponent() {
        return container;
    }



    /**
     * @see org.rapla.entities.Named#getName(java.util.Locale)
     */
    public String getName(Locale locale) {
        return getPluginClass().getSimpleName();
    }

    public String toString()
    {
    	return getName(getLocale());
    }
       
}


