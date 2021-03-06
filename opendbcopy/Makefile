PROJECT = unnamed
SRCDIR = src
TESTDIR = test
BINDIR = bin
LIBDIR = lib
WWWDIR =  www
LIBSFILE = libs
JAVADOCDIR = javadoc
LOCALDIR = local
MANIFESTFILE = manifest
TMPDIR = tmp

SHELL = /bin/sh
CD = CDPATH=""; cd
CYGPATH = cat
WGET = /usr/bin/wget
JAVAC = javac
JAVACFLAGS =  -d $(BINDIR)
JAVA = java
JAVAFLAGS = 
ASTG = $(JAVA) ch.devzone.astg.Main
ASTGFLAGS = -d $(SRCDIR)
CUP = $(JAVA) java_cup.Main
CUPFLAGS = -interface
JFLEX = $(JAVA) JFlex.Main
JFLEXFLAGS =
JAR = jar
JAVADOC = javadoc
JAVADOCFLAGS = -private  -d $(JAVADOCDIR) -use -version -author -windowtitle "$(PROJECT) Documentation" -link http://java.sun.com/j2se/1.3/docs/api
DIST_COMMON = README AUTHORS COPYING ChangeLog INSTALL NEWS TODO configure Makefile.in classpath.in run.in
DISTFILES = $(DIST_COMMON) $(EXTRA_DIST) $(LIBSFILE) $(MANIFESTFILE) bootstrap configure.aj Makefile.aj $(astgfiles) $(cupfiles) $(flexfiles) $(javafiles) $(localfiles)
BINDISTFILES = $(DIST_COMMON) $(EXTRA_BINDIST) $(BINDIR)/$(PROJECT).jar

astgfiles = $(shell find $(SRCDIR) $(TESTDIR) -name 'CVS' -prune -o -name '*.ast' -print 2>/dev/null)
cupfiles = $(shell find $(SRCDIR) $(TESTDIR) -name 'CVS' -prune -o -name '*.cup' -print 2>/dev/null)
flexfiles = $(shell find $(SRCDIR) $(TESTDIR) -name 'CVS' -prune -o -name '*.flex' -print 2>/dev/null)
intermediates = $(shell cat .astgfiles 2>/dev/null) $(astgfiles:%.ast=%Parser.java) $(astgfiles:%.ast=%Symbols.java) $(cupfiles:%.cup=%Parser.java) $(cupfiles:%.cup=%Symbols.java) $(flexfiles:%.flex=%Scanner.java)
javafiles = $(filter-out $(intermediates),$(shell find $(SRCDIR) $(TESTDIR) -name 'CVS' -prune -o -name '*.java' -print 2>/dev/null))
sources = $(patsubst %,'%',$(shell find `pwd`/$(SRCDIR) `pwd`/$(TESTDIR) -name 'CVS' -prune -o -name '*.java' -print 2>/dev/null | $(CYGPATH)))
packages = $(patsubst %,'%',$(sort $(shell sed -n -e 's/^package[ 	]*\(.*\);/\1/p' $(javafiles))))
localfiles = $(shell find $(LOCALDIR) -name 'CVS' -prune -o -type f -print)

classpath := $(shell ./classpath)
export CLASSPATH := /usr/local/classes:.:$(classpath)
JIKESPATH := 
ifdef JIKESPATH
	export JIKESPATH := $(JIKESPATH):$(classpath)
endif

define delete-tmpdir
	rm -rf $(TMPDIR)
endef

define create-tmpdir
	$(call delete-tmpdir); \
	mkdir $(TMPDIR) $(addprefix $(TMPDIR)/,$(1))
endef

define create-jar
	here=`pwd | $(CYGPATH)`; \
	if [[ -e $(MANIFESTFILE) ]]; then \
		($(CD) $(TMPDIR); $(JAR) cvfm $$here/$(1) $$here/$(MANIFESTFILE) .); \
	else \
		($(CD) $(TMPDIR); $(JAR) cvf $$here/$(1) .); \
	fi
endef

define create-tarball
	($(CD) $(TMPDIR); tar chovf - $(PROJECT)) | GZIP=--best gzip -c >$(PROJECT).tar.gz
endef

define copy-files
	($(CD) $(1); tar cf - $(2) 2>/dev/null) | ($(CD) $(TMPDIR)/$(3); tar xf -)
endef

define copy-libdir
	here=`pwd | $(CYGPATH)`; \
	for file in $(LIBDIR)/*; do \
		if [[ $$file == *.jar ]]; then \
			cp $$file $(TMPDIR)/$(1); \
		fi; \
		if [[ -d $$file/ ]]; then \
			($(CD) $$file; $(JAR) cvf $$here/$(TMPDIR)/$(1)/$${file##*/}.jar `find . -name '*.class'`); \
		fi; \
	done
endef

all: astg cup jflex
	mkdir -p $(BINDIR)
	$(JAVAC) $(JAVACFLAGS) $(sources)

astg: $(astgfiles:%.ast=%Parser.java)

cup: $(cupfiles:%.cup=%Parser.java)

jflex: $(flexfiles:%.flex=%Scanner.java)

