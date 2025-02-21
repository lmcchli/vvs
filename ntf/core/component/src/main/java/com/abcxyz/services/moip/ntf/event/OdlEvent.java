package com.abcxyz.services.moip.ntf.event;

import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.services.moip.ntf.coremgmt.EventSentListener;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.out.outdial.NtfTimerTask;
import com.mobeon.common.commands.Command;
import com.mobeon.common.commands.CommandHandler;
import com.mobeon.common.commands.State;
import com.mobeon.ntf.Constants;

/**
 * Outdial event to be used for keeping out dial call information
 */
public class OdlEvent extends NtfEvent {

	/**
	 * information to be kept persistently in the event
	 */
    private String recipientId;
    private String userTel;
    private String odlTrigger;
    private int odlCode;
    private long startTime;
    private int stateNo;
    private int oprCode;
    private String key;
    private long phoneOnLockId=0;
    private String schedulerIdReminder;
    private Command command;

	/**
	 * information to be set dynamically
	 */
	private boolean fromLogin;
	private boolean fromNotify;

	private static final String ODL_TRIGGER_EVENT = "trg";
	private static final String ODL_CODE = "cod";
	private static final String CURRENT_STATE = "sta";
	private static final String CURRENT_OPERATION= "opr";
    private static final String START_TIME = "stime";
	private static final String COMMAND = "com";
    private static final String NOTIF_NUMBER = "notifnumber";

    public static final String SCHEDULER_ID = "sch-id";
    public static final String SCHEDULER_ID_REMINDER = "sch-rem";

	public static final String DASH = "-";

	/**
	 * Observable delegate that calls observers when persistent data is changed.
	 */
	private class ObservableDelegate extends Observable {
		void markChanged() {
			setChanged();
		}
	}


	private ObservableDelegate observableDelegate = new ObservableDelegate();

	public OdlEvent(String rcptId, String tel, String triggerEvent, int code, NtfEvent ntfEvent) {
	    super.initialize(ntfEvent);
	    initializeOdlEvent(rcptId, tel, triggerEvent, code);
	}

	public OdlEvent(String rcptId, String tel, MessageInfo msgInfo, String triggerEvent, int code) {
	    initializeOdlEvent(rcptId, tel, triggerEvent, code);
	    super.setMsgInfo(msgInfo);
	}

	private void initializeOdlEvent(String rcptId, String tel, String triggerEvent, int code) {
	    recipientId = rcptId;
	    userTel = tel;
	    startTime = System.currentTimeMillis();
	    key = createOdlEventKey();
	    odlTrigger = triggerEvent;
	    odlCode = code;
	    EventSentListener listener = NtfEventHandlerRegistry.getEventSentListener(NtfEventTypes.OUTDIAL.getName());
	    setSentListener(listener);
	}

	public OdlEvent(Properties props) throws InvalidOdlEventException{
		recipientId = props.getProperty(Constants.DEST_RECIPIENT_ID);
		userTel = props.getProperty(NOTIF_NUMBER);
		
		// Validates properties
		if (userTel == null || recipientId == null) {
		    throw new InvalidOdlEventException("Missing mandatory properties for outdial event: "
		            + Constants.DEST_RECIPIENT_ID
		            + " or "
		            + NOTIF_NUMBER);
		}
		key = createOdlEventKey();

		try {
			startTime = Long.parseLong(props.getProperty(START_TIME));
		} catch (NumberFormatException e) {
			throw new InvalidOdlEventException("invalid startTime for " + recipientId + " : " + userTel + ", " + e.getMessage());
		}

		odlTrigger = props.getProperty(ODL_TRIGGER_EVENT);

		String prop = props.getProperty(ODL_CODE);
		if (prop != null) {
			try {
				odlCode = Integer.parseInt(prop);
			} catch (NumberFormatException e) {
				throw new InvalidOdlEventException("invalid event status: " + prop);
			}
		}

		prop = props.getProperty(CURRENT_STATE);

		try {
			stateNo = Integer.parseInt(prop);
		} catch (NumberFormatException e) {
			throw new InvalidOdlEventException("invalid event next state: " + prop);
		}

		//need to reconstruct the last operation
		prop = props.getProperty(CURRENT_OPERATION);
		if (prop != null) {
			oprCode = Integer.parseInt(prop);
		}

		prop = props.getProperty(COMMAND);
		if(prop != null) {
			byte[] commandArray = prop.getBytes();
			command = new Command();
			command.restore(commandArray, 0, CommandHandler.OP_NAMES);
		}

        super.keepReferenceID(props.getProperty(SCHEDULER_ID));
        schedulerIdReminder = props.getProperty(SCHEDULER_ID_REMINDER);

		// retrieve message info
		super.parseMsgInfo(props);

		// retrieve extra properties
		super.parsingExtraProperties(props);
	}

	public void startNtfTimerTask(ManagedArrayBlockingQueue<Object> queue, long waitSeconds){
	    Timer timer = new Timer(true);
	    TimerTask timerTask = new NtfTimerTask(this, queue);
	    timer.schedule(timerTask, waitSeconds * 1000);
	}

	public String getRecipentId() {
		return recipientId;
	}

	public int getCurrentState() {
		return stateNo;
	}

