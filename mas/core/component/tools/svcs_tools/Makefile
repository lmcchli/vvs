PKGNAME=MOBYmas
BASE=../..
PKGBASE=$(BASE)/$(PKGNAME)

all: mkproto updateproto build

clean:
	rm -r spool/$(PKGNAME)
	rm built/$(PKGNAME).pkg
	rm proto/pkgproto

mkproto:
	pkgproto $(PKGBASE)/src/=./ > $(PKGBASE)/proto/pkgproto
	@sed -e 's% $(USER) $(GROUP)% root other%' < $(PKGBASE)/proto/pkgproto > $(PKGBASE)/proto/pkgproto2
	@mv $(PKGBASE)/proto/pkgproto2 $(PKGBASE)/proto/pkgproto

updateproto:
	# copy install files
	@echo "i pkginfo=$(PKGBASE)/proto/pkginfo" >> proto/pkgproto
	@echo "i checkinstall=$(PKGBASE)/proto/checkinstall" >> proto/pkgproto
	@echo "i preinstall=$(PKGBASE)/proto/preinstall" >> proto/pkgproto
	@echo "i postinstall=$(PKGBASE)/proto/postinstall" >> proto/pkgproto
	@echo "i postremove=$(PKGBASE)/proto/postremove" >> proto/pkgproto
	@echo "i preremove=$(PKGBASE)/proto/preremove" >> proto/pkgproto
	@echo "i request=$(PKGBASE)/proto/request" >> proto/pkgproto
	@echo "i request=$(PKGBASE)/proto/copyright" >> proto/copyright
	
	# create links
	#@echo "s none ../logs/mas/mas.log=../../mas/log/mas.log" >> proto/pkgproto
	#@echo "s none ../logs/mas/process.log=../../mas/log/process.log" >> proto/pkgproto
	
	@sed "s#^f none $PKGNAME/log/mas\.log#v none $PKGNAME/log/mas\.log#" proto/pkgproto> proto/p1
	@mv proto/p1 proto/pkgproto
	#@sed "s#^f none MOBYhmon/log/scand\.log#v none MAS/log/scand\.log#" proto/pkgproto> proto/p1
	#@mv proto/p1 proto/pkgproto

build:
	#sync
	#sync
	pkgmk -o -f $(PKGBASE)/proto/pkgproto -d $(PKGBASE)/spool
	pkgtrans $(PKGBASE)/spool $(PKGBASE)/built/$(PKGNAME).pkg $(PKGNAME)

