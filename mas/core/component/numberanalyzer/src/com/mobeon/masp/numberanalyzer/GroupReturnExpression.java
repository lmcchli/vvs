/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.numberanalyzer;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * ReturnExpression class that contains information of which groups (fields) that should be included when the analysed
 * number is returned.
 */
class GroupReturnExpression extends ReturnExpression {
    private List<String> groupList;

    GroupReturnExpression(String returnExpr) {
        super(returnExpr);
        loadGroupList();
    }

    public List<String> getGroupList() {
        return groupList;
    }

    private void loadGroupList() {
        groupList = new ArrayList<String>();
        List<String> gList = getGroupIdentfierList();
        String[] arr = returnExpr.split("\\$i\\d{1}");
        int i = 0;
        for (; i < arr.length; i++) {
            String s = arr[i];
            if (s.length() > 0) groupList.add(s);

            if (i < gList.size()) {
                groupList.add(gList.get(i));
            }
        }
        if (groupList.isEmpty()) {
            groupList.addAll(gList);
        } else if (i < gList.size()) {
            for (int j = gList.size() - 1; j >= i; j--) {
                groupList.add(gList.get(j));
            }
        }
        //System.out.println("groupList " + groupList);
    }

    private List<String> getGroupIdentfierList() {
        ArrayList<String> list = new ArrayList<String>();
        Pattern pattern = Pattern.compile("(\\$i\\d{1})");
        Matcher matcher = pattern.matcher(returnExpr);
        while (matcher.find()) {
            list.add(matcher.group(0));
        }
        return list;
    }
}