	public void setCurrentState(int state) {
		stateNo = state;
		observableDelegate.markChanged();
	}

	public int getOperationCode() {
		return oprCode;
	}

	public void setCurrentOperation(int opr) {
		oprCode = opr;
		observableDelegate.markChanged();
	}

	public Command getCommand() {
		return command;
	}

	public void setCommand(Command _command) {
		command = _command;
		if (command.getCurrentOperation() != null) { //one command may not have operation
			oprCode = command.getCurrentOperation().getOpcode();
			observableDelegate.markChanged();
		}
	}

    @Override
    public void keepReferenceID(String referenceId) {
        super.keepReferenceID(referenceId);
        observableDelegate.markChanged();
    }

	/**
	 * user telephone number. will be the key for this event
	 */
	@Override
	public String getRecipient() {
		return recipientId;
	}

	public String getTelNumber() {
		return userTel;
	}

	public void setFromLogin(boolean isLogin) {
		fromLogin = isLogin;
	}

	public boolean isFromLogin() {
		return fromLogin;
	}

	public void setFromNotify(boolean fromNotify) {
		this.fromNotify = fromNotify;
	}

	public boolean isFromNotify() {
		return fromNotify;
	}

	/**
	 * the key for this event
	 */
	public String getOdlEventKey() {
		return key;
	}

	/**
	 * Creates the key for this event.
	 */
	private String createOdlEventKey() {
	    return (userTel.toLowerCase() + DASH + recipientId.toLowerCase());
	}

	public void setOdlTrigger(String trigger) {
		odlTrigger = trigger;
		observableDelegate.markChanged();
	}
	public String getOdlTrigger() {
		return odlTrigger;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setOdlCode(int code) {
		odlCode = code;
		observableDelegate.markChanged();
	}

	public String getSchedulerIdReminder() {
	    return this.schedulerIdReminder;
	}

	public void setSchedulerIdReminder(String schedulerIdReminder) {
	    this.schedulerIdReminder = schedulerIdReminder;
	    observableDelegate.markChanged();
	}

	/**
	 * method executed event state, trigger, code are changed.
	 */
	public void updateStateCommand() {

	}

	public int getOdlCode() {
		return odlCode;
	}

	public Properties getEventProperties() {
		Properties props = new Properties();

        if (recipientId != null) {
            props.put(Constants.DEST_RECIPIENT_ID, recipientId);
        }
        if (userTel != null) {
            props.put(NOTIF_NUMBER, userTel);
        }
		if (odlTrigger != null) {
			props.put(ODL_TRIGGER_EVENT, "" + odlTrigger);
		}

		if (odlCode != -1) {
			props.put(ODL_CODE, "" + odlCode);
		}

		props.put(CURRENT_STATE, "" + stateNo);
		props.put(CURRENT_OPERATION, "" + oprCode);
		props.put(START_TIME, Long.toString(startTime));

		if(command != null) {
			int size = command.getPackSize();
			byte[] commandArray = new byte[size];
			command.pack(commandArray, 0);
			props.put(COMMAND, new String(commandArray));
		}

		super.addMsgProperties(props);
		super.addExtraProperties(props);

		return props;
	}

    /**
     * @return Properties from this NtfEvent that are needed for a reminder notification
     */
	public Properties getReminderProperties(){
	    Properties props = super.getReminderProperties();
	    props.put(NOTIF_NUMBER, getTelNumber());

	    if (odlTrigger != null) {
	        props.put(ODL_TRIGGER_EVENT, "" + odlTrigger);
	    }
	    if (odlCode != -1) {
	        props.put(ODL_CODE, "" + odlCode);
	    }
	    props.put(CURRENT_STATE, "" + stateNo);
	    props.put(CURRENT_OPERATION, "" + oprCode);
	    props.put(START_TIME, Long.toString(startTime));

	    return props;
	}

	public boolean equals(Object o) {
		OdlEvent other = (OdlEvent) o;
		return key.equals(other.key);
	}

	public int hashCode() {
		return key.hashCode();
	}

	/**
	 * for logging
	 */
	public String toString() {
		return "Odluser: <" + getIdentity() + ">, state: <" + this.stateNo + ">, trigger: <"
			+ this.odlTrigger + ">, code: <" + odlCode + ">, operation: <" + State.getOprName(this.oprCode) + ">" +
			", key: <" + key + ">" + ", command: <" + command + ">";
	}

	public String getIdentity() {
	    return getRecipient() + " : " + getTelNumber();
	}

	/**
	 * Registers an observer to this event.
	 *
	 * @param observer Observer to register.
	 */
	public void addObserver(Observer observer) {
		observableDelegate.addObserver(observer);
	}

	/**
	 * Unregister an observer from this event.
	 *
	 * @param observer Observer to remove.
	 */
	public void deleteObserver(Observer observer) {
		observableDelegate.deleteObserver(observer);
	}

	/**
	 * Notifies registered observers.
	 */
	public void notifyObservers() {
		observableDelegate.notifyObservers(this);
	}

	public void setPhoneOnLock(long phoneOnLockId)
	{
	    this.phoneOnLockId=  phoneOnLockId;
	}
    public long getPhoneOnLock()
    {
        return phoneOnLockId;
    }

}
