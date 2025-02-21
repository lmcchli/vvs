PKGNAME=MOBYmas
BASE=../..
PKGBASE=$(BASE)/$(PKGNAME)"_patch"

all: mkproto updateproto

clean:
	rm -r spool/$(PKGNAME)
	rm built/$(PKGNAME).pkg
	rm pkgproto

mkproto:
#	@pkgproto $(PKGBASE)/src/=./ > $(PKGBASE)/proto/pkgproto
#	@sed -e 's% $(USER) $(GROUP)% root other%' < $(PKGBASE)/proto/pkgproto > $(PKGBASE)/proto/pkgproto2
#	@sed -e 's% none% application%' < $(PKGBASE)/proto/pkgproto2 > $(PKGBASE)/proto/pkgproto
#	@rm $(PKGBASE)/proto/pkgproto2
	@pkgproto $(PKGBASE)/src/=./ > $(PKGBASE)/prototype
	@sed -e 's% $(USER) $(GROUP)% root other%' < $(PKGBASE)/prototype > $(PKGBASE)/prototype2
#	@sed -e 's% none% application%' < $(PKGBASE)/prototype2 > $(PKGBASE)/prototype
	@mv $(PKGBASE)/prototype2 $(PKGBASE)/prototype
#	@rm $(PKGBASE)/prototype2

updateproto:
#	@echo "i pkginfo=$(PKGBASE)/proto/pkginfo" >> proto/pkgproto
#	@echo "i preinstall=$(PKGBASE)/proto/preinstall" >> proto/pkgproto
#	@echo "i postinstall=$(PKGBASE)/proto/postinstall" >> proto/pkgproto
#	@echo "i i.application=$(PKGBASE)/proto/i.application" >> proto/pkgproto
#	@echo "i patch_postinstall=$(PKGBASE)/proto/patch_postinstall" >> proto/pkgproto
#	@echo "i patch_checkinstall=$(PKGBASE)/proto/patch_checkinstall" >> proto/pkgproto
#	@echo "i checkinstall=$(PKGBASE)/proto/checkinstall" >> proto/pkgproto
	
#	@sed "s#^f none $PKGNAME/log/mas\.log#v none $PKGNAME/log/mas\.log#" proto/pkgproto> proto/p1
#	@mv proto/p1 proto/pkgproto

	@sed "s#^f none $PKGNAME/log/mas\.log#v none $PKGNAME/log/mas\.log#" prototype > p1
	@mv p1 prototype

build:
	pkgmk -o -f $(PKGBASE)/proto/pkgproto -d $(PKGBASE)/spool 
	pkgtrans $(PKGBASE)/spool $(PKGBASE)/built/$(PKGNAME).pkg $(PKGNAME) 
