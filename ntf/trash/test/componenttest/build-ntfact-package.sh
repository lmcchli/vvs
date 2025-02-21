#!/bin/sh

cp ../basictest/send ntfact/bin/.
tar -cf ntfact.tar ntfact
gzip ntfact.tar
