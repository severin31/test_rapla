package org.rapla.client.edit.reservation.sample;

import com.google.web.bindery.event.shared.EventBus;
import org.rapla.client.edit.reservation.ReservationController;
import org.rapla.client.edit.reservation.sample.ReservationView.Presenter;
import org.rapla.client.event.StopActivityEvent;
import org.rapla.entities.User;
import org.rapla.entities.domain.Appointment;
import org.rapla.entities.domain.PermissionContainer;
import org.rapla.entities.domain.RepeatingType;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.dynamictype.Attribute;
import org.rapla.entities.dynamictype.Classification;
import org.rapla.entities.dynamictype.DynamicType;
import org.rapla.entities.dynamictype.DynamicTypeAnnotations;
import org.rapla.facade.ClientFacade;
import org.rapla.framework.RaplaException;
import org.rapla.framework.RaplaLocale;
import org.rapla.framework.logger.Logger;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class ReservationPresenter implements ReservationController, Presenter
{
    public static final String EDIT_ACTIVITY_ID = "edit";

    @Inject private ClientFacade facade;
    @Inject private Logger logger;
    @Inject private RaplaLocale raplaLocale;

    @Inject private EventBus eventBus;

    private ReservationView<?> view;
    private Reservation editReservation;
    Appointment selectedAppointment;
    private boolean isNew;

    @SuppressWarnings({ "rawtypes", "unchecked" }) @Inject public ReservationPresenter(ReservationView view)
    {
        this.view = view;
        view.setPresenter(this);
    }
    
    @Override
    public boolean isVisible()
    {
        return view.isVisible();
    }

    @Override public void edit(final Reservation event, boolean isNew)
    {
        try
        {
            this.isNew = isNew;
            editReservation = facade.edit(event);
            selectedAppointment = editReservation.getAppointments().length > 0 ? editReservation.getAppointments()[0] : null;
            view.show(event);
        }
        catch (RaplaException e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    @Override public void onSaveButtonClicked()
    {
        logger.info("save clicked");
        try
        {
            facade.store(editReservation);
        }
        catch (RaplaException e1)
        {
            logger.error(e1.getMessage(), e1);
        }
        fireEventAndCloseView();
    }

    @Override public void onDeleteButtonClicked()
    {
        logger.info("delete clicked");
        try
        {
            facade.remove(editReservation);
        }
        catch (RaplaException e1)
        {
            logger.error(e1.getMessage(), e1);
        }
        fireEventAndCloseView();
    }

    @Override public void onCancelButtonClicked()
    {
        logger.info("cancel clicked");
        fireEventAndCloseView();
    }

    private void fireEventAndCloseView()
    {
        eventBus.fireEvent(new StopActivityEvent(EDIT_ACTIVITY_ID, editReservation.getId()));
        view.hide();
    }

    @Override public void changeAttribute(Attribute attribute, Object newValue)
    {
        final Classification classification = editReservation.getClassification();
        if (isAllowedToWrite(attribute, classification))
        {
            classification.setValue(attribute, newValue);
        }
        else
        {
            view.showWarning("Not allowed!", "Editing value for " + attribute.getName(raplaLocale.getLocale()));
        }
    }

    private boolean isAllowedToWrite(Attribute attribute, final Classification classification)
    {
        // TODO future
        return true;
    }

    @Override public boolean isDeleteButtonEnabled()
    {
        return isNew;
    }

    @Override public void changeClassification(DynamicType newDynamicType)
    {
        if (newDynamicType != null)
        {
            final Classification newClassification = newDynamicType.newClassification();
            editReservation.setClassification(newClassification);
            view.show(editReservation);
        }
    }

    @Override public Collection<DynamicType> getChangeableReservationDynamicTypes()
    {
        final Collection<DynamicType> creatableTypes = new ArrayList<DynamicType>();
        try
        {
            final DynamicType[] types = facade.getDynamicTypes(DynamicTypeAnnotations.VALUE_CLASSIFICATION_TYPE_RESERVATION);
            final User user = facade.getUser();
            for (DynamicType type : types)
            {
                if (PermissionContainer.Util.canCreate(type, user))
                {
                    creatableTypes.add(type);
                }
            }
        }
        catch (RaplaException e)
        {
            logger.warn(e.getMessage(), e);
        }
        return creatableTypes;
    }

    @Override public void newDateClicked()
    {
        Date startDate = new Date();
        Date endDate = new Date();
        try
        {
            Appointment newAppointment = facade.newAppointment(startDate, endDate);
            editReservation.addAppointment(newAppointment);
            this.selectedAppointment = newAppointment;
            view.updateAppointments(newAppointment);
        }
        catch (RaplaException e)
        {
            logger.error("Error creating new appointment: " + e.getMessage(), e);
        }
    }

    @Override public void selectAppointment(Appointment selectedAppointment)
    {
        this.selectedAppointment = selectedAppointment;
        view.updateAppointments(selectedAppointment);
    }

    @Override public void deleteDateClicked()
    {
        editReservation.removeAppointment(selectedAppointment);
        Appointment[] allAppointments = editReservation.getAppointments();
        Appointment newSelectedAppointment = allAppointments.length > 0 ? allAppointments[0] : null;
        selectedAppointment = newSelectedAppointment;
        view.updateAppointments(newSelectedAppointment);
    }

    @Override public void timeChanged(Date startDate, Date endDate)
    {
        if (selectedAppointment != null)
        {
            selectedAppointment.move(startDate, endDate);
        }
    }

    @Override public void allDayEvent(boolean wholeDays)
    {
        if (selectedAppointment != null)
        {
            selectedAppointment.setWholeDays(wholeDays);
            view.updateAppointments(selectedAppointment);
        }
    }

    @Override public void repeating(RepeatingType repeating)
    {
        if (selectedAppointment != null)
        {
            selectedAppointment.setRepeatingEnabled(repeating != null);
            selectedAppointment.getRepeating().setType(repeating);
        }
    }

    @Override public void convertAppointment()
    {
        // TODO implement me
    }

}
