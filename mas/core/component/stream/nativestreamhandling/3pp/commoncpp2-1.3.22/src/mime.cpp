// Copyright (C) 2001-2005 Open Source Telecom Corporation.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// As a special exception, you may use this file as part of a free software
// library without restriction.  Specifically, if other files instantiate
// templates or use macros or inline functions from this file, or you compile
// this file and link it with other files to produce an executable, this
// file does not by itself cause the resulting executable to be covered by
// the GNU General Public License.  This exception does not however    
// invalidate any other reasons why the executable file might be covered by
// the GNU General Public License.    
//
// This exception applies only to the code released under the name GNU
// Common C++.  If you copy code from other releases into a copy of GNU
// Common C++, as the General Public License permits, the exception does
// not apply to the code that you add in this way.  To avoid misleading
// anyone as to the status of such modified files, you must delete
// this exception notice from them.
//
// If you write modifications of your own for GNU Common C++, it is your choice
// whether to permit this exception to apply to your modifications.
// If you do not wish that, delete this exception notice.
//

#include <cc++/config.h>
#ifdef	CCXX_WITHOUT_EXTRAS
#include <cc++/export.h>
#endif
#include <cc++/file.h>
#include <cc++/thread.h>
#include <cc++/socket.h>
#include <cc++/exception.h>
#ifndef	CCXX_WITHOUT_EXTRAS
#include <cc++/export.h>
#endif
#include <cc++/mime.h>

#ifdef	CCXX_NAMESPACES
namespace ost {
#endif

MIMEMultipart::MIMEMultipart(const char *mt)
{
	const char *cp = strchr(mt, '/');
	if(cp)
		mt = ++cp;

	first = last = NULL;
	header[1] = NULL;
	header[0] = mtype;
	setString(boundry, sizeof(boundry), "xyzzy");
	snprintf(mtype, sizeof(mtype), "Content-Type: multipart/%s, boundry=%s", mt, boundry);
}

void MIMEMultipart::head(std::ostream *out)
{
	char **list = header;

	while(**list)
		*out << *(list++) << "\r\n";

	out->flush();
}

void MIMEMultipart::body(std::ostream *out)
{
	MIMEItemPart *item = first;

	while(item)
	{
		*out << "--" << boundry << "\r\n";
		item->head(out);
		*out << "\r\n";
		item->body(out);
		item = item->next;
	}
	*out << "--" << boundry << "--\r\n";
	out->flush();
}

MIMEItemPart::MIMEItemPart(MIMEMultipart *m, const char *ct)
{
	if(m->last)
	{
		m->last->next = this;
		m->last = this;
	}
	else
		m->first = m->last = this;
	next = NULL;
	ctype = ct;
}

MIMEMultipartForm::MIMEMultipartForm() :
MIMEMultipart("form-data")
{
}

void MIMEItemPart::head(std::ostream *out)
{
	*out << "Content-Type: %s\r\n", ctype;
}

MIMEFormData::MIMEFormData(MIMEMultipartForm *m, const char *n, const char *v) :
MIMEItemPart(m, "")
{
	name = n;
	content = v;
}

void MIMEFormData::head(std::ostream *out)
{
	*out << "Content-Disposition: form-data; name=\"" << name << "\"\r\n";
}

void MIMEFormData::body(std::ostream *out)
{
	*out << content << "\r\n";
}

#ifdef	CCXX_NAMESPACES
}
#endif

/** EMACS **
 * Local variables:
 * mode: c++
 * c-basic-offset: 8
 * End:
 */
