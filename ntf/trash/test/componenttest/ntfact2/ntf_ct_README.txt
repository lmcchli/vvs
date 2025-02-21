

                Automatic Component Test (CT) for NTF
                =====================================


1. Introduction

    This is an effort to make automatic CT for NTF. It is independent
    of previous attempts, e.g. the one described in 
    "ntf/test/componenttest/doc/NTF ACT Suggestion.doc".

    The test-cases are all stand-alone shell (/bin/sh) scripts which
    are using some common functions. The scrips are short and simple
    so the user should not hesitate to check them out.

    The (24) automatic test-cases takes about 30 minutes to execute if
    no errors are encountered.


1.1 Automatic test-cases

    Only regression tests are automatized. The set of regression test
    (marked "Regr" in the excel document) is a moving target.

    The 15 installation related test-cases (in the 40150xx series) are
    not automatic, they are still manual.


2. Setup environment

    There is no help for setting up the environment (except for the
    "Prepare MUR" described below), it have to be done
    manually. Perhaps the script
    "/ntf/test/componenttest/ntfact/ntfact-setup.sh" can be of help,
    but it has not been tested.

    Necessary other components;

        o MCR

        o MUR

        o MS

        o MER

    Necessary test tools;

        o The "send" script (used in NTF basic tests)

        o SMS-C

2.1 SSH

    "ssh" is used to execute commands on other machines, e.g. to
    re-start the ntf. To avoid typing passwd all the time, create a
    ~/.ssh/authorized_keys on those host.


3. Configure Tests

    All test configuration is made in a configuration file. This file
    will be sourced by all test-scripts, so it must be in
    "shell-format".

    Best way is to take the commented example at;

        "/vobs/ipms/ntf/test/componenttest/ntfact2/ntf_ct.cfg"

    and modify it for your needs.


4. Prepare MUR

    Before each test-case the MUR data is defined. This is done by two
    scripts;

        o restoremur

        o preparemur

    These script are executed like "restoremur;preparemur" before
    every test-case. After the test the MUR is left as-is, so it is up
    to the next test-case script to restore the MUR.

    The "preparemur" creates 2 users and 2 CoS'es, and the
    "restoremur" deletes them.

    The tester (you) may want to alter these scripts to suite any
    particular purpose (like the segmented CoS).


5. Execute Tests

    The test-scripts assumes that "send" and SMS-C runs on the same
    host as the script itself. Other hosts are accessed with "ssh".


    The test-cases can be invoked individually. Example;

        ./ntf_ct_4001001.sh              # (prints help-text)
        ./ntf_ct_4001001.sh ntf_ct.cfg   # (executes the test-case)


    All automated test-cases can be executed with;

        ./ntf_ct_runtests.sh ntf_ct.cfg


    The best way is probably to execute the firs test
    (ntf_ct_4001001.sh) individually to check the environment. Once it
    works, run all tests and handle any failing tests.


5.1 Special test-cases

    TC 4001070 (SMSCBackup) is a manual test but some help with the
    test preparation is made with;

        ./ntf_ct_4001070.sh ntf_ct.cfg [prepare|restore]

