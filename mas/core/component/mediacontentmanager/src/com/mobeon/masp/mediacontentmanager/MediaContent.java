/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import com.mobeon.masp.mediacontentmanager.condition.ConditionInterpreter;
import com.mobeon.masp.mediacontentmanager.condition.ConditionInterpreterException;
import com.mobeon.masp.mediacontentmanager.condition.RhinoInterpreter;
import com.mobeon.masp.mediacontentmanager.condition.Condition;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A <code>MediaContent</code> has a unique ID
 * (within a MediaContent package) and a number of
 * {@link Message}s.
 * <p/>
 * Each <code>Message</code> has a {@link com.mobeon.masp.mediacontentmanager.condition.Condition}, i.e.
 * the condition for it to be played. The condition may have
 * parameters which is set by the values of the input
 * type {@link IMediaQualifier}s.
 * <p/>
 * The condition is interpreted with a {@link com.mobeon.masp.mediacontentmanager.condition.ConditionInterpreter}.
 * and based on input a content
 * will select any matching message.
 *
 * @author Mats Egland
 */
public class MediaContent {
    /**
     * The {@link com.mobeon.common.logging.ILogger} logger used for logging purposes.
     */
    protected static final ILogger LOGGER =
            ILoggerFactory.getILogger(MediaContent.class);
    /**
     * The unique id for this content.
     */
    private AtomicReference<String> id;
    /**
     * List of messages this content contains.
     */
    private List<Message> messageList =
            new ArrayList<Message>();

    /**
     * List of qualifiers this content has. This is
     * a list of the input to this content.
     */
    private List<IMediaQualifier> inputQualifierList =
            new ArrayList<IMediaQualifier>();
    /**
     * URI to the content-definition file that
     * has the definition of this content.
     */
    private URI contentDefinitionFile;
    /**
     * The condition interpreter.
     */
    private ConditionInterpreter conditionInterpreter;

    /**
     * If all <code>Message</code>s with a condition that is satisfied should
     * be returned.
     */
    private Boolean returnAll = false;

