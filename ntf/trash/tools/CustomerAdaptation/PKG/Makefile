PKGNAME=
BASE=

all: mkproto updateproto build

clean:
	@rm -r $(BASE)/delivery/ca

mkproto:
	pkgproto $(BASE)/delivery/ca/build/=./ > $(BASE)/delivery/ca/PKG/pkgproto
	@sed -e 's% $(USER) $(GROUP)% root other%' < $(BASE)/delivery/ca/PKG/pkgproto > $(BASE)/delivery/ca/PKG/pkgproto2
	@rm $(BASE)/delivery/ca/PKG/pkgproto
	@mv $(BASE)/delivery/ca/PKG/pkgproto2 $(BASE)/delivery/ca/PKG/pkgproto

updateproto:
	@echo "i pkginfo=$(BASE)/delivery/ca/PKG/pkginfo" >> $(BASE)/delivery/ca/PKG/pkgproto
	@echo "i request=$(BASE)/delivery/ca/PKG/request" >> $(BASE)/delivery/ca/PKG/pkgproto
	@echo "i preinstall=$(BASE)/delivery/ca/PKG/preinstall" >> $(BASE)/delivery/ca/PKG/pkgproto	
	@echo "i postinstall=$(BASE)/delivery/ca/PKG/postinstall" >> $(BASE)/delivery/ca/PKG/pkgproto
	@echo "i postremove=$(BASE)/delivery/ca/PKG/postremove" >> $(BASE)/delivery/ca/PKG/pkgproto

build:
	pkgmk -o -f $(BASE)/delivery/ca/PKG/pkgproto -d $(BASE)/delivery/ca/temp
	pkgtrans $(BASE)/delivery/ca/temp $(BASE)/delivery/ca/$(PKGNAME).pkg $(PKGNAME)
	rm -r $(BASE)/delivery/ca/temp
	rm $(BASE)/delivery/ca/PKG/pkgproto
	rm $(BASE)/delivery/ca/PKG/pkginfo
