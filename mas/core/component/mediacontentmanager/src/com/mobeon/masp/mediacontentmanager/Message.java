/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import com.mobeon.masp.mediacontentmanager.condition.Condition;

import java.util.List;
import java.util.ArrayList;

/**
 * A <code>Message</code> consists of many {@link MessageElement}s to represent
 * a complete message. A  <code>Message</code> also belongs to a parent
 * <code>MediaContent</code> which the message is part of.
 * <p/>
 * Example:
 * The following message consist of three elements:
 * <ul>
 * <li>"You have.wav"</li>
 * <li>\<number\></li>
 * <li>"of messages.wav</li>
 * </ul>
 * <p/>
 * A message also has a <code>Condition</code> to be able to
 * select it among multiple messages. A client will then interpret
 * the condition of each message to select the mathing one.
 *
 * @author Mats Egland
 */
public class Message {
    /**
     * The condition for this Message. A condition
     * can be used to select a Message among several.
     */
    private Condition condition;

    /**
     * Synchronization object used for the
     * <code>messageElementList</code>.
     */
    private final Object LOCK = new Object();

    /**
     * List of {@link MessageElement}s in order, that together
     * make up this message.
     */
    List<MessageElement> messageElementList =
            new ArrayList<MessageElement>();


    /**
     * The parent content that this message is
     * part of.
     */
    private MediaContent parent;

    /**
     * Creates a message with the specified condition and
     * parent.
     *
     * @param cond The condition for this message to be played.
     * @throws IllegalArgumentException If a argument is null.
     */
    public Message(Condition cond, MediaContent parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Argument parent is null");
        } else if (cond == null) {
            throw new IllegalArgumentException("Argument condition is null");
        }

        this.parent = parent;
        this.condition = cond;
    }

    /**
     * Appends the specified <code>MessageElement</code> to the end of
     * the list of message-elements that makes up this message.
     *
     * @param messageElement The message-element that is appended.
     * @throws IllegalArgumentException If message-element is null or
     *                                  if the element is already in the list.
     */
    public void appendMessageElement(MessageElement messageElement) {
        if (messageElement == null) {
            throw new IllegalArgumentException("MessageElement is null");
        }
        synchronized (LOCK) {
            if (messageElementList.contains(messageElement)) {
                throw new IllegalArgumentException(
                        "MessageElement passed is already present in list.");
            }
            messageElementList.add(messageElement);
        }
    }

    /**
     * Returns the list of {@link MessageElement}s that comprises
     * this message. The returned list is a new list with a copy
     * of the content in the internal, so a client may iterate
     * over it safely.
     *
     * @return The list of elements of this message.
     */
    public List<MessageElement> getMessageElements() {
        synchronized (LOCK) {
            return new ArrayList<MessageElement>(messageElementList);
        }
    }

    /**
     * Returns the parent content this message
     * is part of.
     *
     * @return The parent of this Message.
     */
    public MediaContent getParent() {
        return parent;
    }

    /**
     * Returns the condition for this message.
     *
     * @return the condition as a {@link Condition}.
     */
    public Condition getCondition() {
        return condition;
    }

}
