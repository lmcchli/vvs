PKGNAME=LMENtf
BASE=../..
INSTALLBASE=/vobs/ipms/ntf/src/install/LMENtf
THIS_VU := $(shell cleartool pwv -sho)
VERSION?=$(THIS_VU)
all: mkproto updateproto build

clean:
	@rm -r /vobs/ipms/ntf/src/install/temp/$(PKGNAME)
	@rm $(BASE)/delivery/$(PKGNAME).pkg
	@rm LMENtf/pkgproto
	@rm LMENtf/pkgproto2
	@rm LMENtf/pkginfo
	@rm LMENtf.response
mkproto:
	@sed -e 's%__DATE__%$(shell date +%Y-%m-%d)%' < $(INSTALLBASE)/pkginfo.tmp > $(INSTALLBASE)/pkginfo.tmp2
	@sed -e 's%__VERSION__%$(VERSION)%' < $(INSTALLBASE)/pkginfo.tmp2 > $(INSTALLBASE)/pkginfo
	@rm LMENtf/pkginfo.tmp2
	pkgproto $(BASE)/build/=./ > LMENtf/pkgproto
	@sed -e 's% $(USER) $(GROUP)% root other%' < $(INSTALLBASE)/pkgproto > $(INSTALLBASE)/pkgproto2
	@rm $(INSTALLBASE)/pkgproto
	@mv $(INSTALLBASE)/pkgproto2 $(INSTALLBASE)/pkgproto
updateproto:
	@echo "i pkginfo=$(INSTALLBASE)/pkginfo" >> LMENtf/pkgproto
	@echo "i postinstall=$(INSTALLBASE)/postinstall" >> LMENtf/pkgproto
	@echo "i preremove=$(INSTALLBASE)/preremove" >> LMENtf/pkgproto
	@echo "i postremove=$(INSTALLBASE)/postremove" >> LMENtf/pkgproto
build:
	pkgmk -o -f $(INSTALLBASE)/pkgproto -v $(VERSION) -d /vobs/ipms/ntf/src/install/temp
	pkgtrans /vobs/ipms/ntf/src/install/temp $(BASE)/delivery/ntf_files/$(PKGNAME).pkg $(PKGNAME)
	rm -rf /vobs/ipms/ntf/src/install/temp/$(PKGNAME)
