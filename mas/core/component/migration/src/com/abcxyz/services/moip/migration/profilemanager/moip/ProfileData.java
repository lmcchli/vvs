/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip;

/**
 * Class containing attribute data from the UserRegister
 *
 * @author mande
 */
public class ProfileData {
    /**
     * Used for storing UserRegister data
     */
    private String[] data;

    public ProfileData(String[] data) {
        this.data = data.clone();
    }

    public String[] getData() {
        return data.clone();
    }

    public void setData(String[] data) {
        this.data = data.clone();
    }
}