lib:
	if [[ -f $(LIBSFILE) ]]; then \
		shopt -s nullglob; \
		here=`pwd | $(CYGPATH)`; \
		mkdir -p $(LIBDIR); \
		$(CD) $(LIBDIR); \
		while read line; do \
			if [[ $$line == *://* ]]; then \
				uri=$$line; \
			elif [[ -n $$line && $$line != \#* ]]; then \
				$(WGET) -N $$uri/$$line; \
				libs=$$libs\|$$line\|; \
			fi; \
		done < $$here/$(LIBSFILE); \
		for file in *; do \
			if [[ $$libs != *\|$$file\|* ]]; then \
				rm -v $$file; \
			fi \
		done \
	fi

javadoc: astg
	mkdir -p $(JAVADOCDIR)
	$(JAVADOC) $(JAVADOCFLAGS) $(packages)

clean:
	-rm -rf $(BINDIR) $(JAVADOCDIR)
	-cleanfiles="$(intermediates) $(CLEANFILES)"; \
	echo "$$cleanfiles" | grep -q '^ *$$' || rm -f $$cleanfiles

cvscheck:
	rm -rf cvscheck
	cvs co -d cvscheck `cat CVS/Repository`
	$(CD) cvscheck; ./bootstrap; ./configure
	if [[ -d $(LIBDIR) ]]; then \
		cp -a $(LIBDIR) cvscheck; \
	fi
	$(MAKE) -C cvscheck
	rm -rf cvscheck

eclipse:
	if [[ ! -e .project ]]; then \
		echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" >.project; \
		echo "<projectDescription>" >>.project; \
		echo "  <name>$(PROJECT)</name>" >>.project; \
		echo "  <comment></comment>" >>.project; \
		echo "  <projects>" >>.project; \
		echo "  </projects>" >>.project; \
		echo "  <buildSpec>" >>.project; \
		echo "    <buildCommand>" >>.project; \
		echo "      <name>org.eclipse.jdt.core.javabuilder</name>" >>.project; \
		echo "      <arguments>" >>.project; \
		echo "      </arguments>" >>.project; \
		echo "    </buildCommand>" >>.project; \
		echo "  </buildSpec>" >>.project; \
		echo "  <natures>" >>.project; \
		echo "    <nature>org.eclipse.jdt.core.javanature</nature>" >>.project; \
		echo "  </natures>" >>.project; \
		echo "</projectDescription>" >>.project; \
		echo "Wrote new .project file"; \
	fi
	echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" >.classpath; \
	echo "<classpath>" >>.classpath; \
	echo "  <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>" >>.classpath; \
	for dir in $(SRCDIR) $(TESTDIR) $(LOCALDIR); do \
		if [[ -d $$dir ]]; then \
			echo "  <classpathentry excluding=\"**/CVS/*\" kind=\"src\" path=\"$$dir\"/>"  >>.classpath; \
		fi \
	done; \
	echo "  <classpathentry kind=\"output\" path=\"$(BINDIR)\"/>" >>.classpath; \
	for dir in $(LIBDIR)/*; do \
		if [[ $$dir == *.jar || -d $$dir ]]; then \
			echo "  <classpathentry kind=\"lib\" path=\"$$dir\"/>" >>.classpath; \
		fi; \
	done; \
	echo "</classpath>" >>.classpath; \
	echo "Wrote new .classpath file"

jar: all
	$(call create-tmpdir)
	$(call copy-files,$(BINDIR),`find . -name 'CVS' -prune -o -name '*.class' -print`,.)
	$(call copy-files,$(LOCALDIR),`find . -name 'CVS' -prune -o -type f -print`,.)
	$(call create-jar,$(BINDIR)/$(PROJECT).jar)
	$(call delete-tmpdir)

war: all
	$(call create-tmpdir,WEB-INF WEB-INF/classes WEB-INF/lib)
	$(call copy-files,$(BINDIR),`find . -name 'CVS' -prune -o -name '*.class' -print`,WEB-INF/classes)
	$(call copy-files,$(LOCALDIR),`find . -name 'CVS' -prune -o -type f -print`,WEB-INF/classes)
	$(call copy-files,$(WWWDIR),`find . -name 'CVS' -prune -o -type f -print`,.)
	$(call copy-files,.,web.xml,WEB-INF)
	$(call copy-libdir,WEB-INF/lib)
	$(call create-jar,$(PROJECT).war)
	$(call delete-tmpdir)

dist:
	$(call create-tmpdir,$(PROJECT))
	$(call copy-files,.,$(DISTFILES),$(PROJECT))
	$(call create-tarball)
	$(call delete-tmpdir)

libdist:
	$(call create-tmpdir,$(PROJECT) $(PROJECT)/$(LIBDIR))
	$(call copy-files,.,$(DISTFILES),$(PROJECT))
	$(call copy-libdir,$(PROJECT)/$(LIBDIR))
	$(call create-tarball)
	$(call delete-tmpdir)

bindist: jar
	$(call create-tmpdir,$(PROJECT) $(PROJECT)/$(BINDIR) $(PROJECT)/$(LIBDIR))
	touch $(TMPDIR)/$(PROJECT)/Makefile.in
	$(call copy-files,.,$(BINDISTFILES),$(PROJECT))
	$(call copy-libdir,$(PROJECT)/$(LIBDIR))
	$(call create-tarball)
	$(call delete-tmpdir)

%.cup: %.ast
	$(ASTG) $(ASTGFLAGS) $< $@

%Parser.java: %.cup
	$(CD) $(<D); \
	$(CUP) $(CUPFLAGS) -parser $(*F)Parser -symbols $(*F)Symbols <$(<F)

%Scanner.java: %.flex
	$(JFLEX) $(JFLEXFLAGS) $<

.PHONY: all astg cup jflex javadoc lib clean cvscheck jar war dist libdist bindist eclipse

.SILENT: lib cvscheck jar war dist libdist bindist eclipse
