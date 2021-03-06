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
package org.rapla.gui.internal.edit.reservation;

import java.util.Collection;
import java.util.Locale;

import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.rapla.components.xmlbundle.I18nBundle;
import org.rapla.entities.domain.Appointment;
import org.rapla.entities.domain.AppointmentFormater;
import org.rapla.entities.domain.Reservation;
import org.rapla.facade.CalendarModel;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.client.extensionpoints.EventCheck;
import org.rapla.gui.PopupContext;
import org.rapla.gui.RaplaGUIComponent;
import org.rapla.gui.internal.SwingPopupContext;
import org.rapla.gui.toolkit.DialogUI;
import org.rapla.inject.Extension;

@Extension(provides = EventCheck.class,id="defaultcheck")
public class DefaultReservationCheck extends RaplaGUIComponent implements EventCheck
{
    AppointmentFormater appointmentFormater;
    CalendarModel model;
    @Inject
    public DefaultReservationCheck(RaplaContext context, AppointmentFormater appointmentFormater, CalendarModel model) {
        super(context);
        this.appointmentFormater = appointmentFormater;
        this.model = model;
    }

    public boolean check(Collection<Reservation> reservations, PopupContext sourceComponent) throws RaplaException {
        try
        {
            
            JPanel warningPanel = new JPanel();
            for (Reservation reservation:reservations)
            {
                getClientFacade().checkReservation( reservation);
                Appointment[] appointments = reservation.getAppointments();
                Appointment duplicatedAppointment = null;
                for (int i=0;i<appointments.length;i++) {
                    for (int j=i + 1;j<appointments.length;j++)
                        if (appointments[i].matches(appointments[j])) {
                            duplicatedAppointment = appointments[i];
                            break;
                        }
                }

                final I18nBundle i18n = getI18n();
                Locale locale = i18n.getLocale();
                String name = reservation.getName(locale);
                if (name.trim().length() == 0) {
                    final String warning = getI18n().getString("error.no_reservation_name");
                    JLabel warningLabel = new JLabel();
                    warningLabel.setForeground(java.awt.Color.red);
                    warningLabel.setText
                    (
                      warning
                     );
                    warningPanel.add( warningLabel);
                }

                if (!model.isMatchingSelectionAndFilter(reservation, null))
                {
                    JLabel warningLabel = new JLabel();
                    warningLabel.setForeground(java.awt.Color.red);
                    final String warning = getI18n().format("warning.not_in_calendar",reservation.getName( getLocale()));
                    warningLabel.setText
                        (
                          warning
                         );
                    warningPanel.add( warningLabel);
                }
                    
                
                warningPanel.setLayout( new BoxLayout( warningPanel, BoxLayout.Y_AXIS));
                if (duplicatedAppointment != null) {
                    JLabel warningLabel = new JLabel();
                    warningLabel.setForeground(java.awt.Color.red);
                    warningLabel.setText
                        (getI18n().format
                         (
                          "warning.duplicated_appointments"
                          ,appointmentFormater.getShortSummary(duplicatedAppointment)
                          )
                         );
                    warningPanel.add( warningLabel);
                }
                
                if (reservation.getAllocatables().length == 0)
                {
                    JLabel warningLabel = new JLabel();
                    warningLabel.setForeground(java.awt.Color.red);
                    warningLabel.setText(getString("warning.no_allocatables_selected"));
                    warningPanel.add( warningLabel);
                }
            }
            
            if (  warningPanel.getComponentCount() > 0) {
                DialogUI dialog = DialogUI.create(
                        getContext()
                        ,((SwingPopupContext)sourceComponent).getParent()
                        ,true
                        ,warningPanel
                        ,new String[] {
                        getString("continue")
                        ,getString("back")
                }
                );
                dialog.setTitle( getString("warning"));
                dialog.setIcon(getIcon("icon.warning"));
                dialog.setDefault(1);
                dialog.getButton(0).setIcon(getIcon("icon.save"));
                dialog.getButton(1).setIcon(getIcon("icon.cancel"));
                dialog.start();
                if (dialog.getSelectedIndex() == 0)
                {
                    return true;
                }
                else
                {
                	return false;
                }
            }

            

            return true;
        }
        catch (RaplaException ex)
        {
            showWarning( ex.getMessage(), ((SwingPopupContext)sourceComponent).getParent());
            return false;
        }
    }

}



