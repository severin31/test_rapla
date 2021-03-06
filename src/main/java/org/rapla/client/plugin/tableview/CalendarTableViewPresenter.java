package org.rapla.client.plugin.tableview;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;

import org.rapla.client.base.CalendarPlugin;
import org.rapla.client.edit.reservation.sample.ReservationPresenter;
import org.rapla.client.event.StartActivityEvent;
import org.rapla.client.plugin.tableview.CalendarTableView.Presenter;
import org.rapla.components.util.DateTools;
import org.rapla.entities.domain.Reservation;
import org.rapla.facade.CalendarSelectionModel;
import org.rapla.framework.RaplaException;
import org.rapla.framework.logger.Logger;
import org.rapla.inject.Extension;

import com.google.web.bindery.event.shared.EventBus;

@Extension(provides = CalendarPlugin.class, id = CalendarTableViewPresenter.TABLE_VIEW)
public class CalendarTableViewPresenter<W> implements Presenter, CalendarPlugin<W>
{

    public static final String TABLE_VIEW = "table";
    private final CalendarTableView<W> view;
    @Inject
    private Logger logger;
    @Inject
    private EventBus eventBus;
    @Inject
    private CalendarSelectionModel model;

    @SuppressWarnings("unchecked")
    @Inject
    public CalendarTableViewPresenter(@SuppressWarnings("rawtypes") CalendarTableView view)
    {
        this.view = view;
        this.view.setPresenter(this);
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }

    @Override
    public String getName()
    {
        return "list";
    }

    @Override
    public Date calcNext(Date currentDate)
    {
        return DateTools.addMonths(currentDate, 1);
    }

    @Override
    public Date calcPrevious(Date currentDate)
    {
        return DateTools.addMonths(currentDate, -1);
    }

    @Override
    public W provideContent()
    {
        updateContent();
        return view.provideContent();
    }

    @Override
    public void selectReservation(Reservation selectedObject)
    {
        final StartActivityEvent activity = new StartActivityEvent(ReservationPresenter.EDIT_ACTIVITY_ID, selectedObject.getId());
        eventBus.fireEvent(activity);
        logger.info("selection changed");

    }

    @Override
    public void updateContent()
    {
        try
        {
            Reservation[] reservations = model.getReservations();
            Collection<Reservation> result = Arrays.asList(reservations);
            logger.info(result.size() + " Reservations loaded.");
            view.update(result);
        }
        catch (RaplaException e)
        {
            logger.error(e.getMessage(), e);
        }
    }
}
