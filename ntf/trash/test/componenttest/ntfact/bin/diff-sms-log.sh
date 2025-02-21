#!/bin/bash
#
# Used to diff values from smslog
# $1 smslog path
# To: $2,$3,$4
# From: $5,$6,$7
# Service type: $8
# ESM class: $9
# Protocol id: $10
# Priority flag: $11
# Shedule delivery time: $12
# Validity period: $13
# Registered delivery: $14
# Replace: $15
# Data coding: $16
# Message id: $17
# SMS length: $18
# Short message : $19
# OPTIONAL PARAMETERS
# -------------------
# 
# ====================================================
#
# Note:
#  $19 should be a regular expression matching the expeceted content of SMS.
#      Example: "s@Short message : You have a new voice message \(.*\)@@"
#
rm -f /tmp/msg1 /tmp/msg2

cat <<EOF > /tmp/msg1
To: $2,$3,$4
From: $5,$6,$7
Service type: $8
ESM class: $9
Protocol id: ${10}
Priority flag: ${11}
Shedule delivery time: ${12}
Validity period: ${13}
Registered delivery: ${14}
Replace: ${15}
Data coding: ${16}
Message id: ${17}
SMS length: ${18}
EOF

tail -18 $1 | head -13  > /tmp/msg2

diff /tmp/msg1 /tmp/msg2
tail -5 $1 | head -1 | sed -e "${19}" | tr -d '\n'