    /**
     * Creates a <code>MediaContent</code> with the specified
     * id. The id is trimmed from whitespaces before set.
     *
     * @param id                    The unique id for this content.
     * @param contentDefinitionFile URI to the definition file
     *                              for this content.
     * @throws IllegalArgumentException If the id is null or empty.
     */
    public MediaContent(String id, URI contentDefinitionFile) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "id argument to MediaContent constructor is null");
        } else if (id.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "id argument to MediaContent constructor is empty or only whitespaces");
        }
        this.contentDefinitionFile = contentDefinitionFile;
        this.id = new AtomicReference<String>(id.trim());
        //this.conditionInterpreter = new ConditionInterpreterImpl();
        this.conditionInterpreter = RhinoInterpreter.getInstance();
    }

    /**
     * Returns the Message which condition matches the
     * passed qualifiers.
     * <p/>
     * The method is not synchronized as all qualifiers and messages
     * should have been added before this method is called.
     *
     * @param qualifiers The qualifiers for the message.
     * @return The matching Message, or null if no matching message
     *         exists.
     * @throws IllegalArgumentException      If the number of qualifers specified
     *                                       does not match the number of qualifers
     *                                       for this content, or if the types of the
     *                                       qualifiers does not match the types of
     *                                       the qualifiers for this content.
     * @throws ConditionInterpreterException If a condition for a message
     *                                       was illegal.
     */
    Message getMatchingMessage(IMediaQualifier[] qualifiers) throws ConditionInterpreterException {
        if (!validateQualifiers(qualifiers)) {
            throw new IllegalArgumentException(
                    "The specified qualifers does not match the" +
                            " qualifiers for content with id=" + this.id);
        }
        if (messageList != null) {

            // The qualifers has no name, so append the
            // name according to the inputQualifierList member
            for (int i = 0; i < inputQualifierList.size(); i++) {
                qualifiers[i].setName(inputQualifierList.get(i).getName());
                qualifiers[i].setGender(inputQualifierList.get(i).getGender());
            }
            boolean result;
            if (returnAll) {
                // Return all Messages with a condition that is satisfied.
                Message resultMessage = new Message(new Condition("true"), this);
                for (Message message : messageList) {
                    result = conditionInterpreter.interpretCondition(
                            message.getCondition(), qualifiers);
                    if (result) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("MediaContent with id=" + this.getId()
                                    + " has a message that meets the input qualifers. Condition=" +
                                    message.getCondition().getCondition());
                        }
                        for (MessageElement element : message.getMessageElements()) {
                            resultMessage.appendMessageElement(element);
                        }
                    }
                }
                if (resultMessage.getMessageElements().size() > 0) {
                    return resultMessage;
                } else {
                    return null;
                }

            } else {
                for (Message message : messageList) {

                    result = conditionInterpreter.interpretCondition(
                            message.getCondition(), qualifiers);
                    if (result) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("MediaContent with id=" + this.getId()
                                    + " has a message that meets the input qualifers. Condition=" +
                                    message.getCondition().getCondition());
                        }
                        return message;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Validates if the specified qualifiers makes up a valid input
     * to this content. To be valid the number and types of the qualifers must match.
     *
     * @param qualifiers The qualifiers to validate against the qualifiers of this content.
     * @return true if the specified qualifiers is valid.
     */
    public boolean validateQualifiers(IMediaQualifier[] qualifiers) {
        if (qualifiers == null && inputQualifierList.size() > 0) {
            return false;
        } else if (qualifiers != null && (inputQualifierList.size() != qualifiers.length)) {
            return false;
        }
        for (int i = 0; i < inputQualifierList.size(); i++) {
            if (qualifiers[i].getType() != inputQualifierList.get(i).getType()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds the specified message to this content.
     * The method is not synchronized.
     *
     * @param message The message added.
     * @throws IllegalArgumentException If argument is null.
     */
    public void addMessage(Message message) {
        messageList.add(message);

    }

    /**
     * Appends the specified message to the end of list of
     * IMediaQualifiers.
     * The method is not synchronized.
     *
     * @param qualifier The qualifier that will be added.
     * @throws IllegalArgumentException If argument is null.
     */
    public void addQualifier(IMediaQualifier qualifier) {
        inputQualifierList.add(qualifier);

    }

    /**
     * Returns all <code>IMediaQualifier</code>s for this content.
     * The method is not synchronized as all qualifiers should have
     * been added before any is retreived.
     *
     * @return All qualifers for this content.
     */
    public List<IMediaQualifier> getAllQualifiers() {
        return new ArrayList<IMediaQualifier>(inputQualifierList);
    }

    /**
     * Returns the number of qualifiers for this content.
     * The method is not synchronized as all qualifiers should have
     * been added before this method is called.
     *
     * @return Number of qualifiers for content.
     */
    public int getNrOfQualifiers() {
        return inputQualifierList.size();
    }

    /**
     * Returns the id for this content. The id
     * of a content is always trimmed from
     * whitespaces.
     *
     * @return The id of this content.
     */
    public String getId() {
        return id.get();
    }

    /**
     * Returns URI to the content definition file, i.e. the
     * file that contains the definition for this content.
     *
     * @return The file that holds the
     *         definition of this content.
     */
    public URI getContentDefinitionFile() {
        return contentDefinitionFile;
    }

    /**
     * Set the value of returnAll, true or false. If the value is true, all
     * <code>Message</code>s with a condition that is satisfied will be
     * returned by getMatchingMessage(), otherwise only the first matching
     * <code>Message</code> is returned.
     * @param returnAll The value of returnAll.
     */
    public void setReturnAll(Boolean returnAll) {
        this.returnAll = returnAll;
    }
}
